package org.mokai.connector.http;

import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.Processor;

public class HttpConnector implements Processor, ExposableConfiguration<HttpConfiguration> {
	
	private HttpConfiguration configuration;
	
	public HttpConnector() {
		this(new HttpConfiguration());
	}
	
	public HttpConnector(HttpConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public final void process(Message message) throws Exception {
		
		if (configuration.getUrl() == null) {
			throw new IllegalArgumentException("URL not provided");
		}
		
		String url = configuration.getUrl();
		
		boolean existsQuery = false;
		for (Map.Entry<String, Object> entry : message.getProperties().entrySet()) {
			url += getOperator(existsQuery);
			
			// by default, use the key from the property
			String key = entry.getKey();
			
			// check if there is a mapper and apply it
			if (configuration.getMapper().containsKey(key)) {
				key = configuration.getMapper().get(key);
			}
			
			url += key + "=" + entry.getValue();
			
			existsQuery = true;
		}
		
		// addd the additional query
		String additionalQuery = configuration.getAdditionalQuery();
		if (additionalQuery != null) {
			if (!additionalQuery.startsWith("&")) {
				additionalQuery = "&" + additionalQuery;
			}
			
			url += additionalQuery;
		}
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse = httpClient.execute(httpGet);
		
		int responseCode = httpResponse.getStatusLine().getStatusCode();
		
		if (configuration.isThrowExceptionOnFailure() && (responseCode < 100 || responseCode >= 300)) {
			String uri = httpGet.getURI().toString();
			String statusText = httpResponse.getStatusLine() != null ? httpResponse.getStatusLine().getReasonPhrase() : null;
			
			Header locationHeader = httpResponse.getFirstHeader("location");
			String redirectLocation = "";
			if (locationHeader != null) {
				redirectLocation = locationHeader.getValue();
			}
			
			throw new HttpOperationFailedException(uri, redirectLocation, responseCode, statusText, "");
		}
	}
	
	private String getOperator(boolean existsQuery) {
		if (existsQuery) {
			return "&";
		}
		
		return "?";
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
