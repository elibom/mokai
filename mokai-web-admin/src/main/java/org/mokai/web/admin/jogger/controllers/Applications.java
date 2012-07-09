package org.mokai.web.admin.jogger.controllers;

import java.util.List;

import org.jogger.http.Request;
import org.jogger.http.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.ConnectorService;
import org.mokai.RoutingEngine;

/**
 * Applications controller.
 * 
 * @author German Escobar
 */
public class Applications {
	
	private RoutingEngine routingEngine;

	public void index(Request request, Response response) throws JSONException {
		List<ConnectorService> applications = routingEngine.getApplications();
		
		JSONArray jsonApplications = new JSONArray();
		for (ConnectorService application : applications) {
			jsonApplications.put( new ConnectorUI(application).toJSON() );
		}
		
		response.contentType("application/json").print(jsonApplications.toString());
	}
	
	public void show(Request request, Response response) throws JSONException {
		String id = request.getPathVariable("id").asString();
		ConnectorService connectorService = routingEngine.getApplication(id);
		
		if (connectorService == null) { 
			response.notFound();
			return;
		}
		
		JSONObject jsonConnector = HelperUI.getConnectorJSON(connectorService);
		response.contentType("application/json").print(jsonConnector.toString());
	}
	
	public void start(Request request, Response response) throws JSONException {
		
		String id = request.getPathVariable("id").asString();
		ConnectorService connectorService = routingEngine.getApplication(id);
		
		if (connectorService == null) { 
			response.notFound();
			return;
		}
		
		connectorService.start();
	}
	
	public void stop(Request request, Response response) throws JSONException {
		
		String id = request.getPathVariable("id").asString();
		ConnectorService connectorService = routingEngine.getApplication(id);
		
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
