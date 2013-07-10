package org.mokai.connector.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.mokai.Configurable;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("HTTP")
@Description("Sends messages through HTTP")
public class HttpConnector implements Processor, ExposableConfiguration<HttpConfiguration>, Configurable {
	
	private Logger log = LoggerFactory.getLogger(HttpConnector.class);
	
	private HttpConfiguration configuration;
	
	/**
	 * Constructor. Initializes with an empty configuration.
	 */
	public HttpConnector() {
		this(new HttpConfiguration());
	}
	
	/**
	 * Constructor. Initializes with the specified configuration.
	 * 
	 * @param configuration used to initialize the connector.
	 */
	public HttpConnector(HttpConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * It's a good practice to fail as soon as possible. We are using this callback method to validate that the 
	 * configuration is valid; if it is not, it fails immediately and won't allow the application to start.
	 */
	@Override
	public void configure() throws Exception {
		validateConfiguration();
	}
	
	/**
	 * Helper method that validates if the configuration is valid.
	 * 
	 * @throws IllegalArgumentException if the configuration is invalid.
	 */
	private void validateConfiguration() throws IllegalArgumentException {
		// configuration should not be null
		if (configuration == null) {
			throw new IllegalArgumentException("No configuration specified");
		}
		
		// url should not be null
		if (configuration.getUrl() == null) {
			throw new IllegalArgumentException("URL not provided");
		}
		
		// url should be valid
		try { 
			new URL(configuration.getUrl());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		
		// method should be supported
		String method = configuration.getMethod();
		if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
			throw new IllegalArgumentException("Unrecognized HTTP method '" + method + "'. Possible values are GET or POST");
		}
	}

	@Override
	public void destroy() throws Exception { }

	@Override
	public final void process(Message message) throws Exception {
		long startTime = System.currentTimeMillis();
		
		validateConfiguration();
		
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, configuration.getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(httpParams, configuration.getSocketTimeout());
		
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpUriRequest request = buildRequest(message, configuration.getUrl());
		if (request == null) {
			throw new IllegalArgumentException("HTTP method '" + configuration.getMethod() + "' not supported");
		}
		
		// include basic authentication
		if (configuration.isBasicAuth()) {
			String auth = buildBasicAuth(configuration.getUsername(), configuration.getPassword());
			request.addHeader("Authorization", "Basic " + auth);
		}
		
		// execute request
		
		HttpResponse httpResponse = httpClient.execute(request);
		
		// process response - set the response code in the commandStatus property
		int responseCode = httpResponse.getStatusLine().getStatusCode();
		message.setProperty("responseCode", responseCode);
		
		log.trace("HTTP request took " + (System.currentTimeMillis() - startTime) + " millis");
		
		if (configuration.isThrowExceptionOnFailure() && (responseCode < 100 || responseCode >= 300)) {
			String uri = request.getURI().toString();
			String statusText = httpResponse.getStatusLine() != null ? httpResponse.getStatusLine().getReasonPhrase() : null;
			
			Header locationHeader = httpResponse.getFirstHeader("location");
			String redirectLocation = "";
			if (locationHeader != null) {
				redirectLocation = locationHeader.getValue();
			}
			
			throw new HttpOperationFailedException(uri, redirectLocation, responseCode, statusText, "");
		}
	}
	
	/**
	 * Helper method. Builds the actual HTTP request (GET, POST, etc.) that is going to be sent to the URL.
	 * 
	 * @param message used to extract properties
	 * @param url the URL to which we are sending the request.
	 * @return a HttpUriRequest that is used by the Apache Http Client internally to send the request or null if the 
	 * 		HTTP method is not supported.
	 * @throws Exception if something goes wrong.
	 */
	private HttpUriRequest buildRequest(Message message, String url) throws Exception {
		
		HttpUriRequest request = null; // we will return this object
		
		final String method = configuration.getMethod();
		String properties = buildPropertiesString(message);
		
		if ("GET".equalsIgnoreCase(method)) {
			
			String urlWithProperties = url;
			
			// append the properties to the URL
			String query = URI.create(url).getQuery();
			if (query != null && !"".equals(query)) {
				urlWithProperties += "&" + properties;
			} else {
				urlWithProperties += "?" + properties;
			}
			
			request = new HttpGet(urlWithProperties);
			
		} else if ("POST".equalsIgnoreCase(method)) {
			
			// append the properties to the body
			HttpPost post = new HttpPost(url);
			post.setURI(URI.create(url));
			post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=" + configuration.getEncoding());
			post.setEntity(new StringEntity(properties));
			
			request = post;
			
		}
		
		return request;
	}
	
	/**
	 * Helper method. Retrieves the properties from the message and creates the query string. 
	 * 
	 * @param message the message from which we are retrieiving the properties.
	 * @return a String with the query string. An empty String if no properties.
	 */
	private String buildPropertiesString(Message message) {
		
		boolean hasProperties = false;
		String properties = "";
		
		for (Map.Entry<String, Object> entry : message.getProperties().entrySet()) {
			try { 
				String key = entry.getKey();
				if (configuration.getMapper().containsKey(key)) {
					key = configuration.getMapper().get(key);
				}
				
				properties += getOperator(hasProperties) + encode(key, entry.getValue() + "");
				hasProperties = true;
			} catch (Exception e) {
				log.error("Exception encoding key '" + entry.getKey() + "' and value '" + entry.getValue() + "': " + e.getMessage(), e);
			}
		}
		
		Map<String,String> additionalParams = configuration.getAdditionalParams();
		for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
			try {
				properties += getOperator(hasProperties) + encode(entry.getKey(), entry.getValue());
				hasProperties = true;
			} catch (Exception e) {
				log.error("Exception encoding key '" + entry.getKey() + "' and value '" + entry.getValue() + "': " + e.getMessage(), e);
			}
		}
		
		return properties;
	}
	
	@SuppressWarnings("restriction")
	private String buildBasicAuth(String username, String password) {
		String userPassword = username + ":" + password;
		return new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
	}
	
	private String encode(String key, String value) throws Exception {
		String encoding = configuration.getEncoding();
		return URLEncoder.encode(key, encoding) + "=" + URLEncoder.encode(value, encoding);
	}
	
	private String getOperator(boolean existsQuery) {
		if (existsQuery) {
			return "&";
		}
		
		return "";
	}

	@Override
	public boolean supports(Message message) {
		return true;
	}

	@Override
	public final HttpConfiguration getConfiguration() {
		return configuration;
	}

}
