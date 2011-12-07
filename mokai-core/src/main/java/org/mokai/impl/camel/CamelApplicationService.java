package org.mokai.impl.camel;

import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.ExecutionException;
import org.mokai.Message.Direction;

/**
 * Concrete {@link ConnectorService}. Defines the URI's of the endpoints used by applications.
 * 
 * @author German Escobar
 */
public class CamelApplicationService extends AbstractCamelConnectorService {

	/**
	 * Constructor. 
	 * 
	 * @param id
	 * @param connector
	 * @param resourceRegistry
	 * @throws IllegalArgumentException
	 * @throws ExecutionException
	 */
	public CamelApplicationService(String id, Connector connector, ResourceRegistry resourceRegistry) 
			throws IllegalArgumentException, ExecutionException {
		super(id, connector, resourceRegistry);
	}

	@Override
	protected String getOutboundUriPrefix() {
		return "activemq:application-";
	}

	@Override
	protected String getInboundUriPrefix() {
		return "direct:application-";
	}

	@Override
	protected String getProcessedMessagesUri() {
		return UriConstants.APPLICATIONS_PROCESSED_MESSAGES;
	}

	@Override
	protected String getFailedMessagesUri() {
		return UriConstants.APPLICATIONS_FAILED_MESSAGES;
	}

	@Override
	protected String getMessagesRouterUri() {
		return UriConstants.CONNECTIONS_ROUTER;
	}

	@Override
	protected Direction getDirection() {
		return Direction.TO_APPLICATIONS;
	}

}
