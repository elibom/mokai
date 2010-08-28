package org.mokai.persist.impl;

import java.util.Collection;
import java.util.Collections;

import org.mokai.Message;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;

/**
 * Default implementation of a {@link MessageStore} that does nothing and always
 * return and empty collection of {@link Message}s
 * 
 * @author German Escobar
 */
public class DefaultMessageStore implements MessageStore {
	
	@Override
	public void saveOrUpdate(Message message) throws StoreException {
		
	}

	@Override
	public Collection<Message> list(MessageCriteria criteria)
			throws StoreException {
		return Collections.emptyList();
	}

}
