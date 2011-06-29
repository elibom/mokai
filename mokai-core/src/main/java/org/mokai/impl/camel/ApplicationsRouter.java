package org.mokai.impl.camel;

import java.util.List;

import org.mokai.ConnectorService;

/**
 * Decides which application will handle a message. It is used in the {@link CamelRoutingEngine} class.
 * 
 * @author German Escobar
 */
public class ApplicationsRouter extends AbstractRouter {

	@Override
	protected List<ConnectorService> getConnectorServices() {
		return routingEngine.getApplications();
	}

	@Override
	protected String getUriPrefix() {
		return "activemq:application-";
	}

	@Override
	protected String getUnroutableMessagesUri() {
		return UriConstants.APPLICATIONS_UNROUTABLE_MESSAGES;
	}

	
}
