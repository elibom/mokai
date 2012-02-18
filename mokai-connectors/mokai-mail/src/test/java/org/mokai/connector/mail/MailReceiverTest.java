package org.mokai.connector.mail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mokai.ConnectorContext;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.MessageProducer;
import org.mokai.Monitorable.Status;
import org.mokai.annotation.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

/**
 * 
 * @author German Escobar
 */
public class MailReceiverTest {

	@Test
	public void shouldReceiveImapEmail() throws Exception {
		
		GreenMail greenMail = new GreenMail(ServerSetupTest.ALL);
	    greenMail.start();
	    
	    try { 
	    	
		    //use random content to avoid potential residual lingering problems
		    String subject = GreenMailUtil.random();
		    String body = GreenMailUtil.random();
		    GreenMailUtil.sendTextEmailTest("test@localhost.com", "from@localhost.com", subject, body);
		    
		    Assert.assertTrue( greenMail.waitForIncomingEmail(5000, 1) ); 
		    
		    MockMessageProducer messageProducer = new MockMessageProducer();
		    
		    MailReceiverConfig configuration = new MailReceiverConfig();
		    configuration.setPort(3143);
		    configuration.setInterval(1);
		    configuration.setUsername("test@localhost.com");
		    configuration.setPassword("test@localhost.com");
		    
		    MailReceiver receiver = new MailReceiver(configuration);
		    injectResource(new MockConnectorContext(), receiver);
		    injectResource(messageProducer, receiver);
		    receiver.doStart();
		    
		    // wait max 5 secs for message to be received
		    waitUntilMessageIsReceived(messageProducer, 5000);
		    
		    Assert.assertEquals(messageProducer.messageCount(), 1);
		    
		    receiver.doStop();
		    
		    Message message = messageProducer.getMessage(0);
		    Assert.assertNotNull(message);
		    Assert.assertEquals(message.getProperty("recipients", String.class), "test@localhost.com");
		    Assert.assertEquals(message.getProperty("to", String.class), "test@localhost.com");
		    Assert.assertEquals(message.getProperty("cc", String.class), "");
		    Assert.assertEquals(message.getProperty("bcc", String.class), "");
		    Assert.assertEquals(message.getProperty("from", String.class), "from@localhost.com");
		    Assert.assertEquals(message.getProperty("subject", String.class), subject);
		    Assert.assertEquals(message.getProperty("text", String.class), body);
		    
		    // check that email is still there
		    Assert.assertEquals(greenMail.getReceivedMessages().length, 1);
		    /*MimeMessage m = greenMail.getReceivedMessages()[0];
		    Assert.assertNotNull(m);
		    Assert.assertTrue(m.getFlags().contains(Flags.Flag.SEEN));*/
		    
	    } finally {
		    greenMail.stop();
	    }
	    
	}
	
	@Test
	public void shouldReceivePop3AndDeleteEmail() throws Exception {
		
		GreenMail greenMail = new GreenMail(ServerSetupTest.ALL);
	    greenMail.start();
	    
	    try { 
	    	
		    //use random content to avoid potential residual lingering problems
		    String subject = GreenMailUtil.random();
		    String body = GreenMailUtil.random();
		    GreenMailUtil.sendTextEmailTest("test@localhost.com", "from@localhost.com", subject, body);
		    
		    Assert.assertTrue( greenMail.waitForIncomingEmail(5000, 1) ); 
		    
		    MockMessageProducer messageProducer = new MockMessageProducer();
		    
		    MailReceiverConfig configuration = new MailReceiverConfig();
		    configuration.setProtocol("pop3");
		    configuration.setPort(3110);
		    configuration.setInterval(1);
		    configuration.setUsername("test@localhost.com");
		    configuration.setPassword("test@localhost.com");
		    configuration.setDelete(true);
		    
		    MailReceiver receiver = new MailReceiver(configuration);
		    injectResource(new MockConnectorContext(), receiver);
		    injectResource(messageProducer, receiver);
		    receiver.doStart();
		    
		    // wait max 5 secs for message to be received
		    waitUntilMessageIsReceived(messageProducer, 5000);
		    
		    Assert.assertEquals(messageProducer.messageCount(), 1);
		    
		    receiver.doStop();
		    
		    // check that email is not there
		    Assert.assertEquals(greenMail.getReceivedMessages().length, 0);
		    
	    } finally {
		    greenMail.stop();
	    }
	    
	}
	
	@Test
	public void shouldSetupEmailToSms() throws Exception {
		
		GreenMail greenMail = new GreenMail(ServerSetupTest.ALL);
	    greenMail.start();
	    
	    try { 
	    	
		    //use random content to avoid potential residual lingering problems
		    String subject = GreenMailUtil.random();
		    String body = GreenMailUtil.random();
		    GreenMailUtil.sendTextEmailTest("test@localhost.com", "from@localhost.com", subject, body);
		    
		    Assert.assertTrue( greenMail.waitForIncomingEmail(5000, 1) ); 
		    
		    MockMessageProducer messageProducer = new MockMessageProducer();
		    
		    MailReceiverConfig configuration = new MailReceiverConfig();
		    configuration.setPort(3143);
		    configuration.setInterval(1);
		    configuration.setUsername("test@localhost.com");
		    configuration.setPassword("test@localhost.com");
		    configuration.setEmailToSms(true);
		    
		    MailReceiver receiver = new MailReceiver(configuration);
		    injectResource(new MockConnectorContext(), receiver);
		    injectResource(messageProducer, receiver);
		    receiver.doStart();
		    
		    // wait max 5 secs for message to be received
		    waitUntilMessageIsReceived(messageProducer, 5000);
		    
		    Assert.assertEquals(messageProducer.messageCount(), 1);
		    
		    receiver.doStop();
		    
		    Message message = messageProducer.getMessage(0);
		    Assert.assertNotNull(message);
		    Assert.assertEquals(message.getProperty("recipients", String.class), "test@localhost.com");
		    Assert.assertEquals(message.getProperty("to", String.class), subject);
		    Assert.assertEquals(message.getProperty("emailTo", String.class), "test@localhost.com");
		    Assert.assertEquals(message.getProperty("cc", String.class), "");
		    Assert.assertEquals(message.getProperty("bcc", String.class), "");
		    Assert.assertEquals(message.getProperty("from", String.class), "12345");
		    Assert.assertEquals(message.getProperty("emailFrom", String.class), "from@localhost.com");
		    Assert.assertEquals(message.getProperty("subject", String.class), subject);
		    Assert.assertEquals(message.getProperty("text", String.class), body);
		    
	    } finally {
		    greenMail.stop();
	    }
		
	}
	
	@Test
	public void shouldTimeoutIfInvalidPort() throws Exception {
		
		GreenMail greenMail = new GreenMail();
	    greenMail.start();
	    
	    MailReceiverConfig configuration = new MailReceiverConfig();
	    configuration.setPort(3996);
	    configuration.setInterval(1);
	    
	    MailReceiver receiver = new MailReceiver(configuration);
	    injectResource(new MockConnectorContext(), receiver);
	    injectResource(new MockMessageProducer(), receiver);
	    receiver.doStart();
	    
	    waitUntilStatus(receiver, 20000, Status.FAILED);
	    
	    receiver.doStop();
	    greenMail.stop();
	}
	
	private void waitUntilStatus(MailReceiver connector, long timeout, Status status) {
		
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
	
	private void waitUntilMessageIsReceived(MockMessageProducer messageProducer, long timeout) {
		
		long startTime = System.currentTimeMillis();
		long actualTime = System.currentTimeMillis();
		
		while (messageProducer.messageCount() == 0 && (actualTime - startTime) <= timeout) {
	    	try { Thread.sleep(500); } catch (Exception e) {}
	    	
	    	actualTime = System.currentTimeMillis();
	    }
		
		// wait 500 millis more just in case
		if ( (actualTime - startTime) <= timeout ) {
			try { Thread.sleep(500); } catch (Exception e) {}
		}
		
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
	
	private class MockConnectorContext implements ConnectorContext {

		@Override
		public String getId() {
			return "test";
		}

		@Override
		public Direction getDirection() {
			return Direction.UNKNOWN;
		}
		
	}
	
}
