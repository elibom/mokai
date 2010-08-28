package org.mokai.impl.camel.test;

import java.util.List;

import junit.framework.Assert;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.mockito.Mockito;
import org.mokai.Action;
import org.mokai.Configurable;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Receiver;
import org.mokai.Service;
import org.mokai.Serviceable;
import org.mokai.annotation.Resource;
import org.mokai.impl.camel.CamelReceiverService;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class CamelReceiverServiceTest extends CamelBaseTest {
	
	@Test
	public void testSingleMessage() throws Exception {
		
		// validation route
		addValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);
		resultEndpoint.message(0).body().isInstanceOf(Message.class);
		
		// create receiver
		SimpleReceiver receiver = new SimpleReceiver();
		new CamelReceiverService("test", receiver, camelContext);
		
		// send a message
		receiver.receiveMessage(new Message());
		
		// validate results
		resultEndpoint.assertIsSatisfied();
		
		Exchange exchange = resultEndpoint.getReceivedExchanges().iterator().next();
		Message message = exchange.getIn().getBody(Message.class);
		
		Assert.assertNotNull(message.getReference());
		Assert.assertEquals("test", message.getSource());
		Assert.assertEquals(Message.SourceType.RECEIVER, message.getSourceType());
		Assert.assertEquals(Message.ANONYMOUS_ACCOUNT_ID, message.getAccountId());
		Assert.assertEquals(Message.Flow.OUTBOUND, message.getFlow());
	}
	
	@Test
	public void testPostReceivingActions() throws Exception {
		
		// validation route
		addValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);

		SimpleReceiver receiver = new SimpleReceiver();
		CamelReceiverService receiverService = new CamelReceiverService("test", receiver, camelContext);
		
		// add a post-receiving action
		MockAction postReceivingAction = new MockAction();
		receiverService.addPostReceivingAction(postReceivingAction);
		
		// add another post-receiving action that changes the message
		receiverService.addPostReceivingAction(new Action() {

			@Override
			public void execute(Message message) throws Exception {
				message.setAccountId("germanescobar");
			}
			
		});
		
		// simulate we receive a message
		receiver.receiveMessage(new Message());
		
		// validate results
		resultEndpoint.assertIsSatisfied();
		Assert.assertEquals(1, postReceivingAction.getChanged());
		
		Exchange exchange = resultEndpoint.getReceivedExchanges().iterator().next();
		Message message = exchange.getIn().getBody(Message.class);
		Assert.assertEquals("germanescobar", message.getAccountId());
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullCamelContext() {
		SimpleReceiver receiver = new SimpleReceiver();
		new CamelReceiverService("test", receiver, null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullConnector() throws Exception {
		new CamelReceiverService("test", null, camelContext);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullId() throws Exception {
		new CamelReceiverService(null, new SimpleReceiver(), camelContext);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithEmptyId() throws Exception {
		new CamelReceiverService("", new SimpleReceiver(), camelContext);
	}
	
	@Test
	public void testIdWithSpaces() throws Exception {
		CamelReceiverService receiverService = new CamelReceiverService("T e s T", new SimpleReceiver(), camelContext);
		Assert.assertEquals("test", receiverService.getId());
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullAction() throws Exception {
		CamelReceiverService receiverService = new CamelReceiverService("test", new SimpleReceiver(), camelContext);
		receiverService.addPostReceivingAction(null);
	}
	
	@Test
	public void testAddRemoveActions() throws Exception {
		CamelReceiverService receiverService = new CamelReceiverService("test", new SimpleReceiver(), camelContext);
		
		Action action1 = new MockAction();
		Action action2 = new MockAction();
		
		// clean validations
		Assert.assertEquals(0, receiverService.getPostReceivingActions().size());
		
		// add first action
		receiverService.addPostReceivingAction(action1);
		Assert.assertEquals(1, receiverService.getPostReceivingActions().size());
		
		// add second action
		receiverService.addPostReceivingAction(action2);
		Assert.assertEquals(2, receiverService.getPostReceivingActions().size());
		
		List<Action> postReceivingActions  = receiverService.getPostReceivingActions();
		Assert.assertEquals(postReceivingActions.get(0), action1);
		Assert.assertEquals(postReceivingActions.get(1), action2);
		
		// remove action 1
		receiverService.removePostReceivingAction(action1);
		
		Assert.assertEquals(1, receiverService.getPostReceivingActions().size());
		postReceivingActions  = receiverService.getPostReceivingActions();
		Assert.assertEquals(postReceivingActions.get(0), action2);
		
	}
	
	@Test(expectedExceptions=ExecutionException.class)
	public void testActionException() throws Exception {
		SimpleReceiver receiver = new SimpleReceiver();
		CamelReceiverService receiverService = new CamelReceiverService("test", receiver, camelContext);
		
		Message message = new Message();
		
		Action action = Mockito.mock(Action.class);
		Mockito.doThrow(new NullPointerException()).when(action).execute(message);
		
		receiverService.addPostReceivingAction(action);
		
		receiver.receiveMessage(message);
	}
	
	@Test
	public void testNonServiceableConnector() {
		CamelReceiverService receiverService = 
			new CamelReceiverService("test", new SimpleReceiver(), camelContext);
		
		// test start
		receiverService.start();
		Assert.assertEquals(Service.State.STARTED, receiverService.getState());
		
		// test stop
		receiverService.stop();
		Assert.assertEquals(Service.State.STOPPED, receiverService.getState());
	}
	
	@Test
	public void testServiceableConnector() throws Exception {
		Receiver receiver = Mockito.mock(Receiver.class, Mockito.withSettings().extraInterfaces(Serviceable.class));
		CamelReceiverService receiverService = new CamelReceiverService("test", receiver, camelContext);
		
		// test start
		receiverService.start();
		Mockito.verify((Serviceable) receiver).doStart();
		
		// test stop
		receiverService.stop();
		Mockito.verify((Serviceable) receiver).doStop();
	}
	
	@Test(expectedExceptions=ExecutionException.class)
	public void shoudFailExceptionOnConnectorStart() throws Exception {
		Receiver receiver = Mockito.mock(Receiver.class, Mockito.withSettings().extraInterfaces(Serviceable.class));
		Mockito.doThrow(new NullPointerException()).when((Serviceable) receiver).doStart();
		
		CamelReceiverService receiverService = new CamelReceiverService("test", receiver, camelContext);
		receiverService.start();
	}
	
	@Test(expectedExceptions=ExecutionException.class)
	public void shouldFailExceptionOnConnectorStop() throws Exception {
		Receiver receiver = Mockito.mock(Receiver.class, Mockito.withSettings().extraInterfaces(Serviceable.class));
		Mockito.doThrow(new NullPointerException()).when((Serviceable) receiver).doStop();
		
		CamelReceiverService receiverService = new CamelReceiverService("test", receiver, camelContext);
		receiverService.start();
		receiverService.stop();
	}
	
	@Test
	public void testConfigurableConnector() throws Exception {
		Receiver receiver = Mockito.mock(Receiver.class, Mockito.withSettings().extraInterfaces(Configurable.class));
		CamelReceiverService receiverService = new CamelReceiverService("test", receiver, camelContext);
		
		// verify
		Mockito.verify((Configurable) receiver).configure();
		
		receiverService.destroy();
		
		// verify
		Mockito.verify((Configurable) receiver).destroy();
	}
	
	/**
	 * Helper method to create the route that validates the output of the receivers.
	 * @throws Exception
	 */
	private void addValidationRoute() throws Exception {
		camelContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("activemq:outboundRouter").to("mock:validate");
			}
			
		});
	}
	
	/**
	 * Simple receiver that exposes a sendMessage method to simulate messages.
	 * 
	 * @author German Escobar
	 */
	protected class SimpleReceiver implements Receiver {
		
		@Resource
		private MessageProducer messageProducer;
		
		public void receiveMessage(Message message) {
			messageProducer.produce(message);
		}
	}
	
}
