package org.mokai.persist.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.persist.MessageCriteria;

/**
 * Abstracts the message operations from the {@link JdbcMessageStore}
 * allowing users to customize them. Besides the normal operations to
 * handle messages (insert, update, etc.), it exposes methods to tell
 * the {@link JdbcMessageStore} that it supports or not a specific
 * type of message.
 *
 * @author German Escobar
 */
public interface MessageHandler {

	/**
	 * Tells whether this handler supports a specific direction of a
	 * {@link Message} or not.
	 *
	 * @param direction the direction to be tested.
	 * @return true if it supports the direction of the message, false
	 * otherwise.
	 * @see Direction
	 */
	boolean supportsDirection(Direction direction);

	/**
	 * Inserts a {@link Message} into the database.
	 *
	 * @param conn the Connection used to insert the message.
	 * @param message the message to be inserted.
	 * @return the generated id of the inserted message.
	 * @throws SQLException if something goes wrong.
	 */
	long insertMessage(Connection conn, Message message) throws SQLException;

	/**
	 * Updates a {@link Message} record from the database.
	 *
	 * @param conn the Connection used to update the message.
	 * @param message the message to be updated.
	 * @return true if the message was found, false otherwise.
	 * @throws SQLException if something goes wrong.
	 */
	boolean updateMessage(Connection conn, Message message) throws SQLException;

	/**
	 * Updates the status of all the messages that matches the criteria.
	 *
	 * @param conn the Connection used to update the status of the messages.
	 * @param criteria the criteria used to select the messages that are
	 * going to be updated.
	 * @param newStatus the new status for the messages.
	 * @throws SQLException if something goes wrong.
	 */
	void updateMessagesStatus(Connection conn, MessageCriteria criteria, byte newStatus) throws SQLException;

	/**
	 * Retrieves the messages that matches the criteria.
	 *
	 * @param conn the Connection used to retrieve the messages.
	 * @param criteria the criteria used to select the messages that are
	 * going to be retrieved.
	 * @return a {@link Collection} of {@link Message} objects.
	 * @throws SQLException if something goes wrong.
	 */
	Collection<Message> listMessages(Connection conn, MessageCriteria criteria) throws SQLException;

}
