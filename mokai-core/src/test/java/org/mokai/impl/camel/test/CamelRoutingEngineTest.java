package org.mokai.impl.camel.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.camel.ProducerTemplate;
import org.mockito.Mockito;
import org.mokai.Acceptor;
import org.mokai.Message;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.Processor;
import org.mokai.ProcessorService;
import org.mokai.Receiver;
import org.mokai.ReceiverService;
import org.mokai.Service;
import org.mokai.impl.camel.CamelRoutingEngine;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;
import org.testng.annotations.Test;

public class CamelRoutingEngineTest {
	
	@Test
	public void testCreateRemoveProcessors() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Processor processor = Mockito.mock(Processor.class);
		
		// create a processor service
		ProcessorService ps1 = 
			routingEngine.createProcessor("test1", 2000, processor);
		
		// check that the processor service was created successfully
		Assert.assertNotNull(ps1);
		Assert.assertEquals(Service.State.STARTED, ps1.getState());
		
		// check that there is only one processor
		List<ProcessorService> processorServices = routingEngine.getProcessors();
		Assert.assertEquals(1, processorServices.size());
		
		// create a second and third processor
		ProcessorService ps2 = 
			routingEngine.createProcessor("test2", 0, processor);
		ProcessorService ps3 =
			routingEngine.createProcessor("test3", 1000, processor);
		
		processorServices = routingEngine.getProcessors();
		Assert.assertEquals(3, processorServices.size());
		
		// check that the processors are in order
		ProcessorService psTest = processorServices.get(0);
		Assert.assertEquals(ps2, psTest); // the one with 0 priority
		
		psTest = processorServices.get(1);
		Assert.assertEquals(ps3, psTest); // the one with 1000 priority
		
		psTest = processorServices.get(2); 
		Assert.assertEquals(ps1, psTest); // the one with 2000 priority
		
		// remove the processor test3
		routingEngine.removeProcessor("test3");
		
		// check that there are only 2 processor services
		processorServices = routingEngine.getProcessors();
		Assert.assertEquals(2, processorServices.size());
		
		psTest = processorServices.get(0);
		Assert.assertEquals(ps2, psTest); // the one with 0 priority
		
		psTest = processorServices.get(1); 
		Assert.assertEquals(ps1, psTest); // the one with 2000 priority
		
		routingEngine.stop();
	}
	
	@Test
	public void testRetrieveProcessor() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Processor processor = Mockito.mock(Processor.class);
		
		// create a processor service
		ProcessorService processorService = 
			routingEngine.createProcessor("test", 2000, processor);
		
		// retrieve an existing processor
		ProcessorService psTest = routingEngine.getProcessor("test");
		Assert.assertEquals(processorService, psTest);
		
		// try to retrieve an unexisting processor
		psTest = routingEngine.getProcessor("nonexisting");
		Assert.assertNull(psTest);
		
		routingEngine.stop();
	}
	
	@Test(expectedExceptions=UnsupportedOperationException.class)
	public void shouldFailToModifyReturnedProcessors() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Processor processor = Mockito.mock(Processor.class);
		ProcessorService ps1 = 
			routingEngine.createProcessor("test1", 2000, processor);
		
		List<ProcessorService> processorService = routingEngine.getProcessors(); 
		processorService.add(ps1); // should fail
	}
	
	@Test(expectedExceptions=ObjectNotFoundException.class)
	public void shouldFailToRemoveNonExistingProcessor() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		routingEngine.removeProcessor("test");
	}
	
	@Test(expectedExceptions=ObjectAlreadyExistsException.class)
	public void shouldFailToAddExistingProcessor() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Processor processor = Mockito.mock(Processor.class);
		
		// create a processor service
		routingEngine.createProcessor("test", 2000, processor);
		
		// try to create another processor with the same id
		routingEngine.createProcessor("test", 1, processor);
	}
	
	@Test
	public void testCreateRemoveReceivers() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Receiver receiver = Mockito.mock(Receiver.class);
		
		ReceiverService rs1 = 
			routingEngine.createReceiver("test1", receiver);
		
		// check that the receiver service was created successfully
		Assert.assertNotNull(rs1);
		Assert.assertEquals(Service.State.STARTED, rs1.getState());
		
		// check that there is only one receiver
		Collection<ReceiverService> receivers = routingEngine.getReceivers();
		Assert.assertEquals(1, receivers.size());
		
		// create a second and third receiver
		ReceiverService rs2 = 
			routingEngine.createReceiver("test2", receiver);
		ReceiverService rs3 = 
			routingEngine.createReceiver("test3", receiver);
		
		receivers = routingEngine.getReceivers();
		Assert.assertEquals(3, receivers.size());
		
		// check that all receivers are contained
		Assert.assertTrue(receivers.contains(rs1));
		Assert.assertTrue(receivers.contains(rs2));
		Assert.assertTrue(receivers.contains(rs3));
		
		// remove one of the processors
		routingEngine.removeReceiver("test3");
		
		receivers = routingEngine.getReceivers();
		Assert.assertEquals(2, receivers.size());
		
		Assert.assertTrue(receivers.contains(rs1));
		Assert.assertTrue(receivers.contains(rs2));
		Assert.assertFalse(receivers.contains(rs3));
		
		routingEngine.stop();
	}
	
	public void testRetrieveReciever() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Receiver receiver = Mockito.mock(Receiver.class);
		
		// create a receiver service
		ReceiverService receiverService = routingEngine.createReceiver("test", receiver);
		
		// try to retrieve an existing receiver
		ReceiverService rsTest = routingEngine.getReceiver("test");
		Assert.assertEquals(receiverService, rsTest);
		
		// try to retrieve an unexisting receiver
		rsTest = routingEngine.getReceiver("notexistent");
		Assert.assertNull(rsTest);
		
		routingEngine.stop();
	}
	
	@Test(expectedExceptions=UnsupportedOperationException.class)
	public void shouldFailToModifyReturnedReceivers() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Receiver receiver = Mockito.mock(Receiver.class);
		ReceiverService rs1 = 
			routingEngine.createReceiver("test", receiver);
		
		
		Collection<ReceiverService> receiverServices = routingEngine.getReceivers(); 
		receiverServices.add(rs1); // should fail
	}
	
	@Test(expectedExceptions=ObjectNotFoundException.class)
	public void shouldFailToRemoveNonExistingReceiver() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		routingEngine.removeReceiver("test");
	}
	
	@Test(expectedExceptions=ObjectAlreadyExistsException.class)
	public void shouldFailToAddExistingReceiver() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Receiver receiver = Mockito.mock(Receiver.class);
		
		// create a receiver service
		routingEngine.createReceiver("test", receiver);
		
		// try to create another receiver with the same id
		routingEngine.createReceiver("test", receiver);
	}
	
	@Test
	public void testSimpleMessageFlow() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		// create the processor
		MockProcessor processor = new MockProcessor();
		ProcessorService processorService = 
			routingEngine.createProcessor("1", 1000, processor);
		processorService.addAcceptor(new MockAcceptor());
		
		// send the message
		ProducerTemplate producer = routingEngine.getCamelContext().createProducerTemplate();
		producer.requestBody("activemq:outboundRouter", new Message());
		
		Assert.assertEquals(1, processor.getCount());
		
		routingEngine.stop();
	}
	
	@Test
	public void testUnroutableMessage() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		
		// a custom message store
		MessageStore messageStore = new MessageStore() {

			@Override
			public Collection<Message> list(MessageCriteria criteria)
					throws StoreException {
				return null;
			}

			@Override
			public void saveOrUpdate(Message message) throws StoreException {
				try { barrier.await(); } catch (Exception e) {}
			}
			
		};
		
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.setMessageStore(messageStore);
		routingEngine.start();
		
		// create the processor
		MockProcessor processor = new MockProcessor();
		ProcessorService processorService = 
			routingEngine.createProcessor("1", 1000, processor);
		processorService.addAcceptor(new Acceptor() {

			@Override
			public boolean accepts(Message message) {
				return false;
			}
			
		});
		
		// send the message
		ProducerTemplate producer = routingEngine.getCamelContext().createProducerTemplate();
		producer.sendBody("activemq:outboundRouter", new Message());
		
		// wait
		barrier.await(3, TimeUnit.SECONDS);
		
		Assert.assertEquals(0, processor.getCount());
		
		routingEngine.stop();
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

	}
	
	protected class MockAcceptor implements Acceptor {

		@Override
		public boolean accepts(Message message) {
			return true;
		}
		
	}
}
