package org.mokai.boot;

import org.mokai.RoutingEngine;
import org.mokai.Service;
import org.mokai.config.xml.ApplicationsConfiguration;
import org.mokai.config.xml.ConnectionsConfiguration;

/**
 * This class is configured in the core-context.xml file with dependencies to all the
 * classes that need to be configured before starting the routing engine. See MOKAI-21 (Jira)
 * for more information.
 * 
 * @author German Escobar
 */
public class RoutingEngineLifecycle {

	private RoutingEngine routingEngine;
	
	private ApplicationsConfiguration applicationsConfiguration;
	private ConnectionsConfiguration connectionsConfiguration;
	
	public void start() {
		connectionsConfiguration.load();
		applicationsConfiguration.load();
		
		if (Service.class.isInstance(routingEngine)) {
			Service service = (Service) routingEngine;
			service.start();
		}
	}
	
	public void stop() {
		if (Service.class.isInstance(routingEngine)) {
			Service service = (Service) routingEngine;
			service.stop();
		}
	}

	public void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

	public void setApplicationsConfiguration(ApplicationsConfiguration applicationsConfiguration) {
		this.applicationsConfiguration = applicationsConfiguration;
	}

	public void setConnectionsConfiguration(ConnectionsConfiguration connectionsConfiguration) {
		this.connectionsConfiguration = connectionsConfiguration;
	}
	
}
