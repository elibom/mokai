package org.mokai.persist.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author German Escobar
 */
public class DirectionJdbcHandler implements JdbcHandler {
	
	private Logger log = LoggerFactory.getLogger(DirectionJdbcHandler.class);
	
	/**
	 * The sql handler for the outbound messages.
	 */
	protected JdbcHandler outboundHandler;
	
	/**
	 * The sql handler for the inbound messages.
	 */
	protected JdbcHandler inboundHandler;
	
	@Override
	public boolean supportsType(String type) {
	
		if (outboundHandler != null && outboundHandler.supportsType(type)) {
			return true;
		}
		
		if (inboundHandler != null && inboundHandler.supportsType(type)) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean supportsDirection(Direction direction) {
		
		if (direction == null) {
			return false;
		}
		
		if (outboundHandler != null && outboundHandler.supportsDirection(direction)) {
			return true;
		}
		
		if (inboundHandler != null && inboundHandler.supportsDirection(direction)) {
			return true;
		}
		
		return false;
	}

	@Override
	public long insertMessage(Connection conn, Message message) throws SQLException, 
			IllegalStateException {
		
		checkHandlersNotNull();
		
		// retrieve the direction of the message
		Direction direction = message.getDirection();
		
		long id = -1;
		
		// generate the statement
		if (direction.equals(Direction.OUTBOUND)) {
			id = outboundHandler.insertMessage(conn, message);
		} else if (direction.equals(Direction.INBOUND)) {
			id = inboundHandler.insertMessage(conn, message);
		} else {
			log.warn("can't save a message with direction: " + direction);
		}
		
		return id;
	}
	
	@Override
	public boolean updateMessage(Connection conn, Message message) throws SQLException, 
			IllegalStateException {
		
		checkHandlersNotNull();
		
		// retrieve the direction of the message
		Direction direction = message.getDirection();
		
		// generate statement and execute
		if (direction.equals(Direction.OUTBOUND)) {
			return outboundHandler.updateMessage(conn, message);			
		} else if (direction.equals(Direction.INBOUND)) {
			return inboundHandler.updateMessage(conn, message);
		} else {
			log.warn("can't save a message with direction: " + direction);
		}
		
		return true;
	}

	@Override
	public void updateMessagesStatus(Connection conn, MessageCriteria criteria, Status newStatus) 
			throws SQLException, IllegalStateException {
		
		checkHandlersNotNull();
		
		Direction direction = null;
		if (criteria != null) {
			direction = criteria.getDirection();
		}
		
		if (direction == null || direction.equals(Direction.OUTBOUND) || direction.equals(Direction.UNKNOWN)) {
			outboundHandler.updateMessagesStatus(conn, criteria, newStatus);
		}
		
		if (direction == null || direction.equals(Direction.INBOUND) || direction.equals(Direction.UNKNOWN)) {
			inboundHandler.updateMessagesStatus(conn, criteria, newStatus);
		}
	}
	
	@Override
	public Collection<Message> listMessages(Connection conn, MessageCriteria criteria) throws SQLException, 
			IllegalStateException {
		
		checkHandlersNotNull();
		
		Collection<Message> messages = new ArrayList<Message>();
		
		// retrieve the direction
		Direction direction = null;
		if (criteria != null) {
			direction = criteria.getDirection();
		}
		
		if (direction == null || direction.equals(Direction.OUTBOUND) || direction.equals(Direction.UNKNOWN)) {
			messages.addAll(outboundHandler.listMessages(conn, criteria));
		}
		
		if (direction == null || direction.equals(Direction.INBOUND) || direction.equals(Direction.UNKNOWN)) {
			messages.addAll(inboundHandler.listMessages(conn, criteria));
		}
		
		return messages;
		
	}
	
	private void checkHandlersNotNull() throws IllegalStateException {
		if (outboundHandler == null) {
			throw new IllegalStateException("no outbound handler specified");
		}
		
		if (inboundHandler == null) {
			throw new IllegalStateException("no inbound handler specified");
		}
	}
	
	/**
	 * Allows to customize the way JDBC operations are handled for  
	 * outbound messages.
	 * 
	 * @param outboundHandler the {@link JdbcHandler} to be used for
	 * handling outbound messages.
	 * @throws IllegalArgumentException if the outboundHandler is null.
	 */
	public void setOutboundJdbcHandler(JdbcHandler outboundHandler)
			throws IllegalArgumentException {
		
		Validate.notNull(outboundHandler);
		
		this.outboundHandler = outboundHandler;
	}

	/**
	 * Allows to customize the way JDBC operations are handled for
	 * inbound messages.
	 * 
	 * @param inboundHandler the {@link JdbcHandler} to be used for 
	 * handling inbound messages.
	 * @throws IllegalArgumentExceptio if the inboundHandler is null.
	 */
	public void setInboundJdbcHandler(JdbcHandler inboundHandler) 
			throws IllegalArgumentException {
		
		Validate.notNull(inboundHandler);
		
		this.inboundHandler = inboundHandler;
	}

}
