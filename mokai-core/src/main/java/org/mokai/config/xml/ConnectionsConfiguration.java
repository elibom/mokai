package org.mokai.config.xml;

import java.util.List;

import org.mokai.Connector;
import org.mokai.ConnectorService;

/**
 * Loads connections from an XML configuration file.
 *
 * @author German Escobar
 */
public class ConnectionsConfiguration extends AbstractConfiguration {

	@Override
	protected String getDefaultPath() {
		return "conf/connections.xml";
	}

	@Override
	protected ConnectorService addConnector(String id, Connector connector) {
		return routingEngine.addConnection(id, connector);
	}

	@Override
	protected List<ConnectorService> getConnectors() {
		return routingEngine.getConnections();
	}

}
