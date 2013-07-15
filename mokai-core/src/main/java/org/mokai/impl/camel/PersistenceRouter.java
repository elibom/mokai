package org.mokai.impl.camel;

import org.apache.camel.Exchange;
import org.apache.camel.RecipientList;
import org.mokai.Message;

/**
 *
 *
 * @author German Escobar
 */
public class PersistenceRouter {

	@RecipientList
	public final String route(Exchange exchange) {
		Message message = exchange.getIn().getBody(Message.class);

		if (message.getStatus() == Message.STATUS_PROCESSED) {
			return "direct:processedmessages";
		}

		return "activemq:failedmessages";
	}
}
