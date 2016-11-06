package org.mokai.web.admin.jogger.controllers;

import org.mokai.web.admin.jogger.helpers.MessagePresenter;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.RoutingEngine;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.web.admin.jogger.Annotations.Secured;

@Secured
public class Messages {

    private RoutingEngine routingEngine;

    public void connections(Request request, Response response) throws JSONException {
        String to = request.getParameter("recipient");
        Byte[] status = parseStatusParameter(request.getParameter("status"));
        int numRecords = parseNumRecords(request.getParameter("numRecords"));

        Collection<Message> messages = listMessages(Direction.TO_CONNECTIONS, "to", to, status, numRecords);

        boolean htmlResponse = request.getHeader("Accept").contains("text/html");
        if (htmlResponse) {
            Map<String, Object> root = new HashMap<String, Object>();
            root.put("messages", convertMessages(messages));

            response.contentType("text/html; charset=UTF-8").render("messages.ftl", root);
        } else {
            JSONArray jsonMessages = getJSONMessages(messages);
            response.contentType("application/json; charset=UTF-8").write(jsonMessages.toString());
        }
    }

    public void applications(Request request, Response response) throws JSONException {
        String from = request.getParameter("recipient");
        Byte[] status = parseStatusParameter(request.getParameter("status"));
        int numRecords = parseNumRecords(request.getParameter("numRecords"));

        Collection<Message> messages = listMessages(Direction.TO_APPLICATIONS, "from", from, status, numRecords);

        boolean htmlResponse = request.getHeader("Accept").contains("text/html");
        if (htmlResponse) {
            Map<String, Object> root = new HashMap<String, Object>();
            root.put("messages", convertMessages(messages));

            response.contentType("text/html; charset=UTF-8").render("messages.ftl", root);
        } else {
            JSONArray jsonMessages = getJSONMessages(messages);
            response.contentType("application/json; charset=UTF-8").write(jsonMessages.toString());
        }
    }

    private Byte[] parseStatusParameter(String statusStrParam) {
        if (statusStrParam == null) {
            return new Byte[0];
        }
        String[] statusStrArray = statusStrParam.split(",");
        List<Byte> statusList = new ArrayList<Byte>();
        for (String statusStr : statusStrArray) {
            try {
                statusList.add(Byte.parseByte(statusStr));
            } catch (NumberFormatException nfe) {
            }
        }
        return statusList.toArray(new Byte[0]);
    }

    private int parseNumRecords(String numRecordsStr) {
        final int defaultNumRecords = 1000;
        if (numRecordsStr == null) {
            return defaultNumRecords;
        }
        try {
            return Integer.parseInt(numRecordsStr);
        } catch (Exception e) {
            return defaultNumRecords;
        }
    }

    private Collection<Message> listMessages(Direction direction, String recipientKey, String recipientValue, Byte[] status, int numRecords) {
        MessageCriteria criteria = new MessageCriteria()
                .direction(direction)
                .orderBy("id")
                .orderType(OrderType.DOWNWARDS)
                .numRecords(numRecords == 0 ? 1000 : numRecords);

        if (recipientValue != null) {
            criteria.addProperty(recipientKey, recipientValue);
        }

        if (status != null) {
            criteria.addStatus(status);
        }

        return routingEngine.getMessageStore().list(criteria);
    }

    private List<MessagePresenter> convertMessages(Collection<Message> messages) {
        List<MessagePresenter> uiMessages = new ArrayList<MessagePresenter>();
        for (Message message : messages) {
            uiMessages.add(new MessagePresenter(message));
        }

        return uiMessages;
    }

    private JSONArray getJSONMessages(Collection<Message> messages) throws JSONException {
        JSONArray jsonMessages = new JSONArray();
        for (Message message : messages) {
            jsonMessages.put(getJSONMessage(message));
        }

        return jsonMessages;
    }

    private JSONObject getJSONMessage(Message message) throws JSONException {
        JSONObject jsonMessage = new JSONObject()
                .put("id", message.getId())
                .put("reference", message.getReference())
                .put("source", message.getSource())
                .put("destination", message.getDestination())
                .put("status", message.getStatus())
                .put("creationTime", message.getCreationTime())
                .put("modificationTime", message.getModificationTime());

        Map<String, Object> properties = message.getProperties();
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            jsonMessage.put(property.getKey(), property.getValue());
        }

        return jsonMessage;
    }

    public void setRoutingEngine(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

}
