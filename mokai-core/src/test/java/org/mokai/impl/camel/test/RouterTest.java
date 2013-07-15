package org.mokai.impl.camel.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mokai.Acceptor;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.impl.camel.AbstractRouter;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RouterTest {

	/**
	 * Should match a connector service if the following conditions are met:
	 *
	 * 1. The connector implements Processor.
	 * 2. The processor supports the message (i.e. {@link Processor#supports(Message)} returns true)
	 * 3. At least one acceptor accepts the message.
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldReturnUriIfAtLeastOneAcceptorAccepts() throws Exception {
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(true);

		Acceptor a1 = buildAcceptor(true);
		Acceptor a2 = buildAcceptor(false);

		ConnectorService connectorService = buildConnectorService("test1", p1, a1, a2);

		TestRouter router = new TestRouter(Collections.singletonList(connectorService));

		String endpointUri = router.route(new Message());
		Assert.assertEquals(endpointUri, "endpoint-test1");
	}

	@Test
	public void shouldReturnUnroutableUriIfEmptyConnectors() throws Exception {
		TestRouter router = new TestRouter(new ArrayList<ConnectorService>());

		String endpointUri = router.route(new Message());
		Assert.assertEquals(endpointUri, "unroutable");
	}

	@Test
	public void shouldReturnUnroutableUriIfNoProcessors() throws Exception {
		Connector c1 = mock(Connector.class);
		Acceptor a1 = buildAcceptor(true);
		ConnectorService connectorService = buildConnectorService("test1", c1, a1);

		TestRouter router = new TestRouter(Collections.singletonList(connectorService));

		String endpointUri = router.route(new Message());
		Assert.assertEquals(endpointUri, "unroutable");
	}

	/**
	 * Order matters. If two connector services accept a message, the first wins.
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldReturnFirstUriIfMoreThanOneAccepts() throws Exception {
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(true);

		Acceptor a1 = buildAcceptor(true);

		ConnectorService cs1 = buildConnectorService("test1", p1, a1);
		ConnectorService cs2 = buildConnectorService("test2", p1, a1);

		List<ConnectorService> connectorServices = new ArrayList<ConnectorService>();
		connectorServices.add(cs1);
		connectorServices.add(cs2);

		TestRouter router = new TestRouter(connectorServices);

		String endpointUri = router.route(new Message());
		Assert.assertEquals(endpointUri, "endpoint-test1");
	}

	@Test
	public void shouldReturnAcceptedUri() throws Exception {
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(true);

		Acceptor a1 = buildAcceptor(false);
		Acceptor a2 = buildAcceptor(true);

		ConnectorService cs1 = buildConnectorService("test1", p1, a1);
		ConnectorService cs2 = buildConnectorService("test2", p1, a2);

		List<ConnectorService> connectorServices = new ArrayList<ConnectorService>();
		connectorServices.add(cs1);
		connectorServices.add(cs2);

		TestRouter router = new TestRouter(connectorServices);

		String endpointUri = router.route(new Message());
		Assert.assertEquals(endpointUri, "endpoint-test2");
	}

	@Test
	public void shouldReturnUriUsingMessageDestination() throws Exception {
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(true);

		ConnectorService cs1 = buildConnectorService("test1", p1);
		ConnectorService cs2 = buildConnectorService("test2", p1);

		List<ConnectorService> connectorServices = new ArrayList<ConnectorService>();
		connectorServices.add(cs1);
		connectorServices.add(cs2);

		TestRouter router = new TestRouter(connectorServices);

		String endpointUri = router.route(new Message().withDestination("test2"));
		Assert.assertEquals(endpointUri, "endpoint-test2");
	}

	@Test
	public void shouldReturnUnroutableIfMsgDestinationNotFound() throws Exception {
		TestRouter router = new TestRouter(new ArrayList<ConnectorService>());

		String endpointUri = router.route(new Message().withDestination("test2"));
		Assert.assertEquals(endpointUri, "unroutable");
	}

	@Test
	public void shouldReturnUnroutableIfMsgDestinationFoundButNoSupport() throws Exception {
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(false);

		List<ConnectorService> connectorServices = new ArrayList<ConnectorService>();
		ConnectorService cs1 = buildConnectorService("test1", p1);
		connectorServices.add(cs1);

		TestRouter router = new TestRouter(connectorServices);

		String endpointUri = router.route(new Message().withDestination("test1"));
		Assert.assertEquals(endpointUri, "unroutable");
	}

	public ConnectorService buildConnectorService(String id, Connector connector, Acceptor...acceptors) {
		List<Acceptor> lstAcceptors = new ArrayList<Acceptor>();
		for (Acceptor acceptor : acceptors) {
			lstAcceptors.add(acceptor);
		}

		ConnectorService connectorService = mock(ConnectorService.class);
		when(connectorService.getId()).thenReturn(id);
		when(connectorService.getConnector()).thenReturn(connector);
		when(connectorService.getAcceptors()).thenReturn(lstAcceptors);

		return connectorService;
	}

	public Acceptor buildAcceptor(boolean accepts) {
		Acceptor acceptor = mock(Acceptor.class);
		when(acceptor.accepts(any(Message.class))).thenReturn(accepts);

		return acceptor;
	}

	private class TestRouter extends AbstractRouter {

		private List<ConnectorService> connectorServices;

		public TestRouter(List<ConnectorService> connectorServices) {
			this.connectorServices = connectorServices;
		}

		@Override
		protected List<ConnectorService> getConnectorServices() {
			return connectorServices;
		}

		@Override
		protected ConnectorService getConnectorService(String id) {
			for (ConnectorService cs : connectorServices) {
				if (cs.getId().equals(id)) {
					return cs;
				}
			}

			return null;
		}

		@Override
		protected String getUriPrefix() {
			return "endpoint-";
		}

		@Override
		protected String getUnroutableMessagesUri() {
			return "unroutable";
		}

	}

}
