package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import org.json.JSONException;
import org.mokai.ConnectorService;
import org.mokai.RoutingEngine;
import org.mokai.web.admin.jogger.Annotations;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
@Annotations.Secured
public class Monitoring {

    private RoutingEngine routingEngine;

    public void connections(Request request, Response response) throws JSONException {
        StringBuffer status = new StringBuffer(); // this is what we will return

        for (ConnectorService application : routingEngine.getApplications()) {
            status.append(application.getId())
                    .append("_")
                    .append(application.getStatus())
                    .append("_")
                    .append(application.getState())
                    .append(" ");
        }

        for (ConnectorService connection : routingEngine.getConnections()) {
            status.append(connection.getId())
                    .append("_")
                    .append(connection.getStatus())
                    .append("_")
                    .append(connection.getState())
                    .append(" ");
        }
    }

    public void setRoutingEngine(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

}
