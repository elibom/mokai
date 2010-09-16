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
import org.mokai.persist.RejectedException;

/**
 * <p>A {@link MessageHandler} implementation useful when we want to split inbound
 * and outbound messages in different tables. It uses two {@link MessageHandler}
 * implementations: one for handling outbound messages and the other for 
 * handling inbound messages.</p>
 * 
 * <p>A restriction of this handler is that you must provide both inbound and
 * outbound handlers. You cannot leave one of those in null.</p>
 * 
 * @author German Escobar
 */
public class OutboundInboundHandler implements MessageHandler {
	
	/**
	 * The handler for the outbound messages.
	 */
	protected MessageHandler outboundHandler;
	
	/**
	 * The handler for the inbound messages.
	 */
	protected MessageHandler inboundHandler;
	
	/**
	 * Checks if the outbound or inbound handlers supports the type.
	 * 
	 * @throws IllegalStateException if at least one of the handlers is null.
	 */
	@Override
	public final boolean supportsType(String type) throws IllegalStateException {
		
		checkHandlersNotNull();
	
		// check if the outbound handler supports the type
		if (outboundHandler != null && outboundHandler.supportsType(type)) {
			return true;
		}
		
		// check if the inbound handler supports the type
		if (inboundHandler != null && inboundHandler.supportsType(type)) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Returns true if the direction is {@link Direction#INBOUND} or 
	 * {@link Direction#OUTBOUND}. False otherwise.
	 * 
	 * @throws IllegalStateException if at least one of the handlers is null.
	 */
	@Override
	public final boolean supportsDirection(Direction direction) throws IllegalStateException {
		
		checkHandlersNotNull();
		
		// we dont support null directions
		if (direction == null) {
			return false;
		}
		
		// check if the outbound handler supports the direction
		if (direction.equals(Direction.OUTBOUND)) {
			return true;
		}
		
		if (direction.equals(Direction.INBOUND)) {
			return true;
		}
		
		return false;
	}

	/**
	 * If the direction of the message is {@link Direction#OUTBOUND}, it uses the
	 * outbound handler. If the direction is {@link Direction#INBOUND}, it uses 
	 * the inbound handler.
	 * 
	 * @throws RejectedException if the direction of the messages is not outbound
	 * or inbound.
	 * @throws IllegalStateException if at least one of the handlers is null.
	 */
	@Override
	public final long insertMessage(Connection conn, Message message) throws SQLException, 
			RejectedException, IllegalStateException {
		
		checkHandlersNotNull();
		
		// retrieve the direction of the message
		Direction direction = message.getDirection();
		
		long id = -1;
		
		// insert the message using one of the handlers
		if (direction.equals(Direction.OUTBOUND)) {
			id = outboundHandler.insertMessage(conn, message);
		} else if (direction.equals(Direction.INBOUND)) {
			id = inboundHandler.insertMessage(conn, message);
		} else {
			throw new RejectedException("can't save a message with direction: " + direction);
		}
		
		return id;
	}
	
	/**
	 * If the direction of the message is {@link Direction#OUTBOUND}, it uses the
	 * outbound handler. If the direction is {@link Direction#INBOUND}, it uses 
	 * the inbound handler.
	 * 
	 * @throws RejectedException if the direction of the messages is not outbound
	 * or inbound.
	 * @throws IllegalStateException if at least one of the handlers is null.
	 */
	@Override
	public final boolean updateMessage(Connection conn, Message message) throws SQLException, 
			RejectedException, IllegalStateException {
		
		checkHandlersNotNull();
		
		// retrieve the direction of the message
		Direction direction = message.getDirection();
		
		// generate statement and execute
		if (direction.equals(Direction.OUTBOUND)) {
			return outboundHandler.updateMessage(conn, message);			
		} else if (direction.equals(Direction.INBOUND)) {
			return inboundHandler.updateMessage(conn, message);
		} else {
			throw new RejectedException("can't save a message with direction: " + direction);
		}

	}

	/**
	 * If the direction is null, it updates the status in both handlers, otherwise, 
	 * if the direction is {@link Direction#OUTBOUND}, it uses only the outbound 
	 * handler, if it is {@link Direction#INBOUND}, it uses only the inbound handler.
	 * 
	 * @throws IllegalStateException if at least one of the handlers is null.
	 */
	@Override
	public final void updateMessagesStatus(Connection conn, MessageCriteria criteria, Status newStatus) 
			throws SQLException, IllegalStateException {
		
		checkHandlersNotNull();
		
		Direction direction = null;
		if (criteria != null) {
			direction = criteria.getDirection();
		}
		
		if (direction == null || direction.equals(Direction.OUTBOUND)) {
			outboundHandler.updateMessagesStatus(conn, criteria, newStatus);
		}
		
		if (direction == null || direction.equals(Direction.INBOUND)) {
			inboundHandler.updateMessagesStatus(conn, criteria, newStatus);
		}
	}
	
	/**
	 * If the direction is null, it retrieves the messages from both handlers, 
	 * otherwise, if the direction is {@link Direction#OUTBOUND}, it retrieves the
	 * messages only from the outbound handler, and if it is 
	 * {@link Direction#INBOUND}, retrieves only the messages from the inbound
	 * handler.
	 * 
	 * @throws IllegalStateException if at leas one of the handlers is null.
	 */
	@Override
	public final Collection<Message> listMessages(Connection conn, MessageCriteria criteria) throws SQLException, 
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
	
	/**
	 * Helper method to check that the handlers are not null.
	 * 
	 * @throws IllegalStateException if at least one of the handlers is null.
	 */
	private void checkHandlersNotNull() throws IllegalStateException {
		if (outboundHandler == null) {
			throw new IllegalStateException("no outbound handler specified");
		}
		
		if (inboundHandler == null) {
			throw new IllegalStateException("no inbound handler specified");
		}
	}
	
	/**
	 * @param outboundHandler the {@link MessageHandler} to be used for
	 * handling outbound messages.
	 * @throws IllegalArgumentException if the outboundHandler is null.
	 */
	public final void setOutboundHandler(MessageHandler outboundHandler)
			throws IllegalArgumentException {
		
		Validate.notNull(outboundHandler);
		
		this.outboundHandler = outboundHandler;
	}

	/**
	 * @param inboundHandler the {@link MessageHandler} to be used for 
	 * handling inbound messages.
	 * @throws IllegalArgumentExceptio if the inboundHandler is null.
	 */
	public final void setInboundHandler(MessageHandler inboundHandler) 
			throws IllegalArgumentException {
		
		Validate.notNull(inboundHandler);
		
		this.inboundHandler = inboundHandler;
	}

}
