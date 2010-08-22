package org.mokai.connector.camel.jetty.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.annotation.Resource;
import org.mokai.connector.camel.jetty.JettyConfiguration;
import org.mokai.connector.camel.jetty.JettyConnector;
import org.mokai.message.SmsMessage;
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
		String text = "test";
		
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod("http://localhost:9080/test?to=" 
				+ to + "&from=" + from + "&message=" + text);
		client.executeMethod(getMethod);
		
		Assert.assertEquals(1, messageProducer.messageCount());
		
		SmsMessage message = (SmsMessage) messageProducer.getMessage(0);
		Assert.assertEquals(to, message.getTo());
		Assert.assertEquals(from, message.getFrom());
		Assert.assertEquals(text, message.getText());
		
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
