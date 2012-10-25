package org.mokai.connector.smpp.test;

import ie.omk.smpp.Address;
import ie.omk.smpp.AlreadyBoundException;
import ie.omk.smpp.Connection;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.InvalidParameterValueException;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPProtocolException;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.net.TcpLink;
import ie.omk.smpp.util.APIConfig;
import ie.omk.smpp.version.VersionException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.gescobar.smppserver.Response;

import org.mockito.Mockito;
import org.mokai.ConnectorContext;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.annotation.Resource;
import org.mokai.connector.smpp.SmppServerConfiguration;
import org.mokai.connector.smpp.SmppServerConnector;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmppServerConnectorTest {
	
	@Test
	public void shouldBindAndProduceMessage() throws Exception {
		
		MockMessageProducer messageProducer = new MockMessageProducer();
		
		SmppServerConfiguration configuration = new SmppServerConfiguration();
		configuration.addUser("test", "test");
		
		SmppServerConnector connector = new SmppServerConnector(configuration);
		injectResource(messageProducer, connector);
		injectResource(buildConnectorContext("test"), connector);
		connector.configure();
		connector.doStart();
		
		// open connection and bind
		Connection connection = connect(4444);
		bind(connection, Connection.TRANSMITTER, "test", "test", null);
		
		SubmitSM submitSM = (SubmitSM) connection.newInstance(SMPPPacket.SUBMIT_SM);
		submitSM.setDestination(new Address(0, 0, "573001111111"));
		submitSM.setSource(new Address(0, 0, "3542"));
		submitSM.setMessageText("This is a test");
		
		SubmitSMResp response = (SubmitSMResp) connection.sendRequest(submitSM);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.getMessageId() != null);
		
		Assert.assertEquals(messageProducer.messageCount(), 1);
		Message message = messageProducer.getMessage(0);
		Assert.assertNotNull(message);
		Assert.assertEquals(message.getProperty("to", String.class), "573001111111");
		Assert.assertEquals(message.getProperty("from", String.class), "3542");
		Assert.assertEquals(message.getProperty("text", String.class), "This is a test");
		
		connection.unbind();
		connection.closeLink();
		
		connector.doStop();
		
	}
	
	@Test
	public void shouldFailToBindIfBadSystemId() throws Exception {
		
		SmppServerConnector connector = new SmppServerConnector();
		connector.configure();
		connector.doStart();
		
		// open connection and bind
		Connection connection = connect(4444);
		
		BindResp bindResp = connection.bind(Connection.TRANSMITTER, "test", "test", null);
		Assert.assertNotNull(bindResp);
		Assert.assertEquals(bindResp.getCommandStatus(), Response.INVALID_SYSTEM_ID.getCommandStatus());
		
		connector.doStop();
	}
	
	@Test
	public void shouldFailToBindIfBadPassword() throws Exception {
		
		SmppServerConfiguration configuration = new SmppServerConfiguration();
		configuration.addUser("test", "test");
		
		SmppServerConnector connector = new SmppServerConnector(configuration);
		connector.configure();
		connector.doStart();
		
		Connection connection = connect(4444);
		BindResp bindResp = connection.bind(Connection.TRANSMITTER, "test", "other", null);
		Assert.assertNotNull(bindResp);
		Assert.assertEquals(bindResp.getCommandStatus(), Response.INVALID_PASSWORD.getCommandStatus());		
		
		connector.doStop();
		
	}
	
	@Test
	public void shouldProduceDeliverSm() throws Exception {
		
		SmppServerConfiguration configuration = new SmppServerConfiguration();
		configuration.addUser("test", "test");
		
		final SmppServerConnector connector = new SmppServerConnector(configuration);
		injectResource(buildConnectorContext("test"), connector);
		connector.configure();
		connector.doStart();
		
		// open connection and bind
		Connection connection = connect(4444);
		bind(connection, Connection.TRANSMITTER, "test", "test", null);
		
		// send deliver_sm
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message message = new Message();
				message.setProperty("to", "1111");
				message.setProperty("from", "2222");
				message.setProperty("text", "This is a test");
				
				try {
					connector.process(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		
		SMPPPacket packet = connection.readNextPacket();
		Assert.assertNotNull(packet);
		Assert.assertEquals(packet.getCommandId(), SMPPPacket.DELIVER_SM);
		
		DeliverSM deliverSm = (DeliverSM) packet;
		Assert.assertEquals(deliverSm.getEsmClass(), (byte) 0); 
		Assert.assertEquals(deliverSm.getSource().getAddress(), "2222");
		Assert.assertEquals(deliverSm.getDestination().getAddress(), "1111");
		Assert.assertEquals(deliverSm.getMessageText(), "This is a test");
		
		connector.doStop();
		
	}
	
	@Test
	public void shouldProduceDeliveryReceipt() throws Exception {
		
		SmppServerConfiguration configuration = new SmppServerConfiguration();
		configuration.addUser("test", "test");
		
		final SmppServerConnector connector = new SmppServerConnector(configuration);
		injectResource(buildConnectorContext("test"), connector);
		connector.configure();
		connector.doStart();
		
		// open connection and bind
		Connection connection = connect(4444);
		bind(connection, Connection.TRANSMITTER, "test", "test", null);
		
		final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddhhmm");
		final Date submitDate = sdf.parse("1201011023");
		final Date doneDate = sdf.parse("1201011048");
		
		// send deliver_sm
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message message = new Message();
				
				message.setProperty("isDLR", true);
				message.setProperty("to", "1111");
				message.setProperty("from", "2222");
				message.setProperty("messageId", "12345");
				message.setProperty("submitted", 1);
				message.setProperty("submitDate", submitDate);
				message.setProperty("delivered", 1);
				message.setProperty("doneDate", doneDate);
				message.setProperty("finalStatus", "DELIVRD");
				
				try {
					connector.process(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		
		SMPPPacket packet = connection.readNextPacket();
		Assert.assertNotNull(packet);
		Assert.assertEquals(packet.getCommandId(), SMPPPacket.DELIVER_SM);
		
		DeliverSM deliverSm = (DeliverSM) packet;
		Assert.assertEquals(deliverSm.getEsmClass(), (byte) 0x04); 
		Assert.assertEquals(deliverSm.getSource().getAddress(), "2222");
		Assert.assertEquals(deliverSm.getDestination().getAddress(), "1111");
		Assert.assertEquals(deliverSm.getMessageText(), "id:12345 sub:001 dlvrd:001 submit date:" + sdf.format(submitDate) + " done date:" + sdf.format(doneDate) + " stat:DELIVRD err:0 text:                    ");
		
	}
	
	private Connection connect(int port) throws UnknownHostException {
		
		APIConfig.getInstance().setProperty(APIConfig.BIND_TIMEOUT, 3000 + "");
		APIConfig.getInstance().setProperty(APIConfig.LINK_TIMEOUT, 3000 + "");
		
		TcpLink link = new TcpLink("localhost", port);
		Connection connection = new Connection(link, false);
		connection.autoAckLink(true);
		connection.autoAckMessages(true);
		
		return connection;
	}
	
	private void bind(Connection connection, int type, String systemId, String password, String systemType) throws InvalidParameterValueException, AlreadyBoundException, VersionException, SMPPProtocolException, IllegalArgumentException, IOException {
		
		BindResp bindResp = connection.bind(Connection.TRANSMITTER, "test", "test", null);
		
		Assert.assertNotNull(bindResp);
		Assert.assertEquals(bindResp.getCommandStatus(), 0);
		
	}
	
	private ConnectorContext buildConnectorContext(String id) {
		
		ConnectorContext connectorContext = Mockito.mock(ConnectorContext.class);
		Mockito.when(connectorContext.getId()).thenReturn(id);
		
		return connectorContext;
		
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
	
}
