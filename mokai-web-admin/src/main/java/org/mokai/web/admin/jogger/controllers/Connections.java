package org.mokai.web.admin.jogger.controllers;

import org.mokai.web.admin.jogger.helpers.EndpointPresenter;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.ConnectorService;
import org.mokai.RoutingEngine;
import org.mokai.web.admin.jogger.Annotations.Secured;
import org.mokai.web.admin.jogger.helpers.WebUtil;

/**
 * Connections controller.
 *
 * @author German Escobar
 * @author Alejandro <lariverosc@gmail.com>
 */
@Secured
public class Connections {

    private RoutingEngine routingEngine;

    public void index(Request request, Response response) throws JSONException {
        List<ConnectorService> connections = routingEngine.getConnections();

        JSONArray jsonConnections = new JSONArray();
        for (ConnectorService connection : connections) {
            jsonConnections.put(new EndpointPresenter(connection).toJSON());
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

        JSONObject jsonConnector = WebUtil.getConnectorJSON(connectorService);
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
        response.write(new JSONObject().put("newState", "STARTED").toString());
    }

    public void stop(Request request, Response response) throws JSONException {
        String id = request.getPathVariable("id");
        ConnectorService connectorService = routingEngine.getConnection(id);

        if (connectorService == null) {
            response.notFound();
            return;
        }

        connectorService.stop();
        response.write(new JSONObject().put("newState", "STOPPED").toString());
    }

    public void setRoutingEngine(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

}
