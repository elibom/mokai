package org.mokai.connector.smpp.test;

import ie.omk.smpp.Address;
import ie.omk.smpp.message.Bind;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.util.AlphabetEncoding;
import ie.omk.smpp.util.Latin1Encoding;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.gescobar.smppserver.PacketProcessor;
import net.gescobar.smppserver.SmppServer;
import net.gescobar.smppserver.SmppSession;

import org.mockito.Mockito;
import org.mokai.ConnectorContext;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.MessageProducer;
import org.mokai.Monitorable.Status;
import org.mokai.annotation.Resource;
import org.mokai.connector.smpp.SmppConfiguration;
import org.mokai.connector.smpp.SmppConfiguration.DlrIdConversion;
import org.mokai.connector.smpp.SmppConnector;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.RejectedException;
import org.mokai.persist.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SmppConnectorTest {

private Logger log = LoggerFactory.getLogger(SmppConnectorTest.class);
	
	private final long DEFAULT_TIMEOUT = 7000;
	
	private final int SERVER_PORT = 4444;
	
	private SmppServer server;
	
	@BeforeMethod
	public void startSimulator() throws Exception {
		server = new SmppServer(SERVER_PORT);
		
		// set a default packet processor
		server.setPacketProcessor(new PacketProcessor() {

			@Override
			public Response processPacket(SMPPPacket packet) {
				return Response.OK;
			}
			
		});
		
		server.start();
	}
	
	@AfterMethod
	public void stopSimulator() throws Exception {
		try {
			stopServer(server, 3000);
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
	
	private void stopServer(SmppServer server, long timeout) {
		
		server.stop();
		
		boolean stopped = false;
		long startTime = new Date().getTime();
		
		while (!stopped && (new Date().getTime() - startTime) < timeout) {
			if (server.getStatus().equals(SmppServer.Status.STOPPED)) {
				stopped = true;
			} else {
				try { Thread.sleep(200); } catch (InterruptedException e) {}
			}
		
		}
		
		Assert.assertEquals(server.getStatus(), SmppServer.Status.STOPPED);
	}
	
	@Test
	public void testSupport() throws Exception {
		SmppConnector connector = new SmppConnector();
		
		Assert.assertFalse(connector.supports(new Message()));
		Assert.assertFalse(connector.supports(new Message().setProperty("to", "")));
		Assert.assertTrue(connector.supports(new Message().setProperty("to", "3")));
		Assert.assertTrue(connector.supports(new Message().setProperty("to", "5730021111111111")));
		Assert.assertFalse(connector.supports(new Message().setProperty("to", "5730021111111111a")));
		Assert.assertFalse(connector.supports(new Message().setProperty("to", "5730021111111111.")));
		Assert.assertFalse(connector.supports(new Message().setProperty("to", "5730021111111111,")));
	}
	
	@Test
	public void testStatus() throws Exception {
		log.info("starting testStatus ... ");
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		SmppConnector connector = new SmppConnector(configuration);
		injectResource(new MockProcessorContext(), connector);
		Assert.assertEquals(connector.getStatus(), Status.UNKNOWN);
		
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		stopSimulator();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.FAILED);		
		
		startSimulator();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		Assert.assertEquals(connector.getStatus(), Status.OK);
		
		connector.doStop();
		
	}
	
	@Test
	public void testBindParameters() throws Exception {
		log.info("starting testBindParameters ... ");
		
		// set a packet processor that will validate parameters
		server.setPacketProcessor(new PacketProcessor() {
			
			boolean firstPacket = true;

			@Override
			public Response processPacket(SMPPPacket packet) {
				
				if (firstPacket) {
					Assert.assertEquals(packet.getCommandId(), SMPPPacket.BIND_TRANSCEIVER);
					
					Bind bind = (Bind) packet;
					Assert.assertEquals(bind.getSystemId(), "test");
					Assert.assertEquals(bind.getPassword(), "test");
					Assert.assertEquals(bind.getSystemType(), "test");
					Assert.assertEquals(bind.getAddressNpi(), 1);
					Assert.assertEquals(bind.getAddressTon(), 2);
					
					firstPacket = false;
				}
				
				return Response.OK;
			}
			
		});
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setSystemType("test");
		configuration.setBindNPI("1");
		configuration.setBindTON("2");
		
		SmppConnector connector = new SmppConnector(configuration);
		injectResource(new MockProcessorContext(), connector);
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		connector.doStop();
		
	}

	@Test
	public void testProcessMessage() throws Exception {
		log.info("starting testProcessMessage ... ");
		
		server.setPacketProcessor(new PacketProcessor() {

			@Override
			public Response processPacket(SMPPPacket packet) {
				
				if (packet.getCommandId() == SMPPPacket.SUBMIT_SM) {
					
					SubmitSM submitSM = (SubmitSM) packet;
					
					AlphabetEncoding enc = null;
					try {
						enc = new Latin1Encoding();
					} catch (UnsupportedEncodingException e) {
						Assert.fail(e.getMessage(), e);
					}
					
					Assert.assertEquals(submitSM.getDestination().getAddress(), "3002175604");
					Assert.assertEquals(submitSM.getSource().getAddress(), "3542");
					
					Assert.assertEquals(submitSM.getDataCoding(), 3);
					Assert.assertEquals(submitSM.getMessage(), enc.encodeString("This is the test with –"));
					
					Response response = Response.OK;
					response.setMessageId("12000");
					
					return response;
					
				}
				
				return Response.OK;
			}
			
		});
		
		MessageStore messageStore = Mockito.mock(MessageStore.class);
		
		Message m = new Message();
		m.setProperty("to", "3002175604");
		m.setProperty("from", "3542");
		
		Mockito.when(messageStore.list(Mockito.any(MessageCriteria.class)))
				.thenReturn(Collections.singletonList(m));
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setDataCoding(3);
		
		SmppConnector connector = new SmppConnector(configuration);
		injectResource(new MockProcessorContext(), connector);
		injectResource(messageStore, connector);
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		Message message = new Message();
		message.setProperty("to", "3002175604");
		message.setProperty("from", "3542");
		message.setProperty("text", "This is the test with –");
		
		connector.process(message);
		
		Assert.assertNotNull(message.getReference());
		
		Mockito.verify(messageStore, Mockito.timeout(1000)).saveOrUpdate(Mockito.any(Message.class));
		
		connector.doStop();
	}
	
	@Test
	public void testReceiveMessage() throws Exception {
		log.info("starting testReceiveMessage ... ");
		
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		SmppConnector connector = new SmppConnector(configuration);
		injectResource(new MockProcessorContext(), connector);
		injectResource(messageProducer, connector);
		connector.doStart();
		
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		String to = "3542";
		String from = "3002175604";
		String text = "this is a test";
		
		// retrieve the session
		Assert.assertEquals(server.getSessions().size(), 1);
		SmppSession session = server.getSessions().iterator().next();
		Assert.assertNotNull(session);
		
		// create and send the request
		DeliverSM deliverSM = new DeliverSM();
		deliverSM.setDestination(new Address(0, 0, to));
		deliverSM.setSource(new Address(0, 0, from));
		deliverSM.setMessageText(text);
		
		session.sendRequest(deliverSM);
		
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
	public void testReceiveDeliveryReceipt() throws Exception {
		log.info("starting testReceiveDeliveryReceipt ... ");
		
		server.setPacketProcessor(new CustomPacketProcessor("12000"));
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		MockMessageStore messageStore = new MockMessageStore();
		MockMessageProducer messageProducer = new MockMessageProducer();
		SmppConnector connector = createAndStartSmppConnector(configuration, messageStore, messageProducer);
		
		String from = "3542";
		String to = "3002175604";
		
		// send a message
		Message message = new Message();
		message.setProperty("to", to);
		message.setProperty("from", from);
		message.setProperty("text", "This is the test");
		message.setProperty("receiptDestination", "test");
		sendMessage(connector, messageStore, message);
		
		// retrieve the session
		Assert.assertEquals(server.getSessions().size(), 1);
		SmppSession session = server.getSessions().iterator().next();
		Assert.assertNotNull(session);
		
		DeliverSM deliverSM = new DeliverSM();
		deliverSM.setEsmClass(SMPPPacket.SMC_RECEIPT);
		deliverSM.setDestination(new Address(0, 0, from));
		deliverSM.setSource(new Address(0, 0, to));
		deliverSM.setMessageText("id:12000 sub:1 dlvrd:1 submit date:1101010000 done date:1101010000 stat:DELIVRD err:0 text:This is a ... ");
		
		session.sendRequest(deliverSM);
		
		long timeout = 2000;
		if (!receiveMessage(messageProducer, timeout)) {
			Assert.fail("the delivery receipt was not received");
		}
		
		Message receivedMessage = (Message) messageProducer.getMessage(0);
		Assert.assertEquals(from, receivedMessage.getProperty("to", String.class));
		Assert.assertEquals(to, receivedMessage.getProperty("from", String.class));
		Assert.assertEquals(receivedMessage.getProperty("messageId", String.class), 12000 + "");
		Assert.assertEquals("DELIVRD", receivedMessage.getProperty("finalStatus", String.class));
		
		connector.doStop();
		
	}
	
	@Test
	public void testReceiveDeliveryReceiptWithHexaId() throws Exception {
		log.info("starting testReceiveDeliveryReceiptWithHexaId ... ");
		
		server.setPacketProcessor(new CustomPacketProcessor("98765432101"));
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setDlrIdConversion(DlrIdConversion.HEXA_TO_DEC);
		
		MockMessageStore messageStore = new MockMessageStore();
		MockMessageProducer messageProducer = new MockMessageProducer();
		SmppConnector connector = createAndStartSmppConnector(configuration, messageStore, messageProducer);
		
		// send a message
		Message message = new Message();
		message.setProperty("to", "3542");
		message.setProperty("from", "3002175604");
		message.setProperty("text", "This is the test");
		message.setProperty("receiptDestination", "test");
		sendMessage(connector, messageStore, message);
		
		// retrieve the session
		Assert.assertEquals(server.getSessions().size(), 1);
		SmppSession session = server.getSessions().iterator().next();
		Assert.assertNotNull(session);
		
		DeliverSM deliverSM = new DeliverSM();
		deliverSM.setEsmClass(SMPPPacket.SMC_RECEIPT);
		deliverSM.setDestination(new Address(0, 0, "3002175604"));
		deliverSM.setSource(new Address(0, 0, "3542"));
		deliverSM.setMessageText("id:16fee0e525 sub:1 dlvrd:1 submit date:1101010000 done date:1101010000 stat:DELIVRD err:0 text:This is a ... ");
		
		session.sendRequest(deliverSM);
		
		long timeout = 2000;
		if (!receiveMessage(messageProducer, timeout)) {
			Assert.fail("the delivery receipt was not received");
		}
		
		Message receivedMessage = (Message) messageProducer.getMessage(0);
		Assert.assertNotNull(receivedMessage);
		
		connector.doStop();
		
	}
	
	@Test
	public void testReceiveDeliveryReceiptWithDecId() throws Exception {
		log.info("starting testReceiveDeliveryReceiptWithHexaId ... ");
		
		server.setPacketProcessor(new CustomPacketProcessor("16fee0e525"));
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setDlrIdConversion(DlrIdConversion.DEC_TO_HEXA);
		
		MockMessageStore messageStore = new MockMessageStore();
		MockMessageProducer messageProducer = new MockMessageProducer();
		SmppConnector connector = createAndStartSmppConnector(configuration, messageStore, messageProducer);
		
		// send a message
		Message message = new Message();
		message.setProperty("to", "3542");
		message.setProperty("from", "3002175604");
		message.setProperty("text", "This is the test");
		message.setProperty("receiptDestination", "test");
		sendMessage(connector, messageStore, message);
		
		// retrieve the session
		Assert.assertEquals(server.getSessions().size(), 1);
		SmppSession session = server.getSessions().iterator().next();
		Assert.assertNotNull(session);
		
		DeliverSM deliverSM = new DeliverSM();
		deliverSM.setEsmClass(SMPPPacket.SMC_RECEIPT);
		deliverSM.setDestination(new Address(0, 0, "3002175604"));
		deliverSM.setSource(new Address(0, 0, "3542"));
		deliverSM.setMessageText("id:98765432101 sub:1 dlvrd:1 submit date:1101010000 done date:1101010000 stat:DELIVRD err:0 text:This is a ... ");
		
		session.sendRequest(deliverSM);
		
		long timeout = 2000;
		if (!receiveMessage(messageProducer, timeout)) {
			Assert.fail("the delivery receipt was not received");
		}
		
		Message receivedMessage = (Message) messageProducer.getMessage(0);
		Assert.assertNotNull(receivedMessage);
		
		connector.doStop();
		
	}
	
	private SmppConnector createAndStartSmppConnector(SmppConfiguration configuration, MessageStore messageStore, MessageProducer messageProducer) throws Exception {
		
		SmppConnector connector = new SmppConnector(configuration);
		injectResource(new MockProcessorContext(), connector);
		
		if (messageStore == null) {
			messageStore = new MockMessageStore();
		}
		injectResource(messageStore, connector);
		
		if (messageProducer == null) {
			messageProducer = new MockMessageProducer();
		}
		injectResource(messageProducer, connector);
		
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		return connector;
	}
	
	class CustomPacketProcessor implements PacketProcessor {
		
		private String messageId;
		
		public CustomPacketProcessor(String messageId) {
			this.messageId = messageId;
		}

		@Override
		public Response processPacket(SMPPPacket packet) {
			if (packet.getCommandId() == SMPPPacket.SUBMIT_SM) {
				
				Response response = Response.OK;
				response.setMessageId(messageId);
				
				return response;
				
			}
			
			return Response.OK;
		}
		
	}
	
	private void sendMessage(SmppConnector connector, MessageStore messageStore, Message message) throws Exception {
		connector.process(message);
		messageStore.saveOrUpdate(message);
	}
	
	@Test
	public void shouldNotRouteDeliveryReceipt() throws Exception {
		
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		
		MessageStore messageStore = new MockMessageStore();
		
		SmppConnector connector = new SmppConnector(configuration);
		injectResource(new MockProcessorContext(), connector);
		injectResource(messageStore, connector);
		injectResource(messageProducer, connector);
		connector.doStart();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		Message message = new Message();
		message.setProperty("to", "3542");
		message.setProperty("from", "3002175604");
		message.setProperty("text", "This is the test");
		message.setProperty("sequenceNumber", 1);
		message.setProperty("messageId", "12000");
		message.setProperty("commandStatus", 0);
		
		messageStore.saveOrUpdate(message);
		
		DeliverSM deliverSM = new DeliverSM();
		deliverSM.setEsmClass(SMPPPacket.SMC_RECEIPT);
		deliverSM.setDestination(new Address(0, 0, "3002175604"));
		deliverSM.setSource(new Address(0, 0, "3542"));
		deliverSM.setMessageText("id:12000 sub:1 dlvrd:1 submit date:1101010000 done date:1101010000 stat:DELIVRD err:0 text:This is a ... ");
		
		// retrieve the session
		Assert.assertEquals(server.getSessions().size(), 1);
		SmppSession session = server.getSessions().iterator().next();
		Assert.assertNotNull(session);
		
		// send the delivery receipt
		session.sendRequest(deliverSM);
		
		long timeout = 2000;
		if (receiveMessage(messageProducer, timeout)) {
			Assert.fail("the message was received");
		}
	}
	
	@Test
	public void testFailedConnectionOnStart() throws Exception {
		log.info("starting testFailedConnectionOnStart ... ");
		
		stopSimulator();
		
		SmppConfiguration configuration = new SmppConfiguration();
		configuration.setHost("localhost");
		configuration.setPort(SERVER_PORT);
		configuration.setSystemId("test");
		configuration.setPassword("test");
		configuration.setInitialReconnectDelay(500);
		configuration.setReconnectDelay(500);
		
		SmppConnector connector = new SmppConnector(configuration);
		injectResource(new MockProcessorContext(), connector);
		connector.doStart();
		
		waitUntilStatus(connector, 3000, Status.FAILED);
		
		startSimulator();
		waitUntilStatus(connector, DEFAULT_TIMEOUT, Status.OK);
		
		connector.doStop();
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
	
	private void injectResource(Object resource, Object connector) throws Exception {
		
		Field[] fields = connector.getClass().getDeclaredFields();
		for (Field field : fields) {
			
			if (field.isAnnotationPresent(Resource.class) 
					&& field.getType().isInstance(resource)) {
				field.setAccessible(true);
				field.set(connector, resource);

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
	
	private class MockProcessorContext implements ConnectorContext {

		@Override
		public String getId() {
			return "test";
		}

		@Override
		public Direction getDirection() {
			return Direction.UNKNOWN;
		}
		
	}
	
	private class MockMessageStore implements MessageStore {
		
		private Collection<Message> messages = new ArrayList<Message>();

		@Override
		public void saveOrUpdate(Message message) throws StoreException, RejectedException {
			messages.add(message);
		}

		@Override
		public void updateStatus(MessageCriteria criteria, byte newStatus) throws StoreException {
			
		}

		@Override
		public Collection<Message> list(MessageCriteria criteria) throws StoreException {
			
			if (criteria != null && criteria.getProperties().get("smsc_messageid") != null) {
				
				Collection<Message> ret = new ArrayList<Message>();
				
				// check messageId
				String messageId = (String) criteria.getProperties().get("smsc_messageid");
				if (messageId != null) {
					
					for (Message message : messages) {
						String testId = message.getProperty("messageId", String.class);
						if (testId != null && messageId.equals(testId)) {
							ret.add(message);
						}
					}
					
				}
				
				return ret;
			}
			
			return messages;
		}
		
	}
	
}
