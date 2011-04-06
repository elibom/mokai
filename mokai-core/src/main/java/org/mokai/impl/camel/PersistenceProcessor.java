package org.mokai.impl.camel;

import java.util.Date;

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
	
	private ResourceRegistry resourceRegistry;
	
	public PersistenceProcessor(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	@Override
	public final void process(Exchange exchange) throws Exception {
		Message message = (Message) exchange.getIn().getBody(Message.class);

		try {
			MessageStore messageStore = resourceRegistry.getResource(MessageStore.class);
			
			boolean insert = message.getId() == Message.NOT_PERSISTED;
			
			long startTime = new Date().getTime();
			messageStore.saveOrUpdate(message);
			long endTime = new Date().getTime();
			
			if (insert) {
				log.trace("insert message with id " + message.getId() + " took " + (endTime - startTime) + " milis");
			} else {
				log.trace("update message with id " + message.getId() + " took " + (endTime - startTime) + " milis");
			}
			
		} catch (RejectedException e) {
			log.warn("the message can't be persisted: " + e.getMessage());
		} catch (StoreException e) {
			log.error("StoreException saving a message: " + e.getMessage(), e);
		}
	}

}
