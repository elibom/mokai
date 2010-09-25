package org.mokai.connector.camel.jetty.test;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.annotation.Resource;
import org.mokai.connector.camel.jetty.JettyConfiguration;
import org.mokai.connector.camel.jetty.JettyConnector;
import org.testng.annotations.Test;

public class JettyConnectorTest {

	@Test
	public void testMessageFlow() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort("9080");
		configuration.setContext("test");
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure(); // the jetty server starts
		
		// create test HTTP call
		String to = "3002175604";
		String from = "3542";
		String originalText = "test á script @";
		String text = URLEncoder.encode(originalText, "UTF-8");
		
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod("http://localhost:9080/test?to=" 
				+ to + "&from=" + from + "&text=" + text);
		client.executeMethod(getMethod);
		
		Assert.assertEquals(1, messageProducer.messageCount());
		
		Message message = (Message) messageProducer.getMessage(0);
		Assert.assertEquals(Message.SMS_TYPE, message.getType());
		Assert.assertNotNull(message.getReference());
		Assert.assertEquals(to, message.getProperty("to", String.class));
		Assert.assertEquals(from, message.getProperty("from", String.class));
		Assert.assertEquals(originalText, message.getProperty("text", String.class));
		
		connector.destroy();
		
	}
	
	@Test
	public void testMapper() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		JettyConfiguration configuration = new JettyConfiguration();
		configuration.setPort("9080");
		configuration.setContext("test");
		
		Map<String,String> mapper = new HashMap<String,String>();
		mapper.put("to", "to1");
		mapper.put("from", "from1");
		mapper.put("text", "text1");
		configuration.setMapper(mapper);
		
		JettyConnector connector = new JettyConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.configure(); // the jetty server starts
		
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
		Assert.assertEquals(Message.SMS_TYPE, message.getType());
		Assert.assertEquals(to, message.getProperty("to1", String.class));
		Assert.assertEquals(from, message.getProperty("from1", String.class));
		Assert.assertEquals(text, message.getProperty("text1", String.class));
		
		connector.destroy();
		
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
