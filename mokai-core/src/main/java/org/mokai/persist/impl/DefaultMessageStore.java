package org.mokai.persist.impl;

import java.util.Collection;

import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;
import org.mokai.spi.Message;

public class DefaultMessageStore implements MessageStore {
	
	@Override
	public void saveOrUpdate(Message message) throws StoreException {
		
	}

	@Override
	public Collection<Message> list(MessageCriteria criteria)
			throws StoreException {
		return null;
	}

}
