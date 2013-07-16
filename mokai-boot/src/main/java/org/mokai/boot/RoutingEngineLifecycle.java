package org.mokai.boot;

import org.mokai.config.xml.ApplicationsConfiguration;
import org.mokai.config.xml.ConnectionsConfiguration;
import org.mokai.impl.camel.CamelRoutingEngine;

/**
 * This class is configured in the core-context.xml file with dependencies to all the
 * classes that need to be configured before starting the routing engine.
 *
 * @author German Escobar
 */
public class RoutingEngineLifecycle {

	private CamelRoutingEngine routingEngine;

	private ApplicationsConfiguration applicationsConfiguration;
	private ConnectionsConfiguration connectionsConfiguration;

	public void start() {
		connectionsConfiguration.load();
		applicationsConfiguration.load();

		routingEngine.start();
	}

	public void stop() {
		routingEngine.shutdown();
	}

	public void setRoutingEngine(CamelRoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

	public void setApplicationsConfiguration(ApplicationsConfiguration applicationsConfiguration) {
		this.applicationsConfiguration = applicationsConfiguration;
	}

	public void setConnectionsConfiguration(ConnectionsConfiguration connectionsConfiguration) {
		this.connectionsConfiguration = connectionsConfiguration;
	}

}
