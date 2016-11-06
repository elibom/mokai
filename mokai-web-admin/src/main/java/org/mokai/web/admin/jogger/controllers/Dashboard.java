package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import org.json.JSONException;
import org.json.JSONObject;

import org.mokai.Message;
import org.mokai.RoutingEngine;
import org.mokai.persist.MessageCriteria;
import org.mokai.web.admin.jogger.Annotations.Secured;
import org.mokai.web.admin.jogger.helpers.WebUtil;

/**
 * Dashboard controller.
 *
 * @author German Escobar
 * @author Alejandro <lariverosc@gmail.com>
 */
@Secured
public class Dashboard {

    private RoutingEngine routingEngine;

    public void index(Request request, Response response) {
        response.contentType("text/html; charset=UTF-8").render("dashboard.ftl");
    }

    public void data(Request request, Response response) throws JSONException {
        MessageCriteria criteria = new MessageCriteria()
                .addStatus(Message.STATUS_FAILED)
                .addStatus(Message.STATUS_RETRYING);
        int failed = routingEngine.getMessageStore().list(criteria).size();

        criteria = new MessageCriteria().addStatus(Message.STATUS_UNROUTABLE);
        int unroutable = routingEngine.getMessageStore().list(criteria).size();

        JSONObject json = new JSONObject();
        json.put("failedMsgs", failed);
        json.put("unroutableMsgs", unroutable);
        json.put("connections", WebUtil.buildEndpointsJson(routingEngine.getConnections()));
        json.put("applications", WebUtil.buildEndpointsJson(routingEngine.getApplications()));

        response.contentType("text/json; charset=UTF-8").write(json.toString());

    }

    public void setRoutingEngine(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

}
