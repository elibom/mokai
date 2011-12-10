package org.mokai.persist;

import java.util.Collection;

import org.mokai.Message;

/**
 * Responsible of saving, updating and retrieving {@link Message}s. The 
 * persistent mechanism used is defined by the implementation.
 * 
 * @author German Escobar
 */
public interface MessageStore {

	/**
	 * If the message is not persisted, it saves the messages, otherwise
	 * it updates the record. 
	 * 
	 * @param message the {@link Message} we want to save or update.
	 * @throws StoreException if something goes wrong.
	 * @throws RejectedException if the message store refuses to save or 
	 * update the message.
	 */
	void saveOrUpdate(Message message) throws StoreException, RejectedException;
	
	/**
	 * Updates the status of all the messages that matches the criteria with
	 * the newStatus argument.
	 * 
	 * @param criteria the {@link MessageCriteria} used to match the messages.
	 * @param newStatus the new status for the messages. 
	 * @throws StoreException if something goes wrong.
	 */
	void updateStatus(MessageCriteria criteria, byte newStatus) throws StoreException;
	
	/**
	 * Retrieves the messages that matches the criteria.
	 * 
	 * @param criteria the {@link MessageCriteria} used to match the messages.
	 * @return a Collection of {@link Message} objects that matched the criteria or an
	 * empty Collection.
	 * @throws StoreException if something goes wrong.
	 */
	Collection<Message> list(MessageCriteria criteria) throws StoreException;
	
}
