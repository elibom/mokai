package org.mokai.persist;

import java.util.Collection;

import org.mokai.spi.Message;

public interface MessageStore {

	void saveOrUpdate(Message message) throws StoreException;
	
	Collection<Message> list(MessageCriteria criteria) throws StoreException;
	
}
