package org.mokai.web.admin.jogger;

import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.ConnectorService;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.impl.camel.ConnectorServiceChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class AsyncConnectorServiceChangeListener implements ConnectorServiceChangeListener {

    private final Logger log = LoggerFactory.getLogger(AsyncConnectorServiceChangeListener.class);

    @Override
    public void changed(ConnectorService connectorService, Message.Direction direction) {
        try {
            JSONObject json = new JSONObject().put("eventType", getEventType(direction));
            json.put("data", new JSONObject()
                    .put("id", connectorService.getId())
                    .put("state", connectorService.getState().name())
                    .put("status", connectorService.getStatus().name())
                    .put("queued", connectorService.getNumQueuedMessages())
            );
            log.info(json.toString());
        } catch (JSONException e) {
            log.error("JSONException notifying connector service change: " + e.getMessage(), e);
        }
    }

    private String getEventType(Direction direction) {
        if (direction.equals(Direction.TO_CONNECTIONS)) {
            return "CONNECTION_CHANGED";
        } else if (direction.equals(Direction.TO_APPLICATIONS)) {
            return "APPLICATION_CHANGED";
        }
        return "UNKNWON";
    }

}
