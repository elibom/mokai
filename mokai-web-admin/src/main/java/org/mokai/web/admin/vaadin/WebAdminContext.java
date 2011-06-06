package org.mokai.web.admin.vaadin;

import org.mokai.RoutingEngine;

public class WebAdminContext {

	private static WebAdminContext instance;
	
	private RoutingEngine routingEngine;
	
	private WebAdminContext() {
		
	}
	
	public static WebAdminContext getInstance() {
		if (instance == null) {
			instance = new WebAdminContext();
		}
		
		return instance;
	}

	public RoutingEngine getRoutingEngine() {
		return routingEngine;
	}

	public void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}
	
}
