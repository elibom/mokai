package org.mokai.impl.camel.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.mockito.Mockito;
import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Configurable;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Processor;
import org.mokai.Service;
import org.mokai.Serviceable;
import org.mokai.annotation.Resource;
import org.mokai.impl.camel.CamelProcessorService;
import org.mokai.message.SmsMessage;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class CamelProcessorServiceTest extends CamelBaseTest {
	
	public final long DEFAULT_TIMEOUT = 3000;

	@Test
	public void testProcessMessage() throws Exception {
		
		// add processed validation
		addOutboundValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		processorService.start();
		
		simulateMessage(new SmsMessage(), "activemq:processor-test");
		
		resultEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(1, processor.getCount());
		
		SmsMessage message = (SmsMessage) processor.getMessage(0);
		Assert.assertNotNull(message);
		Assert.assertEquals("test", message.getDestination());
		Assert.assertEquals(Message.DestinationType.PROCESSOR, message.getDestinationType());
	}
	
	@Test
	public void testReceiveMessage() throws Exception {
		
		// validation route
		addInboundValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);
		
		SimpleReceiverProcessor processor = new SimpleReceiverProcessor();
		new CamelProcessorService("test", 0, processor, camelContext).start();
		
		// simulate receiving message
		processor.receiveMessage(new SmsMessage());
		
		// validate results
		resultEndpoint.assertIsSatisfied();
		
		Exchange exchange = resultEndpoint.getReceivedExchanges().iterator().next();
		SmsMessage message = exchange.getIn().getBody(SmsMessage.class);
		
		Assert.assertNotNull(message.getReference());
		Assert.assertEquals("test", message.getSource());
		Assert.assertEquals(Message.SourceType.PROCESSOR, message.getSourceType());
		Assert.assertEquals(Message.Type.INBOUND, message.getType());
	}
	
	/**
	 * Tests that processing actions (pre and post) are working
	 * @throws Exception
	 */
	@Test
	public void testProcessingActions() throws Exception {
		
		addOutboundValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		
		// add a pre-processing action
		MockAction preProcessingAction = new MockAction();
		processorService.addPreProcessingAction(preProcessingAction);
		
		// add another pre-processing action that changes the message
		processorService.addPreProcessingAction(new Action() {

			@Override
			public void execute(Message message) throws Exception {
				SmsMessage smsMessage = (SmsMessage) message;
				smsMessage.setFrom("1234");
			}
			
		});
		
		// add a post-processing action
		MockAction postProcessingAction = new MockAction();
		processorService.addPostProcessingAction(postProcessingAction);
		
		// add another post-processing action that changes the message
		processorService.addPostProcessingAction(new Action() {

			@Override
			public void execute(Message message) throws Exception {
				SmsMessage smsMessage = (SmsMessage) message;
				smsMessage.setTo("1111");
			}
			
		});
		
		processorService.start();
		
		simulateMessage(new SmsMessage(), "activemq:processor-test");
		
		resultEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(1, processor.getCount());
		Assert.assertEquals(1, preProcessingAction.getChanged());
		Assert.assertEquals(1, postProcessingAction.getChanged());
		
		SmsMessage message = (SmsMessage) processor.getMessage(0);
		Assert.assertEquals("1234", message.getFrom());
		Assert.assertEquals("1111", message.getTo());
	}
	
	@Test
	public void testPostReceivingActions() throws Exception {
		// validation route
		addInboundValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);
		
		SimpleReceiverProcessor processor = new SimpleReceiverProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		processorService.start();
		
		// add post-receiving action
		MockAction postReceivingAction = new MockAction();
		processorService.addPostReceivingAction(postReceivingAction);
		
		// add another post-receiving action that changes the message
		processorService.addPostReceivingAction(new Action() {

			@Override
			public void execute(Message message) throws Exception {
				message.setAccountId("germanescobar");
			}
			
		});
		
		// simulate we receive a message
		processor.receiveMessage(new SmsMessage());
		
		// validate results
		resultEndpoint.assertIsSatisfied();
		Assert.assertEquals(1, postReceivingAction.getChanged());
		
		Exchange exchange = resultEndpoint.getReceivedExchanges().iterator().next();
		Message message = exchange.getIn().getBody(Message.class);
		Assert.assertEquals("germanescobar", message.getAccountId());
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithoutCamelContext() throws Exception {
		MockProcessor processor = new MockProcessor();
		new CamelProcessorService("test", 0, processor, null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullProcessor() throws Exception {
		new CamelProcessorService("test", 0, null, camelContext);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullId() throws Exception {
		new CamelProcessorService(null, 0, new MockProcessor(), camelContext);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullAcceptor() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		processorService.addAcceptor(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithEmptyId() throws Exception {
		new CamelProcessorService("", 0, new MockProcessor(), camelContext);
	}
	
	@Test
	public void testIdWithSpaces() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("T e s T", 0, new MockProcessor(), camelContext);
		Assert.assertEquals("test", processorService.getId());
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullPreProcessingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		processorService.addPreProcessingAction(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullPostProcessingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		processorService.addPostProcessingAction(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullPostReceivingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		processorService.addPostReceivingAction(null);
	}
	
	/**
	 * Tests that if an exception occurs in the processor, the message is sent to the failedMessages queue.
	 * @throws Exception
	 */
	@Test
	public void testProcessorException() throws Exception {
		addFailedValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);
		
		CamelProcessorService processorService = new CamelProcessorService("test", 0, new Processor() {

			@Override
			public void process(Message message) {
				throw new NullPointerException();	
			}

			@Override
			public boolean supports(Message message) {
				return true;
			}
			
		}, camelContext);
		processorService.start();
		
		simulateMessage(new SmsMessage(), "activemq:processor-test");
		
		resultEndpoint.assertIsSatisfied();
		
		Exchange exchange = resultEndpoint.getReceivedExchanges().iterator().next();
		SmsMessage smsMessage = exchange.getIn().getBody(SmsMessage.class);

		Assert.assertEquals(Message.DestinationType.PROCESSOR, smsMessage.getDestinationType());
		Assert.assertEquals("test", smsMessage.getDestination());
		Assert.assertEquals(Message.Status.FAILED, smsMessage.getStatus());
	}
	
	@Test
	public void testAddRemoveAcceptors() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		
		Acceptor acceptor1 = Mockito.mock(Acceptor.class);
		Acceptor acceptor2 = Mockito.mock(Acceptor.class);
		
		// add first acceptor
		processorService.addAcceptor(acceptor1);
		Assert.assertEquals(1, processorService.getAcceptors().size());
		
		// add second acceptor
		processorService.addAcceptor(acceptor2);
		Assert.assertEquals(2, processorService.getAcceptors().size());
		
		List<Acceptor> acceptors = processorService.getAcceptors();
		Assert.assertEquals(acceptors.get(0), acceptor1);
		Assert.assertEquals(acceptors.get(1), acceptor2);
		
		// remove acceptor 1
		processorService.removeAcceptor(acceptor1);
		
		Assert.assertEquals(1, processorService.getAcceptors().size());
		acceptors = processorService.getAcceptors();
		Assert.assertEquals(acceptors.get(0), acceptor2);
	}
	
	@Test
	public void testAddRemovePreProcessingActions() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		
		Action action1 = new MockAction();
		Action action2 = new MockAction();
		
		// add first action
		processorService.addPreProcessingAction(action1);
		Assert.assertEquals(1, processorService.getPreProcessingActions().size());
		
		// add second action
		processorService.addPreProcessingAction(action2);
		Assert.assertEquals(2, processorService.getPreProcessingActions().size());
		
		List<Action> preProcessingActions  = processorService.getPreProcessingActions();
		Assert.assertEquals(preProcessingActions.get(0), action1);
		Assert.assertEquals(preProcessingActions.get(1), action2);
		
		// remove action 1
		processorService.removePreProcessingAction(action1);
		
		Assert.assertEquals(1, processorService.getPreProcessingActions().size());
		preProcessingActions  = processorService.getPreProcessingActions();
		Assert.assertEquals(preProcessingActions.get(0), action2);
		
	}
	
	@Test
	public void testAddRemovePostProcessingActions() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		
		Action action1 = new MockAction();
		Action action2 = new MockAction();
		
		// add first action
		processorService.addPostProcessingAction(action1);
		Assert.assertEquals(1, processorService.getPostProcessingActions().size());
		
		// add second action
		processorService.addPostProcessingAction(action2);
		Assert.assertEquals(2, processorService.getPostProcessingActions().size());
		
		List<Action> postProcessingActions  = processorService.getPostProcessingActions();
		Assert.assertEquals(postProcessingActions.get(0), action1);
		Assert.assertEquals(postProcessingActions.get(1), action2);
		
		// remove action 1
		processorService.removePostProcessingAction(action1);
		
		Assert.assertEquals(1, processorService.getPostProcessingActions().size());
		postProcessingActions  = processorService.getPostProcessingActions();
		Assert.assertEquals(postProcessingActions.get(0), action2);
		
	}
	
	@Test
	public void testAddRemovePostReceivingActions() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), camelContext);
		
		Action action1 = new MockAction();
		Action action2 = new MockAction();
		
		// add first action
		processorService.addPostReceivingAction(action1);
		Assert.assertEquals(1, processorService.getPostReceivingActions().size());
		
		// add second action
		processorService.addPostReceivingAction(action2);
		Assert.assertEquals(2, processorService.getPostReceivingActions().size());
		
		List<Action> postReceivingActions  = processorService.getPostReceivingActions();
		Assert.assertEquals(postReceivingActions.get(0), action1);
		Assert.assertEquals(postReceivingActions.get(1), action2);
		
		// remove action 1
		processorService.removePostReceivingAction(action1);
		
		Assert.assertEquals(1, processorService.getPostReceivingActions().size());
		postReceivingActions  = processorService.getPostReceivingActions();
		Assert.assertEquals(postReceivingActions.get(0), action2);
		
	}
	
	@Test
	public void testPreProcessingActionException() throws Exception {
		addFailedValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(1);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = new CamelProcessorService("test", 0, processor, camelContext);
		
		SmsMessage message = new SmsMessage();

		Action action = Mockito.mock(Action.class);
		Mockito.doThrow(new NullPointerException()).when(action).execute(message);
		
		processorService.addPreProcessingAction(action);

		processorService.start();

		simulateMessage(message, "activemq:processor-test");
		
		resultEndpoint.assertIsSatisfied(3000);
		
		Exchange exchange = resultEndpoint.getReceivedExchanges().iterator().next();
		SmsMessage smsMessage = exchange.getIn().getBody(SmsMessage.class);

		Assert.assertEquals(Message.DestinationType.PROCESSOR, smsMessage.getDestinationType());
		Assert.assertEquals("test", smsMessage.getDestination());
		Assert.assertEquals(Message.Status.FAILED, smsMessage.getStatus());
		
	}
	
	@Test
	public void testNonServiceableConnector() throws Exception {
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		
		processorService.start();
		Assert.assertEquals(Service.Status.STARTED, processorService.getStatus());
		
		processorService.stop();
		Assert.assertEquals(Service.Status.STOPPED, processorService.getStatus());
	}
	
	@Test
	public void testServiceableConnector() throws Exception {
		
		// mock Processor and Serviceable
		Processor processor = Mockito.mock(Processor.class, 
				Mockito.withSettings().extraInterfaces(Serviceable.class));
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		processorService.start();
		
		// verify
		Assert.assertEquals(Service.Status.STARTED, processorService.getStatus());
		Mockito.verify((Serviceable) processor).doStart();
		
		processorService.stop();
		
		// verify
		Assert.assertEquals(Service.Status.STOPPED, processorService.getStatus());
		Mockito.verify((Serviceable) processor).doStop();
		
	}
	
	@Test(expectedExceptions=ExecutionException.class)
	public void shouldFailOnStartException() throws Exception {
		// mock Processor and Serviceable
		Processor processor = Mockito.mock(Processor.class, 
				Mockito.withSettings().extraInterfaces(Serviceable.class));
		Mockito.doThrow(new NullPointerException()).when((Serviceable) processor).doStart();
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		processorService.start();
	}
	
	@Test(expectedExceptions=ExecutionException.class)
	public void shouldFailOnStopException() throws Exception {
		// mock Processor and Serviceable
		Processor processor = Mockito.mock(Processor.class, 
				Mockito.withSettings().extraInterfaces(Serviceable.class));
		Mockito.doThrow(new NullPointerException()).when((Serviceable) processor).doStop();
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		processorService.start();
		
		processorService.stop();
	}
	
	@Test
	public void testConfigurableConnector() throws Exception {
		// mock Processor and Configurable
		Processor processor = Mockito.mock(Processor.class, 
				Mockito.withSettings().extraInterfaces(Configurable.class));
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		processorService.start();
		
		// verify
		Mockito.verify((Configurable) processor).configure();
		
		processorService.destroy();
		
		// verify
		Assert.assertEquals(Service.Status.STOPPED, processorService.getStatus());
		Mockito.verify((Configurable) processor).destroy();
	}
	
	@Test
	public void testMessageStoppedProcessor() throws Exception {
		
		addOutboundValidationRoute();
		
		// expected results
		MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:validate", MockEndpoint.class);
		resultEndpoint.expectedMessageCount(2);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, camelContext);
		
		try {
			Future<Object> future = camelProducer.asyncRequestBody("activemq:processor-test", new SmsMessage());
			future.get(3, TimeUnit.SECONDS);
			Assert.fail();
		} catch (TimeoutException e) {
			
		}
		
		Assert.assertEquals(0, processor.getCount());
		
		processorService.start();
		
		simulateMessage(new SmsMessage(), "activemq:processor-test");
		
		resultEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(2, processor.getCount());
	}
	
	/**
	 * Helper method to create the route that validates the output of the receivers.
	 * @throws Exception
	 */
	private void addInboundValidationRoute() throws Exception {
		camelContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("activemq:inboundRouter").to("mock:validate");
			}
			
		});
	}
	
	/**
	 * Helper method to create the route that validates the output of the processors.
	 * @throws Exception
	 */
	private void addOutboundValidationRoute() throws Exception {
		camelContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("direct:processedmessages").to("mock:validate");
			}
			
		});
	}
	
	/**
	 * Helper method to create the route that validates the failed messages.
	 * @throws Exception
	 */
	private void addFailedValidationRoute() throws Exception {
		camelContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("activemq:failedmessages").to("mock:validate");
			}
			
		});
	}
	
	/**
	 * Helper method to simulate sending messages
	 * @param message the message that wants to be sent.
	 * @param endpoint the enpoint to which we are going to send the message
	 */
	private void simulateMessage(Message message, String endpoint) {
		camelProducer.sendBody(endpoint, message);
	}
	
	/**
	 * Mock Processor that counts processed messages.
	 * 
	 * @author German Escobar
	 */
	private class MockProcessor implements Processor {
		
		private List<Message> messages = new ArrayList<Message>();

		@Override
		public void process(Message message) {
			messages.add(message);
		}

		@Override
		public boolean supports(Message message) {
			if (SmsMessage.class.isInstance(message)) {
				return true;
			}
			
			return false;
		}
		
		public int getCount() {
			return messages.size();
		}
		
		public Message getMessage(int index) {
			return messages.get(index);
		}

	}
	
	/**
	 * Simple receiver that exposes a sendMessage method to simulate messages.
	 * 
	 * @author German Escobar
	 */
	protected class SimpleReceiverProcessor implements Processor {
		
		@Resource
		private MessageProducer messageProducer;
		
		public void receiveMessage(Message message) {
			messageProducer.produce(message);
		}

		@Override
		public void process(Message message) {
		}

		@Override
		public boolean supports(Message message) {
			return false;
		}
	}
	
}
