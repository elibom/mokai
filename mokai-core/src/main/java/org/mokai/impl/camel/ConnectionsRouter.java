package org.mokai.impl.camel;

import java.util.List;

import org.mokai.ConnectorService;

/**
 * Decides which connection will handle a message. It is used in the {@link CamelRoutingEngine} class.
 * 
 * @author German Escobar
 */
public class ConnectionsRouter extends AbstractRouter {

	@Override
	protected List<ConnectorService> getConnectorServices() {
		return routingEngine.getConnections();
	}

	@Override
	protected String getUriPrefix() {
		return "activemq:connection-";
	}

	@Override
	protected String getUnroutableMessagesUri() {
		return UriConstants.CONNECTIONS_UNROUTABLE_MESSAGES;
	}
	
	
}
