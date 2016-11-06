package org.mokai.connector.http.test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.mokai.Message;
import org.mokai.connector.http.HttpConfiguration;
import org.mokai.connector.http.HttpConnector;
import org.mokai.connector.http.HttpOperationFailedException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpConnectorTest {

	private LocalTestServer testServer;

	@BeforeMethod
	public void setUp() throws Exception {
		testServer = new LocalTestServer(null, null);
		testServer.start();
	}

	@AfterMethod
	public void tearDown() throws Exception {
		if (testServer != null) {
			testServer.stop();
		}
	}

	@Test
	public void shouldProcessGetRequest() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");

		Message message = new Message();
		message.setProperty("to", "3002175604");
		message.setProperty("from", "3542");
		message.setProperty("text", "test · script @áÁ");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(message);

		Assert.assertEquals(mockHandler.getReceivedMethod(), "GET");
		mockHandler.validateExpectedProperties("to=3002175604&from=3542&text=test · script @áÁ");
		Assert.assertTrue(message.getProperty("responseCode", Integer.class) == HttpStatus.SC_OK);
	}

	@Test
	public void shouldAddAmpToGetRequestIfQueryString() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/?to=3002175604");

		Message message = new Message();
		message.setProperty("from", "3542");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(message);

		mockHandler.validateExpectedProperties("to=3002175604&from=3542");
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldNotConfigureIfUrlIsInvalid() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://invalid:port/");

		HttpConnector connector = new HttpConnector(configuration);
		connector.configure();
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldNotProcessIfUrlIsInvalid() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("htp://invalidscheme/");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}

	@Test
	public void shouldProcessPostRequest() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setMethod("POST");

		Message message = new Message();
		message.setProperty("to", "3002175604");
		message.setProperty("from", "3542");
		message.setProperty("text", "test · script @áÁ");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(message);

		Assert.assertEquals(mockHandler.getReceivedMethod(), "POST");
		mockHandler.validateExpectedProperties("to=3002175604&from=3542&text=test · script @áÁ");
		Assert.assertTrue(message.getProperty("responseCode", Integer.class) == HttpStatus.SC_OK);
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldNotConfigureWithNullUrl() throws Exception {

		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();

		HttpConnector connector = new HttpConnector(configuration);
		connector.configure();

	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldNotProcessWithNullUrl() throws Exception {

		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());

	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldNotConfigureUnknownHttpMethod() throws Exception {

		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setMethod("UNKNOWN");

		HttpConnector connector = new HttpConnector(configuration);
		connector.configure();

	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldNotProcessUnknownHttpMethod() throws Exception {

		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setMethod("UNKNOWN");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}

	@Test
	public void testBasicAuth() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setMethod("GET");
		configuration.setBasicAuth(true);
		configuration.setUsername("german");
		configuration.setPassword("password");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());

		Header header = mockHandler.getHeader("Authorization");
		Assert.assertNotNull(header);
		Assert.assertNotNull(header.getValue());
		Assert.assertEquals(header.getValue(), "Basic Z2VybWFuOnBhc3N3b3Jk");
	}

	@Test
	public void testMapperAndAdditionalQuery() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_OK);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		Map<String,String> additionalParams = new HashMap<String,String>();
		additionalParams.put("account", "german");
		additionalParams.put("password", "escobar");
		configuration.setAdditionalParams(additionalParams);

		Map<String,String> mapper = new HashMap<String,String>();
		mapper.put("to", "to1");
		mapper.put("from", "from1");
		mapper.put("text", "text1");
		configuration.setMapper(mapper);

		Message message = new Message();
		message.setProperty("to", "3002175604");
		message.setProperty("from", "3542");
		message.setProperty("text", "test");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(message);

		mockHandler.validateExpectedProperties("to1=3002175604&from1=3542&text1=test&account=german&password=escobar");
	}

	@Test
	public void shouldNotThrowExceptionOnFailure() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_BAD_REQUEST);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setThrowExceptionOnFailure(false);

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}

	@Test(expectedExceptions=HttpOperationFailedException.class)
	public void shouldThrowExceptionOnFailure() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_BAD_REQUEST);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());

	}

	@Test(expectedExceptions=HttpOperationFailedException.class)
	public void shouldFailUrlNotFound() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_NOT_FOUND);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNoUrlSpecified() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_NOT_FOUND);
		testServer.register("/", mockHandler);

		HttpConnector connector = new HttpConnector();
		connector.process(new Message());
	}

	@Test
	public void testDontThrowExceptionOnFailure() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler(HttpStatus.SC_NOT_FOUND);
		testServer.register("/", mockHandler);

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setThrowExceptionOnFailure(false);

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}

	@Test(expectedExceptions=SocketTimeoutException.class)
	public void sholdThrowSocketTimeoutException() throws Exception {
		testServer.register("/", new HttpRequestHandler() {

			@Override
			public void handle(HttpRequest request, HttpResponse response,
					HttpContext context) throws HttpException, IOException {
				try { Thread.sleep(5000); } catch (InterruptedException e) {}
			}

		});

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setThrowExceptionOnFailure(false);
		configuration.setSocketTimeout(3000);

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}

	@Test(expectedExceptions=ConnectTimeoutException.class)
	public void shoudlThrowConnectionTimeoutException() throws Exception {

		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://255.255.255.1/");
		configuration.setThrowExceptionOnFailure(false);
		configuration.setConnectionTimeout(3000);

		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}

	private String getHost() {
		return testServer.getServiceHostName();
	}

	private int getPort() {
		return testServer.getServicePort();
	}

	private class MockRequestHandler implements HttpRequestHandler {

		private Set<String> receivedProperties = new HashSet<String>();

		private String receivedMethod;

		private Header[] receivedHeaders;

		private int responseStatus;

		public MockRequestHandler(int responseStatus) {
			this.responseStatus = responseStatus;
		}

		@Override
		public void handle(HttpRequest request, HttpResponse response, HttpContext context)
				throws HttpException, IOException {

			try {

				receivedMethod = request.getRequestLine().getMethod();
				receivedHeaders = request.getAllHeaders();

				if ("GET".equalsIgnoreCase(receivedMethod)) {

					URI uri = new URI(request.getRequestLine().getUri());
					List<NameValuePair> properties = URLEncodedUtils.parse(uri, "ISO-8859-1");
					for (NameValuePair property : properties) {
				    	receivedProperties.add(property.getName() + "=" + property.getValue());
				    }

				} else if ("POST".equalsIgnoreCase(receivedMethod)) {

					if (!BasicHttpEntityEnclosingRequest.class.isInstance(request)) {
						Assert.fail("Request is not an instance of BasicHttpEntityEnclosingRequest");
						return;
					}

					BasicHttpEntityEnclosingRequest entityRequest = (BasicHttpEntityEnclosingRequest) request;
					HttpEntity entity = entityRequest.getEntity();
				    List<NameValuePair> properties = URLEncodedUtils.parse(entity);
				    for (NameValuePair property : properties) {
				    	receivedProperties.add(property.getName() + "=" + property.getValue());
				    }

				} else {
					Assert.fail("Method '" + receivedMethod +"' not recognized!");
				}

			} catch (URISyntaxException e) {
				throw new HttpException(e.getMessage());
			}

			response.setStatusCode(responseStatus);
		}

		private Set<String> parseQueryString(String queryString) {
			Set<String> ret = new HashSet<String>();

			if (queryString != null && !"".equals(queryString)) {
				StringTokenizer st = new StringTokenizer(queryString, "&");
				while (st.hasMoreTokens()) {
					ret.add(st.nextToken());
				}
			}

			return ret;
		}

		public void validateExpectedProperties(String queryString) {
			Set<String> expectedProperties = parseQueryString(queryString);

			for (String item : expectedProperties) {
				Assert.assertTrue(receivedProperties.contains(item));
			}
		}

		public String getReceivedMethod() {
			return receivedMethod;
		}

		public Header getHeader(String name) {
			for (Header header : receivedHeaders) {
				if (name.equals(header.getName())) {
					return header;
				}
			}

			return null;
		}

	}
}
