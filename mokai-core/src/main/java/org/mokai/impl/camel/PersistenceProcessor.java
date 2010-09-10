package org.mokai.impl.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mokai.Message;
import org.mokai.persist.MessageStore;
import org.mokai.persist.RejectedException;
import org.mokai.persist.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel Processor implementation used by the {@link CamelRoutingEngine} to 
 * save or update messages.
 * 
 * @author German Escobar
 */
public class PersistenceProcessor implements Processor {
	
	private Logger log = LoggerFactory.getLogger(PersistenceProcessor.class);
	
	private MessageStore messageStore;
	
	public PersistenceProcessor(MessageStore messageStore) {
		this.messageStore = messageStore;
	}

	@Override
	public final void process(Exchange exchange) throws Exception {
		Message message = (Message) exchange.getIn().getBody(Message.class);

		try {
			messageStore.saveOrUpdate(message);
		} catch (RejectedException e) {
			log.warn("the message can't be persisted: " + e.getMessage());
		} catch (StoreException e) {
			log.error("StoreException saving a message: " + e.getMessage(), e);
		}
	}

}
