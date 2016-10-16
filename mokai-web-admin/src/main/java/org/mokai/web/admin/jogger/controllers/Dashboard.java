package org.mokai.web.admin.jogger.controllers;

import org.mokai.web.admin.jogger.helpers.HelperUI;
import org.mokai.web.admin.jogger.helpers.ConnectorPresenter;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mokai.Message;
import org.mokai.RoutingEngine;
import org.mokai.persist.MessageCriteria;
import org.mokai.web.admin.jogger.Annotations.Secured;

/**
 * Dashboard controller.
 *
 * @author German Escobar
 */
@Secured
public class Dashboard {

	private RoutingEngine routingEngine;

	public void index(Request request, Response response) {
		List<ConnectorPresenter> connections = HelperUI.buildConnectorUIs( routingEngine.getConnections() );
		List<ConnectorPresenter> applications = HelperUI.buildConnectorUIs( routingEngine.getApplications() );

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
