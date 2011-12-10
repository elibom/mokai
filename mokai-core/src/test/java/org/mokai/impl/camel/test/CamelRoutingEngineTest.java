package org.mokai.impl.camel.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.camel.ProducerTemplate;
import org.mokai.Acceptor;
import org.mokai.Configurable;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.Processor;
import org.mokai.Service;
import org.mokai.impl.camel.CamelRoutingEngine;
import org.mokai.impl.camel.UriConstants;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;
import org.mokai.types.mock.MockConnector;
import org.mokai.types.mock.MockServiceableConnector;
import org.testng.annotations.Test;

public class CamelRoutingEngineTest {
	
	@Test
	public void testCreateRemoveConnection() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Connector connector = mock(Connector.class);
		
		// create a connector service
		ConnectorService cs1 = routingEngine.addConnection("test1", connector).withPriority(2000);
		cs1.start();
		
		// check that the connector service was created successfully
		Assert.assertNotNull(cs1);
		Assert.assertEquals(Service.State.STARTED, cs1.getState());
		
		// check that there is only one connector
		List<ConnectorService> connectorServices = routingEngine.getConnections();
		Assert.assertEquals(1, connectorServices.size());
		
		// create a second and third connector
		ConnectorService cs2 = routingEngine.addConnection("test2", connector).withPriority(0);
		cs2.start();
		
		ConnectorService cs3 = routingEngine.addConnection("test3", connector).withPriority(1000);
		cs3.start();
		
		connectorServices = routingEngine.getConnections();
		Assert.assertEquals(3, connectorServices.size());
		
		// check that the connector are in order
		ConnectorService psTest = connectorServices.get(0);
		Assert.assertEquals(cs2, psTest); // the one with 0 priority
		
		psTest = connectorServices.get(1);
		Assert.assertEquals(cs3, psTest); // the one with 1000 priority
		
		psTest = connectorServices.get(2); 
		Assert.assertEquals(cs1, psTest); // the one with 2000 priority
		
		// remove the connector test3
		routingEngine.removeConnection("test3");
		
		// check that there are only 2 connector services
		connectorServices = routingEngine.getConnections();
		Assert.assertEquals(2, connectorServices.size());
		
		psTest = connectorServices.get(0);
		Assert.assertEquals(cs2, psTest); // the one with 0 priority
		
		psTest = connectorServices.get(1); 
		Assert.assertEquals(cs1, psTest); // the one with 2000 priority
		
		routingEngine.stop();
	}
	
	@Test
	public void testCreateConfigurableConnection() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Connector connector = mock(Connector.class, withSettings().extraInterfaces(Configurable.class));
		
		// add a connection
		routingEngine.addConnection("test1", connector);
		
		verify((Configurable) connector).configure();
		
		routingEngine.stop();
	}
	
	@Test
	public void testRetrieveConnection() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Connector connector = mock(Connector.class);
		
		// create and start a connector service
		ConnectorService connectorService = routingEngine.addConnection("test", connector)
			.withPriority(2000);
		connectorService.start();
		
		// retrieve an existing connector
		ConnectorService csTest = routingEngine.getConnection("test");
		Assert.assertEquals(connectorService, csTest);
		
		// try to retrieve an unexisting connector
		csTest = routingEngine.getConnection("nonexisting");
		Assert.assertNull(csTest);
		
		routingEngine.stop();
	}
	
	@Test
	public void testRetrieveConnections() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Connector connector = mock(Connector.class);
		
		ConnectorService cs1 = routingEngine.addConnection("test1", connector).withPriority(2000);
		ConnectorService cs2 = routingEngine.addConnection("test2", connector).withPriority(1000);
		ConnectorService cs3 = routingEngine.addConnection("test3", connector).withPriority(1500);
		
		List<ConnectorService> connectorServices = routingEngine.getConnections();
		
		Assert.assertNotNull(connectorServices);
		Assert.assertEquals(3, connectorServices.size());
		Assert.assertEquals(cs2, connectorServices.get(0));
		Assert.assertEquals(cs3, connectorServices.get(1));
		Assert.assertEquals(cs1, connectorServices.get(2));
	}
	
	@Test
	public void shouldFailToModifyReturnedConnections() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Connector connector = mock(Connector.class);
		ConnectorService cs1 = routingEngine.addConnection("test1", connector);
		
		List<ConnectorService> connectorServices = routingEngine.getConnections();
		Assert.assertEquals(connectorServices.size(), 1);
		
		connectorServices.add(cs1);
		Assert.assertEquals(connectorServices.size(), 2);
		
		Assert.assertEquals(routingEngine.getConnections().size(), 1);
	}
	
	@Test(expectedExceptions=ObjectNotFoundException.class)
	public void shouldFailToRemoveNonExistingConnection() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		routingEngine.removeConnection("test");
	}
	
	@Test(expectedExceptions=ObjectAlreadyExistsException.class)
	public void shouldFailToAddExistingConnection() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Connector connector = mock(Connector.class);
		
		// create a connector service
		routingEngine.addConnection("test", connector);
		
		// try to create another connector service with the same id
		routingEngine.addConnection("test", connector);
	}
	
	@Test
	public void testCreateRemoveApplication() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Connector connector = mock(Connector.class);
		
		ConnectorService cs1 = routingEngine.addApplication("test1", connector);
		cs1.start();
		
		// check that the application was created successfully
		Assert.assertNotNull(cs1);
		Assert.assertEquals(Service.State.STARTED, cs1.getState());
		
		// check that there is only one application
		Collection<ConnectorService> applications = routingEngine.getApplications();
		Assert.assertEquals(1, applications.size());
		
		// create a second and third receiver
		ConnectorService cs2 = routingEngine.addApplication("test2", connector);
		cs2.start();
		ConnectorService cs3 = routingEngine.addApplication("test3", connector);
		cs3.start();
		
		applications = routingEngine.getApplications();
		Assert.assertEquals(3, applications.size());
		
		// check that all applications are contained
		Assert.assertTrue(applications.contains(cs1));
		Assert.assertTrue(applications.contains(cs2));
		Assert.assertTrue(applications.contains(cs3));
		
		// remove one of the applications
		routingEngine.removeApplication("test3");
		
		applications = routingEngine.getApplications();
		Assert.assertEquals(2, applications.size());
		
		Assert.assertTrue(applications.contains(cs1));
		Assert.assertTrue(applications.contains(cs2));
		Assert.assertFalse(applications.contains(cs3));
		
		routingEngine.stop();
	}
	
	@Test
	public void testCreateConfigurableApplication() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Connector connector = mock(Connector.class, withSettings().extraInterfaces(Configurable.class));
		
		routingEngine.addApplication("test1", connector);
		
		verify((Configurable) connector).configure();
		
		routingEngine.stop();

	}
	
	public void testRetrieveApplication() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		Connector connector = mock(Connector.class);
		
		// create and start an application
		ConnectorService connectorService = routingEngine.addApplication("test", connector);
		connectorService.start();
		
		// try to retrieve an existing application
		ConnectorService csTest = routingEngine.getApplication("test");
		Assert.assertEquals(connectorService, csTest);
		
		// try to retrieve an unexisting application
		csTest = routingEngine.getApplication("notexistent");
		Assert.assertNull(csTest);
		
		routingEngine.stop();
	}
	
	@Test
	public void testRetrieveApplications() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Connector connector = mock(Connector.class);
		
		ConnectorService cs1 = routingEngine.addApplication("test1", connector).withPriority(2000);
		ConnectorService cs2 = routingEngine.addApplication("test2", connector).withPriority(1000);
		ConnectorService cs3 = routingEngine.addApplication("test3", connector).withPriority(1500);
		
		List<ConnectorService> connectorServices = routingEngine.getApplications();
		
		Assert.assertNotNull(connectorServices);
		Assert.assertEquals(3, connectorServices.size());
		Assert.assertEquals(cs2, connectorServices.get(0));
		Assert.assertEquals(cs3, connectorServices.get(1));
		Assert.assertEquals(cs1, connectorServices.get(2));
	}
	
	@Test
	public void shouldFailToModifyReturnedApplications() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Connector connector = mock(Connector.class);
		ConnectorService cs1 = routingEngine.addApplication("test", connector);
		
		Collection<ConnectorService> connectorServices = routingEngine.getApplications(); 
		Assert.assertEquals(connectorServices.size(), 1);
		
		// add another application to the returned collection
		connectorServices.add(cs1);
		Assert.assertEquals(connectorServices.size(), 2);
		
		Assert.assertEquals(routingEngine.getApplications().size(), 1);		
		
	}
	
	@Test(expectedExceptions=ObjectNotFoundException.class)
	public void shouldFailToRemoveNonExistingApplication() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		routingEngine.removeApplication("test");
	}
	
	@Test(expectedExceptions=ObjectAlreadyExistsException.class)
	public void shouldFailToAddExistingApplication() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Connector connector = mock(Connector.class);
		
		// create an application
		routingEngine.addApplication("test", connector);
		
		// try to create another application with the same id
		routingEngine.addApplication("test", connector);
	}
	
	@Test
	public void testStartRoutingEngineWithSlowConnectors() throws Exception {
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		
		Processor connection = new MockServiceableConnector().withStartWaitTime(5000);
		Connector application = new MockServiceableConnector().withStartWaitTime(5000);
		
		routingEngine.addConnection("test", connection);
		routingEngine.addApplication("test", application);
		
		long startTime = new Date().getTime();
		routingEngine.start();
		long endTime = new Date().getTime();
		
		Assert.assertTrue((endTime - startTime) < 5000);
		
		routingEngine.stop();
		
	}
	
	@Test
	public void testToConnectionsMessageFlow() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		
		// a custom message store
		MessageStore messageStore = new MockMessageStore(barrier, Message.STATUS_PROCESSED);
		
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.setMessageStore(messageStore);
		routingEngine.start();
		
		// create the connection
		MockProcessor processor = new MockProcessor();
		ConnectorService connection = routingEngine.addConnection("1", processor);
		connection.addAcceptor(new MockAcceptor());
		connection.start();
		
		// create the application
		MockConnector receiver = new MockConnector();
		ConnectorService application = routingEngine.addApplication("1", receiver);
		application.start();
		
		// send the message
		receiver.produceMessage(new Message());
		
		// wait
		barrier.await(20, TimeUnit.SECONDS);
		
		Assert.assertEquals(1, processor.getCount());
		
		routingEngine.stop();
	}
	
	@Test
	public void testToApplicationsMessageFlow() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		
		// a custom message store
		MessageStore messageStore = new MockMessageStore(barrier, Message.STATUS_PROCESSED);
		
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.setMessageStore(messageStore);
		routingEngine.start();
		
		// create the application
		MockProcessor processor = new MockProcessor();
		ConnectorService application = routingEngine.addApplication("1", processor);
		application.addAcceptor(new MockAcceptor());
		application.start();
		
		// create the connection
		MockConnector receiver = new MockConnector();
		ConnectorService connection = routingEngine.addConnection("1", receiver);
		connection.start();
		
		// send the message
		receiver.produceMessage(new Message());
		
		// wait
		barrier.await(20, TimeUnit.SECONDS);
		
		Assert.assertEquals(1, processor.getCount());
		
		routingEngine.stop();
	}
	
	@Test
	public void testUnroutableConnections() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		
		// a custom message store
		MessageStore messageStore = new MockMessageStore(barrier, Message.STATUS_UNROUTABLE);
		
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.setMessageStore(messageStore);
		routingEngine.start();
		
		// create the processor
		MockProcessor processor = new MockProcessor();
		ConnectorService processorService = routingEngine.addConnection("1", processor);
		processorService.addAcceptor(new Acceptor() {

			@Override
			public boolean accepts(Message message) {
				return false;
			}
			
		});
		processorService.start();
		
		// send the message
		ProducerTemplate producer = routingEngine.getCamelContext().createProducerTemplate();
		producer.sendBody(UriConstants.CONNECTIONS_ROUTER, new Message());
		
		// wait
		barrier.await(3, TimeUnit.SECONDS);
		
		Assert.assertEquals(0, processor.getCount());
		
		routingEngine.stop();
	}
	
	@Test
	public void testUnroutableApplications() throws Exception {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		
		// a custom message store
		MessageStore messageStore = new MockMessageStore(barrier, Message.STATUS_UNROUTABLE);
		
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.setMessageStore(messageStore);
		routingEngine.start();
		
		// create the processor
		MockProcessor processor = new MockProcessor();
		ConnectorService processorService = routingEngine.addConnection("1", processor);
		processorService.addAcceptor(new Acceptor() {

			@Override
			public boolean accepts(Message message) {
				return false;
			}
			
		});
		processorService.start();
		
		// send the message
		ProducerTemplate producer = routingEngine.getCamelContext().createProducerTemplate();
		producer.sendBody(UriConstants.APPLICATIONS_ROUTER, new Message());
		
		// wait
		barrier.await(3, TimeUnit.SECONDS);
		
		Assert.assertEquals(0, processor.getCount());
		
		routingEngine.stop();
	}
	
	@Test
	public void testRetryFailedMessages() throws Exception {
		
		Collection<Message> failedMessages = new ArrayList<Message>();
		
		Message m1 = new Message();
		m1.setStatus(Message.STATUS_FAILED);
		m1.setDirection(Direction.TO_CONNECTIONS);
		failedMessages.add(m1);
		
		Message m2 = new Message();
		m2.setStatus(Message.STATUS_FAILED);
		m2.setDirection(Direction.TO_APPLICATIONS);
		failedMessages.add(m2);
		
		MessageStore messageStore = mock(MessageStore.class);
		when(messageStore.list(any(MessageCriteria.class))).thenReturn(failedMessages);
		
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.setMessageStore(messageStore);
		routingEngine.start();
		
		// create the processors
		Processor connectionProcessor = mock(Processor.class);
		Processor applicationProcessor = mock(Processor.class);
		
		when(connectionProcessor.supports(any(Message.class))).thenReturn(true);
		when(applicationProcessor.supports(any(Message.class))).thenReturn(true);
		
		ConnectorService connectionService = routingEngine.addConnection("1", connectionProcessor);
		ConnectorService applicationService = routingEngine.addApplication("1", applicationProcessor);
		
		Acceptor acceptor = new Acceptor() {

			@Override
			public boolean accepts(Message message) {
				return true;
			}
			
		};
		
		connectionService.addAcceptor(acceptor);
		connectionService.start();
		
		applicationService.addAcceptor(acceptor);
		applicationService.start();
		
		// retry failed messages
		routingEngine.retryFailedMessages();

		// verify
		verify(connectionProcessor, timeout(1500)).process(any(Message.class));
		verify(applicationProcessor, timeout(1500)).process(any(Message.class));
		verify(messageStore, times(4)).saveOrUpdate(any(Message.class));
		
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
	
	protected class MockMessageStore implements MessageStore {
		
		private CyclicBarrier barrier;
		private byte status;
		
		public MockMessageStore(CyclicBarrier barrier, byte status) {
			this.barrier = barrier;
			this.status = status;
		}
		
		@Override
		public Collection<Message> list(MessageCriteria criteria)
				throws StoreException {
			return null;
		}

		@Override
		public void saveOrUpdate(Message message) throws StoreException {
			if (message.getStatus() != status) {
				Assert.fail();
			}
			
			try { barrier.await(); } catch (Exception e) {}
		}

		@Override
		public void updateStatus(MessageCriteria criteria, byte newStatus)
				throws StoreException {}
		
	}
}
