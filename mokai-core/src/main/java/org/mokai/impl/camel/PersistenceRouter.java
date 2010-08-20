package org.mokai.impl.camel;

import org.apache.camel.Exchange;
import org.apache.camel.RecipientList;
import org.mokai.spi.Message;

public class PersistenceRouter {

	@RecipientList
	public String route(Exchange exchange) {
		Message message = exchange.getIn().getBody(Message.class);
		
		if (message.getStatus().equals(Message.Status.PROCESSED)) {
			return "direct:processedmessages";
		}
		
		return "activemq:failedmessages";
	}
}
