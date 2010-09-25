package org.mokai.connector.http.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.localserver.LocalTestServer;
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
	public void testSimpleMessage() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler("to=3002175604&from=3542&text=test+á+script+@", HttpStatus.SC_OK);
		testServer.register("/", mockHandler);
		
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		
		Message message = new Message();
		message.setProperty("to", "3002175604");
		message.setProperty("from", "3542");
		message.setProperty("text", "test á script @");
		
		HttpConnector connector = new HttpConnector(configuration);
		connector.process(message);
		
		mockHandler.isAssertSatisfied();
	}
	
	@Test
	public void testMapperAndAdditionalQuery() throws Exception {
		MockRequestHandler mockHandler = 
			new MockRequestHandler("to1=3002175604&from1=3542&text1=test&account=german&password=escobar", HttpStatus.SC_OK);
		testServer.register("/", mockHandler);
		
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setAdditionalQuery("account=german&password=escobar");
		
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
		
		mockHandler.isAssertSatisfied();
	}
	
	@Test(expectedExceptions=HttpOperationFailedException.class)
	public void shouldFailUrlNotFound() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler("", HttpStatus.SC_NOT_FOUND);
		testServer.register("/", mockHandler);
		
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		
		HttpConnector connector = new HttpConnector(configuration);
		connector.process(new Message());
	}
	
	@Test
	public void testDontThrowExceptionOnFailure() throws Exception {
		MockRequestHandler mockHandler = new MockRequestHandler("", HttpStatus.SC_NOT_FOUND);
		testServer.register("/", mockHandler);
		
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUrl("http://" + getHost() + ":" + getPort() + "/");
		configuration.setThrowExceptionOnFailure(false);
		
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
		
		private Set<String> receivedQuery = new HashSet<String>();
		
		private Set<String> expectedQuery = new HashSet<String>();
		
		private int responseStatus;
		
		public MockRequestHandler(String query, int responseStatus) {
			this.responseStatus = responseStatus;
			
			if (query != null && !"".equals(query)) {
				StringTokenizer st = new StringTokenizer(query, "&");
				while (st.hasMoreTokens()) {
					expectedQuery.add(st.nextToken());
				}
			}
		}

		@Override
		public void handle(HttpRequest request, HttpResponse response, HttpContext context)
				throws HttpException, IOException {
			
			try {
				String query = new URI(request.getRequestLine().getUri()).getQuery();
				if (query != null && !"".equals(query)) {
					StringTokenizer st = new StringTokenizer(query, "&");
					while (st.hasMoreTokens()) {
						receivedQuery.add(st.nextToken());
					}
				}
				
			} catch (URISyntaxException e) {
				throw new HttpException(e.getMessage());
			}
			
			response.setStatusCode(responseStatus);
		}
		
		public void isAssertSatisfied() {
			for (String item : expectedQuery) {
				Assert.assertTrue(receivedQuery.contains(item));
			}
		}
		
	}
}
