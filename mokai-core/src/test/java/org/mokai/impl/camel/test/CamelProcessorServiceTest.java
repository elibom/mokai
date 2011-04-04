package org.mokai.impl.camel.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.camel.CamelContext;
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
import org.mokai.Monitorable;
import org.mokai.Monitorable.Status;
import org.mokai.Processor;
import org.mokai.ProcessorContext;
import org.mokai.Service;
import org.mokai.Service.State;
import org.mokai.Serviceable;
import org.mokai.annotation.Resource;
import org.mokai.impl.camel.CamelProcessorService;
import org.mokai.impl.camel.ResourceRegistry;
import org.mokai.persist.MessageStore;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockConnector;
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
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(1);
		MockEndpoint failedEndpoint = addFailedValidationRoute(0);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
		
		Assert.assertEquals(Status.UNKNOWN, processorService.getStatus());
		
		simulateMessage(new Message(), "activemq:processor-test");
		
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(Status.OK, processorService.getStatus());
		Assert.assertEquals(1, processor.getCount());
		
		Message message = (Message) processor.getMessage(0);
		Assert.assertNotNull(message);
		Assert.assertEquals("test", message.getDestination());
		Assert.assertEquals(Message.DestinationType.PROCESSOR, message.getDestinationType());
	}
	
	@Test
	public void testProcessorServiceState() throws Exception {
		
		Processor processor = Mockito.mock(Processor.class);
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		Assert.assertEquals(State.STOPPED, processorService.getState());
		
		processorService.start();
		Assert.assertEquals(State.STARTED, processorService.getState());
		
		processorService.stop();
		Assert.assertEquals(State.STOPPED, processorService.getState());
		
	}
	
	/**
	 * Tests that a failed processor recovers after it process a good message
	 * @throws Exception
	 */
	@Test
	public void testProcessorStatus() throws Exception {

		// add failed validation
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(0);
		MockEndpoint failedEndpoint = addFailedValidationRoute(1);
		
		Processor processor = Mockito.mock(Processor.class);
		Mockito
			.doThrow(new NullPointerException())
			.when(processor).process(Mockito.any(Message.class));
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
		
		// check that the status is UNKNOWN
		Assert.assertEquals(Status.UNKNOWN, processorService.getStatus());
		
		// simulate the message
		simulateMessage(new Message(), "activemq:processor-test");
		
		// wait until the message fails
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		// check that the status is FAILED
		Assert.assertEquals(Status.FAILED, processorService.getStatus());
		
		// add processed validation
		outboundEndpoint.reset();
		outboundEndpoint.expectedMessageCount(1);
		failedEndpoint.reset();
		failedEndpoint.expectedMessageCount(0);
		
		Mockito.doNothing()
			.when(processor)
			.process(Mockito.any(Message.class));

		// simulate the message
		simulateMessage(new Message(), "activemq:processor-test"); 
		
		// wait until the message is processed
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		// check that the status is OK
		Assert.assertEquals(Status.OK, processorService.getStatus());
	}
	
	/**
	 * Tests a Monitorable Processor with an OK status. 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMonitorableProcessorStatus() throws Exception {
		
		// add processed validation
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(1);
		MockEndpoint failedEndpoint = addFailedValidationRoute(0);
		
		Processor processor = 
			Mockito.mock(Processor.class, Mockito.withSettings().extraInterfaces(Monitorable.class));
		Mockito
			.when(((Monitorable) processor).getStatus())
			.thenReturn(Status.OK);
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
		
		Assert.assertEquals(Status.OK, processorService.getStatus());
		
		// simulate the message
		simulateMessage(new Message(), "activemq:processor-test");
		
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(Status.OK, processorService.getStatus());
	}
	
	@Test
	public void testFailedMonitorableProcessorStatus() throws Exception {
		
		// add processed validation
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(1);
		MockEndpoint failedEndpoint = addFailedValidationRoute(0);
		
		Processor processor = 
			Mockito.mock(Processor.class, Mockito.withSettings().extraInterfaces(Monitorable.class));
		Mockito
			.when(((Monitorable) processor).getStatus())
			.thenReturn(Status.FAILED);
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
		
		Assert.assertEquals(Status.FAILED, processorService.getStatus());
		
		// simulate the message
		simulateMessage(new Message(), "activemq:processor-test");
		
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(Status.FAILED, processorService.getStatus());
	}
	
	@Test
	public void testConflictMonitorableProcessorStatus() throws Exception {
		// add failed validation
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(0);
		MockEndpoint failedEndpoint = addFailedValidationRoute(1);
		
		Processor processor = 
			Mockito.mock(Processor.class, Mockito.withSettings().extraInterfaces(Monitorable.class));
		Mockito
			.doThrow(new NullPointerException())
			.when(processor).process(Mockito.any(Message.class));
		Mockito
			.when(((Monitorable) processor).getStatus())
			.thenReturn(Status.OK);
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
		Assert.assertEquals(Status.OK, processorService.getStatus());
		
		// simulate the message
		simulateMessage(new Message(), "activemq:processor-test");
		
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(Status.FAILED, processorService.getStatus());
	}
	
	@Test
	public void testReceiveMessage() throws Exception {
		
		// validation route
		MockEndpoint inboundEndpoint = addInboundValidationRoute(1);

		SimpleReceiverProcessor processor = new SimpleReceiverProcessor();
		new CamelProcessorService("test", 0, processor, resourceRegistry).start();
		
		// simulate receiving message
		processor.receiveMessage(new Message(Message.SMS_TYPE));
		
		// validate results
		inboundEndpoint.assertIsSatisfied();
		
		Exchange exchange = inboundEndpoint.getReceivedExchanges().iterator().next();
		Message message = exchange.getIn().getBody(Message.class);
		
		Assert.assertNotNull(message.getReference());
		Assert.assertEquals("test", message.getSource());
		Assert.assertEquals(Message.SourceType.PROCESSOR, message.getSourceType());
		Assert.assertEquals(Message.Direction.INBOUND, message.getDirection());
	}
	
	/**
	 * Tests that processing actions (pre and post) are working
	 * @throws Exception
	 */
	@Test
	public void testProcessingActions() throws Exception {
		
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(1);
		MockEndpoint failedEndpoint = addFailedValidationRoute(0);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		
		// add a pre-processing action
		MockAction preProcessingAction = new MockAction();
		processorService.addPreProcessingAction(preProcessingAction);
		
		// add another pre-processing action that changes the message
		processorService.addPreProcessingAction(new Action() {

			@Override
			public void execute(Message message) throws Exception {
				Message smsMessage = (Message) message;
				smsMessage.setProperty("from", "1234");
			}
			
		});
		
		// add a post-processing action
		MockAction postProcessingAction = new MockAction();
		processorService.addPostProcessingAction(postProcessingAction);
		
		// add another post-processing action that changes the message
		processorService.addPostProcessingAction(new Action() {

			@Override
			public void execute(Message message) throws Exception {
				Message smsMessage = (Message) message;
				smsMessage.setProperty("to", "1111");
			}
			
		});
		
		processorService.start();
		
		simulateMessage(new Message(), "activemq:processor-test");
		
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(1, processor.getCount());
		Assert.assertEquals(1, preProcessingAction.getChanged());
		Assert.assertEquals(1, postProcessingAction.getChanged());
		
		Message message = (Message) processor.getMessage(0);
		Assert.assertEquals("1234", message.getProperty("from", String.class));
		Assert.assertEquals("1111", message.getProperty("to", String.class));
	}
	
	@Test
	public void testPostReceivingActions() throws Exception {
		// validation route
		MockEndpoint inboundEndpoint = addInboundValidationRoute(1);
		
		SimpleReceiverProcessor processor = new SimpleReceiverProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
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
		processor.receiveMessage(new Message());
		
		// validate results
		inboundEndpoint.assertIsSatisfied();
		Assert.assertEquals(1, postReceivingAction.getChanged());
		
		Exchange exchange = inboundEndpoint.getReceivedExchanges().iterator().next();
		Message message = exchange.getIn().getBody(Message.class);
		Assert.assertEquals("germanescobar", message.getAccountId());
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithoutResourceRegistry() throws Exception {
		MockProcessor processor = new MockProcessor();
		new CamelProcessorService("test", 0, processor, null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithoutCamelContext() throws Exception {
		
		// an empty resource registry
		ResourceRegistry resourceRegistry = new ResourceRegistry();
		
		MockProcessor processor = new MockProcessor();
		new CamelProcessorService("test", 0, processor, resourceRegistry);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullProcessor() throws Exception {
		new CamelProcessorService("test", 0, null, resourceRegistry);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullId() throws Exception {
		new CamelProcessorService(null, 0, new MockProcessor(), resourceRegistry);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullAcceptor() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		processorService.addAcceptor(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithEmptyId() throws Exception {
		new CamelProcessorService("", 0, new MockProcessor(), resourceRegistry);
	}
	
	@Test
	public void testIdWithSpaces() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("T e s T", 0, new MockProcessor(), resourceRegistry);
		Assert.assertEquals("test", processorService.getId());
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullPreProcessingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		processorService.addPreProcessingAction(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullPostProcessingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		processorService.addPostProcessingAction(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullPostReceivingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		processorService.addPostReceivingAction(null);
	}
	
	/**
	 * Tests that if an exception occurs in the processor, the message is sent to the failedMessages queue.
	 * @throws Exception
	 */
	@Test
	public void testProcessorException() throws Exception {
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(0);
		MockEndpoint failedEndpoint = addFailedValidationRoute(1);
		
		CamelProcessorService processorService = new CamelProcessorService("test", 0, new Processor() {

			@Override
			public void process(Message message) {
				throw new NullPointerException();	
			}

			@Override
			public boolean supports(Message message) {
				return true;
			}
			
		}, resourceRegistry);
		processorService.start();
		
		simulateMessage(new Message(), "activemq:processor-test");
		
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Exchange exchange = failedEndpoint.getReceivedExchanges().iterator().next();
		Message smsMessage = exchange.getIn().getBody(Message.class);

		Assert.assertEquals(Message.DestinationType.PROCESSOR, smsMessage.getDestinationType());
		Assert.assertEquals("test", smsMessage.getDestination());
		Assert.assertEquals(Message.Status.FAILED, smsMessage.getStatus());
	}
	
	@Test
	public void testProcessorExceptionAndRecovery() throws Exception {
		
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(1);
		MockEndpoint failedEndpoint = addFailedValidationRoute(0);
		
		CamelProcessorService processorService = new CamelProcessorService("test", 0, new Processor() {
			
			private int times = 0;

			@Override
			public void process(Message message) {
				if (times == 0) {
					times++;
					throw new NullPointerException();
				}
			}

			@Override
			public boolean supports(Message message) {
				return true;
			}
			
		}, resourceRegistry);
		processorService.start();
		
		simulateMessage(new Message(), "activemq:processor-test");
		
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
	}
	
	@Test
	public void testAddRemoveAcceptors() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		
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
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		
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
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		
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
			new CamelProcessorService("test", 0, new MockProcessor(), resourceRegistry);
		
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
	public void testAddRemoveConfigurableAcceptor() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, Mockito.mock(Processor.class), resourceRegistry);
		
		Acceptor configurableAcceptor = Mockito.mock(Acceptor.class, 
				Mockito.withSettings().extraInterfaces(Configurable.class));
		
		processorService.addAcceptor(configurableAcceptor);
		processorService.removeAcceptor(configurableAcceptor);
		
		Mockito.verify((Configurable) configurableAcceptor).configure();
		Mockito.verify((Configurable) configurableAcceptor).destroy();
		
	}
	
	@Test
	public void testAddRemoveConfigurablePreProcessingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, Mockito.mock(Processor.class), resourceRegistry);
		
		Action configurableAction = Mockito.mock(Action.class, 
				Mockito.withSettings().extraInterfaces(Configurable.class));
		
		processorService.addPreProcessingAction(configurableAction);
		processorService.removePreProcessingAction(configurableAction);
		
		Mockito.verify((Configurable) configurableAction).configure();
		Mockito.verify((Configurable) configurableAction).destroy();
	}
	
	@Test
	public void testAddRemoveConfigurablePostProcessingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, Mockito.mock(Processor.class), resourceRegistry);
		
		Action configurableAction = Mockito.mock(Action.class, 
				Mockito.withSettings().extraInterfaces(Configurable.class));
		
		processorService.addPostProcessingAction(configurableAction);
		processorService.removePostProcessingAction(configurableAction);
		
		Mockito.verify((Configurable) configurableAction).configure();
		Mockito.verify((Configurable) configurableAction).destroy();
	}
	
	@Test
	public void testAddRemoveConfigurablePostReceivingAction() throws Exception {
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, Mockito.mock(Processor.class), resourceRegistry);
		
		Action configurableAction = Mockito.mock(Action.class, 
				Mockito.withSettings().extraInterfaces(Configurable.class));
		
		processorService.addPostReceivingAction(configurableAction);
		processorService.removePostReceivingAction(configurableAction);
		
		Mockito.verify((Configurable) configurableAction).configure();
		Mockito.verify((Configurable) configurableAction).destroy();
	}
	
	@Test
	public void testPreProcessingActionException() throws Exception {

		MockEndpoint outboundEndpoint = addOutboundValidationRoute(0);
		MockEndpoint failedEndpoint = addFailedValidationRoute(1);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = new CamelProcessorService("test", 0, processor, resourceRegistry);

		Action action = Mockito.mock(Action.class);
		Mockito.doThrow(new NullPointerException()).when(action).execute(Mockito.any(Message.class));
		
		processorService.addPreProcessingAction(action);

		processorService.start();

		simulateMessage(new Message(), "activemq:processor-test");
		
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Exchange exchange = failedEndpoint.getReceivedExchanges().iterator().next();
		Message smsMessage = exchange.getIn().getBody(Message.class);

		Assert.assertEquals(Message.DestinationType.PROCESSOR, smsMessage.getDestinationType());
		Assert.assertEquals("test", smsMessage.getDestination());
		Assert.assertEquals(Message.Status.FAILED, smsMessage.getStatus());
		
		System.out.println("testPreProcessingActionException finished ...");
	}
	
	@Test
	public void testPostProcessingActionException() throws Exception {

		MockEndpoint outboundEndpoint = addOutboundValidationRoute(0);
		MockEndpoint failedEndpoint = addFailedValidationRoute(1);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = new CamelProcessorService("test", 0, processor, resourceRegistry);

		Action action = Mockito.mock(Action.class);
		Mockito.doThrow(new NullPointerException()).when(action).execute(Mockito.any(Message.class));
		
		processorService.addPostProcessingAction(action);

		processorService.start();

		simulateMessage(new Message(), "activemq:processor-test");
		
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Exchange exchange = failedEndpoint.getReceivedExchanges().iterator().next();
		Message smsMessage = exchange.getIn().getBody(Message.class);

		Assert.assertEquals(Message.DestinationType.PROCESSOR, smsMessage.getDestinationType());
		Assert.assertEquals("test", smsMessage.getDestination());
		Assert.assertEquals(Message.Status.FAILED, smsMessage.getStatus());
		
		System.out.println("testPreProcessingActionException finished ...");
	}
	
	@Test
	public void testNonServiceableConnector() throws Exception {
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		
		processorService.start();
		Assert.assertEquals(Service.State.STARTED, processorService.getState());
		
		processorService.stop();
		Assert.assertEquals(Service.State.STOPPED, processorService.getState());
	}
	
	@Test
	public void testServiceableConnector() throws Exception {
		
		// mock Processor and Serviceable
		Processor processor = Mockito.mock(Processor.class, 
				Mockito.withSettings().extraInterfaces(Serviceable.class));
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
		
		// verify
		Assert.assertEquals(Service.State.STARTED, processorService.getState());
		Mockito.verify((Serviceable) processor).doStart();
		
		
		processorService.stop();
		
		// verify
		Assert.assertEquals(Service.State.STOPPED, processorService.getState());
		Mockito.verify((Serviceable) processor).doStop();
		
	}
	
	@Test(expectedExceptions=ExecutionException.class)
	public void shouldFailOnStartException() throws Exception {
		// mock Processor and Serviceable
		Processor processor = Mockito.mock(Processor.class, 
				Mockito.withSettings().extraInterfaces(Serviceable.class));
		Mockito.doThrow(new NullPointerException()).when((Serviceable) processor).doStart();
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
	}
	
	@Test(expectedExceptions=ExecutionException.class)
	public void shouldFailOnStopException() throws Exception {
		// mock Processor and Serviceable
		Processor processor = Mockito.mock(Processor.class, 
				Mockito.withSettings().extraInterfaces(Serviceable.class));
		Mockito.doThrow(new NullPointerException()).when((Serviceable) processor).doStop();
		
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		processorService.start();
		
		processorService.stop();
	}
	
	@Test
	public void testMessageStoppedProcessor() throws Exception {
		
		System.out.println("testMessageStoppedProcessor started");
		
		MockEndpoint outboundEndpoint = addOutboundValidationRoute(2);
		outboundEndpoint.setResultWaitTime(3000);
		MockEndpoint failedEndpoint = addFailedValidationRoute(0);
		
		MockProcessor processor = new MockProcessor();
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		
		simulateMessage(new Message(), "activemq:processor-test");
		
		Thread.sleep(3000);
		
		Assert.assertEquals(1, processorService.getNumQueuedMessages());
		
		Assert.assertEquals(0, processor.getCount());
		
		processorService.start();
		
		simulateMessage(new Message(), "activemq:processor-test");
		
		outboundEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		failedEndpoint.assertIsSatisfied(DEFAULT_TIMEOUT);
		
		Assert.assertEquals(2, processor.getCount());
		
		Assert.assertEquals(0, processorService.getNumQueuedMessages());
	}
	
	@Test
	public void testInjectResourceToProcessor() throws Exception {

		// add the resource to the resource registry
		resourceRegistry.putResource(MessageStore.class, Mockito.mock(MessageStore.class));
		
		MockConnector processor = new MockConnector();
		new CamelProcessorService("test", 0, processor, resourceRegistry);
		
		Assert.assertNotNull(processor.getMessageStore());
		
		ProcessorContext context = processor.getContext();
		Assert.assertNotNull(context);
		Assert.assertEquals("test", context.getId());
	}
	
	@Test
	public void testInjectResourceToAcceptor() throws Exception {
		
		// add he resource to the resource registry
		resourceRegistry.putResource(MessageStore.class, Mockito.mock(MessageStore.class));
		
		Processor processor = Mockito.mock(Processor.class);
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		
		MockAcceptor acceptor = new MockAcceptor();
		processorService.addAcceptor(acceptor);
		
		Assert.assertNotNull(acceptor.getMessageStore());
	}
	
	@Test
	public void testInjectResourceToActions() throws Exception {
		
		// add he resource to the resource registry
		resourceRegistry.putResource(MessageStore.class, Mockito.mock(MessageStore.class));
		
		Processor processor = Mockito.mock(Processor.class);
		CamelProcessorService processorService = 
			new CamelProcessorService("test", 0, processor, resourceRegistry);
		
		// test inject resource to pre-processing action
		MockAction action = new MockAction();
		processorService.addPreProcessingAction(action);
		Assert.assertNotNull(action.getMessageStore());
		
		// test inject resource to post-processing action
		action = new MockAction();
		processorService.addPostProcessingAction(action);
		Assert.assertNotNull(action.getMessageStore());
		
		// test inject resource to post-receiving action
		action = new MockAction();
		processorService.addPostReceivingAction(action);
		Assert.assertNotNull(action.getMessageStore());
	}
	
	/**
	 * Helper method to create the route that validates the output of the receivers.
	 * @return
	 * @throws Exception
	 */
	private MockEndpoint addInboundValidationRoute(int expectedMessages) throws Exception {
		
		CamelContext camelContext = resourceRegistry.getResource(CamelContext.class);
		camelContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("activemq:inboundRouter").to("mock:validateInbound");
			}
			
		});
		
		MockEndpoint ret = camelContext.getEndpoint("mock:validateInbound", MockEndpoint.class);
		ret.expectedMessageCount(expectedMessages);
		
		return ret;
	}
	
	/**
	 * Helper method to create the route that validates the output of the processors.
	 * @return
	 * @throws Exception
	 */
	private MockEndpoint addOutboundValidationRoute(int expectedMessages) throws Exception {
		
		CamelContext camelContext = resourceRegistry.getResource(CamelContext.class);
		camelContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("direct:processedmessages").to("mock:validateOutbound");
			}
			
		});
		
		MockEndpoint ret = camelContext.getEndpoint("mock:validateOutbound", MockEndpoint.class);
		ret.expectedMessageCount(expectedMessages);
		
		return ret;
	}
	
	/**
	 * Helper method to create the route that validates the failed messages.
	 * @return
	 * @throws Exception
	 */
	private MockEndpoint addFailedValidationRoute(int expectedMessages) throws Exception {
		
		CamelContext camelContext = resourceRegistry.getResource(CamelContext.class);
		camelContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("activemq:failedmessages").to("mock:validateFailed");
			}
			
		});
		
		MockEndpoint ret = camelContext.getEndpoint("mock:validateFailed", MockEndpoint.class);
		ret.expectedMessageCount(expectedMessages);
		
		return ret;
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
			if (Message.class.isInstance(message)) {
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
	private class SimpleReceiverProcessor implements Processor {
		
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
