package org.mokai.web.admin.jogger;

import org.mokai.ConnectorService;
import org.mokai.Message;
import org.mokai.impl.camel.ConnectorServiceChangeListener;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class AsyncConnectorServiceChangeListener implements ConnectorServiceChangeListener{

    @Override
    public void changed(ConnectorService connectorService, Message.Direction direction) {
        
    }

}
