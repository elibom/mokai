package org.mokai.impl.camel;

import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.ExecutionException;
import org.mokai.Message.Direction;

/**
 * Concrete {@link ConnectorService}. Defines the URI's of the endpoints used by connections.
 * 
 * @author German Escobar
 */
public class CamelConnectionService extends AbstractCamelConnectorService {

	public CamelConnectionService(String id, Connector connector, ResourceRegistry resourceRegistry) 
			throws IllegalArgumentException, ExecutionException {
		super(id, connector, resourceRegistry);
	}

	@Override
	protected String getOutboundUriPrefix() {
		return "activemq:connection-";
	}

	@Override
	protected String getInboundUriPrefix() {
		return "direct:connection-";
	}

	@Override
	protected String getProcessedMessagesUri() {
		return UriConstants.CONNECTIONS_PROCESSED_MESSAGES;
	}

	@Override
	protected String getFailedMessagesUri() {
		return UriConstants.CONNECTIONS_FAILED_MESSAGES;
	}

	@Override
	protected String getMessagesRouterUri() {
		return UriConstants.APPLICATIONS_ROUTER;
	}
	
	@Override
	protected Direction getDirection() {
		return Direction.TO_CONNECTIONS;
	}
	
}
