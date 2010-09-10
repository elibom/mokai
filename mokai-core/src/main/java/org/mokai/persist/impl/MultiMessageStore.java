package org.mokai.persist.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.mokai.Message;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;

/**
 * A {@link MessageStore} implementation that maps message types to message stores.
 * This is useful if your application is handling different types of messages and
 * each type of message must be persisted independently.
 * 
 * @author German Escobar
 */
public class MultiMessageStore implements MessageStore {
	
	/**
	 * Map of message stores. The key holds the type of the message.
	 */
	private Map<String,MessageStore> messageStores = new HashMap<String,MessageStore>();
	
	/**
	 * The default {@link MessageStore} if no suitable message store is found.
	 */
	private MessageStore defaultMessageStore = new DefaultMessageStore();
	
	/**
	 * Tries to find a {@link MessageStore} for the type of the message. If not found, 
	 * it will use the defaultMessageStore. Finally, it calls the 
	 * {@link MessageStore#saveOrUpdate(Message)} method of the {@link MessageStore}.
	 */
	@Override
	public void saveOrUpdate(Message message) throws StoreException, IllegalArgumentException {
		Validate.notNull(message, "no message provided");
		
		MessageStore messageStore = messageStores.get(message.getType());
		if (messageStore == null) {
			messageStore = defaultMessageStore;
		}
		
		messageStore.saveOrUpdate(message);
	}

	/**
	 * If the {@link MessageCriteria} argument specifies a type, it tries to find
	 * the corresponding {@link MessageStore} and calls the 
	 * {@link MessageStore#updateStatus(MessageCriteria, Status)} method. Otherwise,
	 * it will call the method on all the registered {@link MessageStore}s, 
	 * including the defaultMessageStore.
	 */
	@Override
	public void updateStatus(MessageCriteria criteria, Status newStatus) 
			throws StoreException, IllegalArgumentException {
		
		Validate.notNull(criteria, "no MessageCriteria provided");
		Validate.notNull(newStatus, "no Status provided");
		
		// if the criteria specifies a type, try to find that exact message
		// store to update the status, otherwise, update the status of all
		// the message stores
		if (criteria.getType() != null) {
			
			// if a suitable message store is not found, use the default
			MessageStore messageStore = messageStores.get(criteria.getType());
			if (messageStore == null) {
				messageStore = defaultMessageStore;
			}
			
			messageStore.updateStatus(criteria, newStatus);
			
		} else {
			
			// update the status of all the message stores ...
			for (Map.Entry<String,MessageStore> entry : messageStores.entrySet()) {
				entry.getValue().updateStatus(criteria, newStatus);
			}
			
			// ... including the defaul message store 
			defaultMessageStore.updateStatus(criteria, newStatus);
		}
	}

	/**
	 * If the {@link MessageCriteria} argument specifies a type, it tries to find
	 * the corresponding {@link MessageStore} to retrieve the list of messages.
	 * Otherwise, it will aggregate the messages of all the {@link MessageStore}s.
	 */
	@Override
	public Collection<Message> list(MessageCriteria criteria) throws StoreException {
		
		Validate.notNull(criteria);
		
		// the collection that we will return
		Collection<Message> messages = new ArrayList<Message>();
		
		// if the criteria specifies a type, try to find the suitable message 
		// store or rely on the default message store, otherwise, aggregate
		// the message of all the message stores
		if (criteria.getType() != null) {
		
			MessageStore messageStore = messageStores.get(criteria.getType());
			if (messageStore == null) {
				messageStore = defaultMessageStore;
			}
			
			messages.addAll(messageStore.list(criteria));
			
		} else {
			
			// aggregate the messages of all the messages stores ...
			for (Map.Entry<String,MessageStore> entry : messageStores.entrySet()) {
				messages.addAll(entry.getValue().list(criteria));
			}
			
			// ... including the default message store
			messages.addAll(defaultMessageStore.list(criteria));
			
		}
		
		return messages;
	}

	/**
	 * @return a Map of registered message stores. The key of the map is the type
	 * of the message.
	 */
	public Map<String, MessageStore> getMessageStores() {
		return messageStores;
	}

	/**
	 * Sets the Map of message stores that uses the type of message it handles
	 * as the key.
	 * 
	 * @param messageStores a Map of {@link MessageStore} objects.
	 */
	public void setMessageStores(Map<String, MessageStore> messageStores) {
		this.messageStores = messageStores;
	}
	
	/**
	 * Registers a {@link MessageStore} for the specified type of messages. If
	 * the type already exists, it will be overridden.
	 * 
	 * @param type the type of message that the message store will handle
	 * @param messageStore the {@link MessageStore} implementation.
	 * @throws IllegalArgumentException
	 */
	public void addMessageStore(String type, MessageStore messageStore) 
			throws IllegalArgumentException {
		
		Validate.notEmpty(type);
		Validate.notNull(messageStore);
		
		messageStores.put(type, messageStore);
	}

	/**
	 * The default message store is used if no suitable message store is found.
	 * 
	 * @return a {@link MessageStore} implementation.
	 */
	public MessageStore getDefaultMessageStore() {
		return defaultMessageStore;
	}

	public void setDefaultMessageStore(MessageStore defaultMessageStore)
			throws IllegalArgumentException {
		
		Validate.notNull(defaultMessageStore);
		
		this.defaultMessageStore = defaultMessageStore;
	}

}
