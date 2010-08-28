package org.mokai.persist;

import java.util.Collection;

import org.mokai.Message;

/**
 * Implementations of this interface are responsible of saving, updating and
 * retreiving {@link Message}s
 * 
 * @author German Escobar
 */
public interface MessageStore {

	void saveOrUpdate(Message message) throws StoreException;
	
	Collection<Message> list(MessageCriteria criteria) throws StoreException;
	
}
