package org.mokai.config.xml;

import java.util.List;

import org.mokai.Connector;
import org.mokai.ConnectorService;

/**
 * Loads applications from a XML file.
 * 
 * @author German Escobar
 */
public class ApplicationsConfiguration extends AbstractConfiguration {

	@Override
	protected String getDefaultPath() {
		return "conf/applications.xml";
	}

	@Override
	public ConnectorService addConnector(String id, Connector connector) {
		return routingEngine.addApplication(id, connector);
	}

	@Override
	protected List<ConnectorService> getConnectors() {
		return routingEngine.getApplications();
	}
	
}
