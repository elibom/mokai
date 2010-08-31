package org.mokai.connector.smpp.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Monitorable.Status;
import org.mokai.annotation.Resource;
import org.mokai.connector.smpp.SmppConfiguration;
import org.mokai.connector.smpp.SmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpp.smscsim.Simulator;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SmppConnectorTest {
	
	private Logger log = LoggerFactory.getLogger(SmppConnectorTest.class);
	
	private final long DEFAULT_TIMEOUT = 3000;
	
	private Simulator simulator;
	
	@BeforeMethod
	public void startSimulator() throws Exception {
		simulator = new Simulator();
		simulator.start(8321);
		simulator.addUser("test", "test");
	}
	
	@AfterMethod
	public void stopSimulator() throws Exception {
		try {
			simulator.stop();
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
	
	@Test
	public void testStatus() throws Exception {
		log.info("starting testStatus ... ");
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setInitialReconnectDelay(500);
		configuration.setReconnectDelay(500);
		
		SmppConnector connector = new SmppConnector(configuration);
		Assert.assertEquals(connector.getStatus(), Status.UNKNOWN);
		
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		stopSimulator();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.FAILED);		
		
		startSimulator();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		Assert.assertEquals(connector.getStatus(), Status.OK);
		
	}

	@Test
	public void testProcessMessage() throws Exception {
		log.info("starting testProcessMessage ... ");
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		SmppConnector connector = new SmppConnector(configuration);
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		Message message = new Message();
		message.setProperty("to", "3002175604");
		message.setProperty("from", "3542");
		message.setProperty("text", "This is the test");
		
		connector.process(message);
		
		Assert.assertNotNull(message.getReference());
		
		connector.doStop();
	}
	
	@Test
	public void testProcessMessageNullText() throws Exception {
		log.info("starting testProcessMessageNullText ... ");
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		SmppConnector connector = new SmppConnector(configuration);
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		Message message = new Message();
		message.setProperty("to1", "3002175604");
		message.setProperty("from1", "3542");
		message.setProperty("message", "This is the test");
		
		connector.process(message);
		
		connector.doStop();
	}
	
	@Test
	public void testBindNPITON() throws Exception {
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setSourceTON("3");
		configuration.setSourceNPI("10");
		
		SmppConnector connector = new SmppConnector(configuration);
		connector.doStart();
		
		connector.doStop();
	}
	
	@Test
	public void testReceiveMessage() throws Exception {
		log.info("starting testReceiveMessage ... ");
		
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		SmppConnector connector = new SmppConnector(configuration);
		addMessageProducer(messageProducer, connector);
		connector.doStart();
		
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		String to = "3542";
		String from = "3002175604";
		String text = "this is a test";
		
		simulator.simulateMOMessage("test", to, from, text);
		
		long timeout = 2000;
		if (!receiveMessage(messageProducer, timeout)) {
			Assert.fail("the message was not received");
		}
		
		Message message = (Message) messageProducer.getMessage(0);
		Assert.assertEquals(to, message.getProperty("to", String.class));
		Assert.assertEquals(from, message.getProperty("from", String.class));
		Assert.assertEquals(text, message.getProperty("text", String.class));
		
		connector.doStop();
	}
	
	@Test
	public void testFailedConnectionOnStart() throws Exception {
		log.info("starting testFailedConnectionOnStart ... ");
		
		stopSimulator();
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(8321);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setInitialReconnectDelay(500);
		configuration.setReconnectDelay(500);
		
		SmppConnector connector = new SmppConnector(configuration);
		connector.doStart();
		
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.FAILED);
		
		startSimulator();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
	}
	
	private void waitUntilStatus(SmppConnector connector, long timeout, Status status) {
		boolean isValid = false;
		
		long startTime = new Date().getTime();
		long actualTime = new Date().getTime();
		while (!isValid && (actualTime - startTime) <= timeout) {
			if (connector.getStatus() == status) {
				isValid = true;
			} else {
				synchronized (this) {
					try { this.wait(200); } catch (Exception e) {}
				}	
			}
			
			actualTime = new Date().getTime();
		}
		
		Assert.assertEquals(connector.getStatus(), status);
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
					try { this.wait(200); } catch (Exception e) {}
				}	
			}
			
			actualTime = new Date().getTime();
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
