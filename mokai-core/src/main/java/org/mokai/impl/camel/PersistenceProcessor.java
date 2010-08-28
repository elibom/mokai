package org.mokai.impl.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mokai.Message;
import org.mokai.persist.MessageStore;

/**
 * Camel Processor implementation used by the {@link CamelRoutingEngine} to 
 * save or update messages.
 * 
 * @author German Escobar
 */
public class PersistenceProcessor implements Processor {
	
	private MessageStore messageStore;
	
	public PersistenceProcessor(MessageStore messageStore) {
		this.messageStore = messageStore;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message message = (Message) exchange.getIn().getBody(Message.class);

		messageStore.saveOrUpdate(message);
	}

}
