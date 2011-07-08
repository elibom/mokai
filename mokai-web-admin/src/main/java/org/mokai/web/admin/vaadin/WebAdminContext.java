package org.mokai.web.admin.vaadin;

import org.mokai.RoutingEngine;
import org.mokai.web.admin.AdminPasswordStore;
import org.mokai.web.admin.DefaultAdminPasswordStore;

public class WebAdminContext {

	private static WebAdminContext instance;
	
	private RoutingEngine routingEngine;
	
	private AdminPasswordStore adminPasswordStore = new DefaultAdminPasswordStore();
	
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

	public AdminPasswordStore getAdminPasswordStore() {
		return adminPasswordStore;
	}

	public void setAdminPasswordStore(AdminPasswordStore adminPasswordStore) {
		this.adminPasswordStore = adminPasswordStore;
	}
	
}
