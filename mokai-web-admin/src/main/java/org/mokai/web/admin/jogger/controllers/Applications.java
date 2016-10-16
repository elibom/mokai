package org.mokai.web.admin.jogger.controllers;

import org.mokai.web.admin.jogger.helpers.ConnectorPresenter;
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
 * Applications controller.
 *
 * @author German Escobar
 * @author Alejandro <lariverosc@gmail.com>
 */
@Secured
public class Applications {

    private RoutingEngine routingEngine;

    public void index(Request request, Response response) throws JSONException {
        List<ConnectorService> applications = routingEngine.getApplications();

        JSONArray jsonApplications = new JSONArray();
        for (ConnectorService application : applications) {
            jsonApplications.put(new ConnectorPresenter(application).toJSON());
        }

        response.contentType("application/json").write(jsonApplications.toString());
    }

    public void show(Request request, Response response) throws JSONException {
        String id = request.getPathVariable("id");
        ConnectorService connectorService = routingEngine.getApplication(id);

        if (connectorService == null) {
            response.notFound();
            return;
        }

        JSONObject jsonConnector = WebUtil.getConnectorJSON(connectorService);
        response.contentType("application/json").write(jsonConnector.toString());
    }

    public void start(Request request, Response response) throws JSONException {
        String id = request.getPathVariable("id");
        ConnectorService connectorService = routingEngine.getApplication(id);

        if (connectorService == null) {
            response.notFound();
            return;
        }

        connectorService.start();
    }

    public void stop(Request request, Response response) throws JSONException {
        String id = request.getPathVariable("id");
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
