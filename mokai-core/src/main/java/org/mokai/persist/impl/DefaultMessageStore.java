package org.mokai.persist.impl;

import java.util.Collection;
import java.util.Collections;

import org.mokai.Message;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;

/**
 * Default implementation of a {@link MessageStore} that does nothing and always
 * returns and empty collection of {@link Message}s
 *
 * @author German Escobar
 */
public class DefaultMessageStore implements MessageStore {

	@Override
	public Collection<Message> list(MessageCriteria criteria) {
		return Collections.emptyList();
	}

	@Override
	public void saveOrUpdate(Message message) {

	}

	@Override
	public void updateStatus(MessageCriteria criteria, byte newStatus) {

	}

}
