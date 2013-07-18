package org.mokai.web.admin.jogger.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jogger.http.Request;
import org.jogger.http.Response;
import org.mokai.Message;
import org.mokai.RoutingEngine;
import org.mokai.persist.MessageCriteria;
import org.mokai.web.admin.jogger.annotations.Secured;

/**
 * Dashboard controller.
 *
 * @author German Escobar
 */
@Secured
public class Dashboard {

	private RoutingEngine routingEngine;

	public void index(Request request, Response response) {
		List<ConnectorUI> connections = HelperUI.buildConnectorUIs( routingEngine.getConnections() );
		List<ConnectorUI> applications = HelperUI.buildConnectorUIs( routingEngine.getApplications() );

		MessageCriteria criteria = new MessageCriteria()
			.addStatus(Message.STATUS_FAILED)
			.addStatus(Message.STATUS_RETRYING);
		int failed = routingEngine.getMessageStore().list(criteria).size();

		criteria = new MessageCriteria().addStatus(Message.STATUS_UNROUTABLE);
		int unroutable = routingEngine.getMessageStore().list(criteria).size();

		Map<String,Object> root = new HashMap<String,Object>();
		root.put("connections", connections);
		root.put("applications", applications);
		root.put("failedMsgs", failed);
		root.put("unroutableMsgs", unroutable);
		root.put("tab", "dashboard");

		response.render("dashboard.ftl", root);
	}

	public void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

}
