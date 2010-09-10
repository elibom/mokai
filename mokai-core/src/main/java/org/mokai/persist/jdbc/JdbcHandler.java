package org.mokai.persist.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;

/**
 * Abstracts the handling of Jdbc operations from the {@link JdbcMessageStore}
 * allowing users to customize these operations.
 * 
 * @author German Escobar
 */
public interface JdbcHandler {
	
	boolean supportsType(String type);
	
	boolean supportsDirection(Direction direction);
	
	long insertMessage(Connection conn, Message message) throws SQLException;
	
	boolean updateMessage(Connection conn, Message message) throws SQLException;
	
	void updateMessagesStatus(Connection conn, MessageCriteria criteria, Status newStatus) throws SQLException;
	
	Collection<Message> listMessages(Connection conn, MessageCriteria criteria) throws SQLException;
	
}
