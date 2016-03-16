package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.ConnectorService;
import org.mokai.RoutingEngine;
import org.mokai.web.admin.jogger.annotations.Secured;

/**
 * Connections controller.
 *
 * @author German Escobar
 */
@Secured
public class Connections {

	private RoutingEngine routingEngine;

	public void index(Request request, Response response) throws JSONException {
		List<ConnectorService> connections = routingEngine.getConnections();

		JSONArray jsonConnections = new JSONArray();
		for (ConnectorService connection : connections) {
			jsonConnections.put( new ConnectorUI(connection).toJSON() );
		}

        response.contentType("application/json").write(jsonConnections.toString());
	}

	public void show(Request request, Response response) throws JSONException {
        String id = request.getPathVariable("id");
		ConnectorService connectorService = routingEngine.getConnection(id);

		if (connectorService == null) {
			response.notFound();
			return;
		}

		JSONObject jsonConnector = HelperUI.getConnectorJSON(connectorService);
        response.contentType("application/json").write(jsonConnector.toString());
	}

	public void start(Request request, Response response) throws JSONException {
        String id = request.getPathVariable("id");
		ConnectorService connectorService = routingEngine.getConnection(id);

		if (connectorService == null) {
			response.notFound();
			return;
		}

		connectorService.start();
	}

	public void stop(Request request, Response response) throws JSONException {
        String id = request.getPathVariable("id");
		ConnectorService connectorService = routingEngine.getConnection(id);

		if (connectorService == null) {
			response.notFound();
			return;
		}

		connectorService.stop();
	}

	public void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

}
