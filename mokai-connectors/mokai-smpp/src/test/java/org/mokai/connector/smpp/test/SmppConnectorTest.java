package org.mokai.connector.smpp.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mokai.connector.smpp.SmppConfiguration;
import org.mokai.connector.smpp.SmppConnector;
import org.mokai.spi.ExecutionException;
import org.mokai.spi.Message;
import org.mokai.spi.MessageProducer;
import org.mokai.spi.annotation.Resource;
import org.mokai.spi.message.SmsMessage;
import org.smpp.smscsim.Simulator;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SmppConnectorTest {
	
	private Simulator simulator;
	
	@BeforeMethod
	public void startSimulator() throws Exception {
		simulator = new Simulator();
		simulator.start(8321);
		simulator.addUser("test", "test");
	}
	
	@AfterMethod
	public void stopSimulator() throws Exception {
		simulator.stop();
	}

	@Test
	public void testProcessMessage() throws Exception {
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		SmppConnector connector = new SmppConnector(configuration);
		connector.doStart();
		
		SmsMessage message = new SmsMessage();
		message.setTo("3002175604");
		message.setFrom("3542");
		message.setText("This is the test");
		
		connector.process(message);
		
		Assert.assertNotNull(message.getReference());
		
		connector.doStop();
	}
	
	@Test
	public void testReceiveMessage() throws Exception {
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		SmppConnector connector = new SmppConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.doStart();
		
		String to = "3542";
		String from = "3002175604";
		String text = "this is a test";
		
		simulator.simulateMOMessage("test", to, from, text);
		
		long timeout = 2000;
		if (!receiveMessage(messageProducer, timeout)) {
			Assert.fail("the message was not received");
		}
		
		SmsMessage message = (SmsMessage) messageProducer.getMessage(0);
		Assert.assertEquals(to, message.getTo());
		Assert.assertEquals(from, message.getFrom());
		Assert.assertEquals(text, message.getText());
		
		connector.doStop();
	}
	
	private boolean receiveMessage(MockMessageProducer messageProducer, long timeout) {
		boolean received = false;
		
		long startTime = new Date().getTime();
		long actualTime = new Date().getTime();
		while (!received && (actualTime - startTime) <= timeout) {
			if (messageProducer.messageCount() > 0) {
				received = true;
			} else {
				synchronized (this) {
					try { this.wait(100); } catch (Exception e) {}
				}	
			}
		}
		
		return received;
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
