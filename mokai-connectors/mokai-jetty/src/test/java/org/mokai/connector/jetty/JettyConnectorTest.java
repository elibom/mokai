package org.mokai.connector.jetty;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.annotation.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JettyConnectorTest {

	@Test
	public void shouldProcessGetMessage() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try { 
			// create test HTTP call
			String to = "3002175604";
			String from = "3542";
			String originalText = "test · script @áÁ";
			String account = "flycast";
			String password = "test";
			String text = URLEncoder.encode(originalText, "UTF-8");
			
			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod("http://localhost:9080/?to=" 
					+ to + "&from=" + from + "&text=" + text + "&account=" + account
					+ "&password=" + password);
			getMethod.setRequestHeader("Content-Type", "text/html; charset=ISO-8859-1");
			int responseCode = client.executeMethod(getMethod);
			
			Assert.assertEquals(responseCode, 200);
			
			Assert.assertEquals(1, messageProducer.messageCount());
			
			Message message = messageProducer.getMessage(0);
			Assert.assertNotNull(message.getReference());
			Assert.assertEquals(message.getProperty("to", String.class), to);
			Assert.assertEquals(message.getProperty("from", String.class), from);
			Assert.assertEquals(message.getProperty("text", String.class), originalText);
			Assert.assertEquals(message.getProperty("account", String.class), account);
			Assert.assertEquals(message.getProperty("password", String.class), password);
			
		} finally {
			connector.doStop();
			connector.destroy();
		}
		
	}
	
	@Test
	public void shouldIgnoreWithInvalidContextUsingRoot() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try {
			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod("http://localhost:9080/test");
			int responseCode = client.executeMethod(getMethod);
			
			Assert.assertEquals(responseCode, 404);
			
		} finally {
			connector.doStop();
			connector.destroy();
		}
	}
	
	@Test
	public void shouldIgnoreWithInvalidContext() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		configuration.setContext("test");
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try {
			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod("http://localhost:9080/back");
			int responseCode = client.executeMethod(getMethod);
			
			Assert.assertEquals(responseCode, 404);
			
		} finally {
			connector.doStop();
			connector.destroy();
		}
	}
	
	@Test
	public void shouldProcessPostMessage() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		configuration.setContext("test");
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try {
			
			// create test HTTP call
			String to = "3002175604";
			String from = "3542";
			String originalText = "test · script @áÁ";
			String account = "flycast";
			String password = "test";
			String text = URLEncoder.encode(originalText, "ISO-8859-1");
			
			HttpClient client = new HttpClient();
			PostMethod postMethod = new PostMethod("http://localhost:9080/test");
			postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=ISO-8859-1");
			NameValuePair[] data = {
					new NameValuePair("to", to),
			        new NameValuePair("from", from),
			        new NameValuePair("text", text),
			        new NameValuePair("account", account),
			        new NameValuePair("password", password)
			};
			postMethod.setRequestBody(data);
			
			int responseCode = client.executeMethod(postMethod);
			
			Assert.assertEquals(responseCode, 200);
			
			Assert.assertEquals(1, messageProducer.messageCount());
			
			Message message = messageProducer.getMessage(0);
			Assert.assertNotNull(message.getReference());
			Assert.assertEquals(message.getProperty("to", String.class), to);
			Assert.assertEquals(message.getProperty("from", String.class), from);
			Assert.assertEquals(message.getProperty("text", String.class), originalText);
			Assert.assertEquals(message.getProperty("account", String.class), account);
			Assert.assertEquals(message.getProperty("password", String.class), password);
			
		} finally {
			connector.doStop();
			connector.destroy();
		}
		
	}
	
	@SuppressWarnings("restriction")
	@Test
	public void shouldProcessWithAthentication() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		configuration.setUseBasicAuth(true);
		configuration.addUser("admin", "password");
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try {
			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod("http://localhost:9080/");
			String userPassword = "admin:password";
			String basicAuth = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
			getMethod.addRequestHeader("Authorization", "Basic " + basicAuth);
			int responseCode = client.executeMethod(getMethod);
			
			Assert.assertEquals(responseCode, 200);
			
			Assert.assertEquals(1, messageProducer.messageCount());
			
			Message message = messageProducer.getMessage(0);
			Assert.assertNotNull(message);
			
		} finally {
			connector.doStop();
			connector.destroy();
		}
		
	}
	
	@SuppressWarnings("restriction")
	@Test
	public void shouldFailWithInvalidCredentials() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		configuration.setUseBasicAuth(true);
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try {
			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod("http://localhost:9080/");
			String userPassword = "wrong:credentials";
			String basicAuth = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
			getMethod.addRequestHeader("Authorization", "Basic " + basicAuth);
			int responseCode = client.executeMethod(getMethod);
			
			Assert.assertEquals(responseCode, 401);
			
		} finally {
			connector.doStop();
			connector.destroy();
		}
		
	}
	
	@Test
	public void shouldFailWithAthentication() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		configuration.setUseBasicAuth(true);
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try {
			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod("http://localhost:9080/");
			int responseCode = client.executeMethod(getMethod);
			
			Assert.assertEquals(responseCode, 401);
			
		} finally {
			connector.doStop();
			connector.destroy();
		}
		
	}
	
	@Test
	public void testMapper() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort(9080);
		configuration.setContext("test");
		
		Map<String,String> mapper = new HashMap<String,String>();
		mapper.put("to", "to1");
		mapper.put("from", "from1");
		mapper.put("text", "text1");
		configuration.setMapper(mapper);
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure();
		connector.doStart();
		
		try {
			// create test HTTP call
			String to = "3002175604";
			String from = "3542";
			String text = "test";
			
			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod("http://localhost:9080/test?to=" 
					+ to + "&from=" + from + "&text=" + text);
			client.executeMethod(getMethod);
			
			Assert.assertEquals(1, messageProducer.messageCount());
			
			Message message = (Message) messageProducer.getMessage(0);
			Assert.assertEquals(to, message.getProperty("to1", String.class));
			Assert.assertEquals(from, message.getProperty("from1", String.class));
			Assert.assertEquals(text, message.getProperty("text1", String.class));
		
		} finally {
			
			connector.doStop();
			connector.destroy();
			
		}
		
	}
	
	private void addMessageProducer(MessageProducer messageProducer, Object connector) throws Exception {
		
		Field[] fields = connector.getClass().getDeclaredFields();
		for (Field field : fields) {
			
			if (field.isAnnotationPresent(Resource.class) 
					&& field.getType().isInstance(messageProducer)) {
				field.setAccessible(true);
				field.set(connector, messageProducer);

			}
		}
	}
	
	private class MockMessageProducer implements MessageProducer {
		
		private List<Message> messages = new ArrayList<Message>();

		@Override
		public void produce(Message message) throws IllegalArgumentException,
				ExecutionException {
			messages.add(message);
		}
		
		public int messageCount() {
			return messages.size();
		}
		
		public Message getMessage(int index) {
			return messages.get(index);
		}
		
	}
	
}
