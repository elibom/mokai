package org.mokai.persist.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.ObjectNotFoundException;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.RejectedException;
import org.mokai.persist.StoreException;

/**
 * <p>An implementation of a {@link MessageStore} used to persist messages 
 * in a relational database. It uses a {@link MessageHandler} to abstract 
 * the way messages are inserted, updated, etc., allowing customization in 
 * the way in which messages are persisted (one table per message, multiple 
 * tables per message, one big table, etc.).</p>
 * 
 * @author German Escobar
 */
public class JdbcMessageStore implements MessageStore {
	
	/**
	 * The sql handler for sms messages.
	 */
	protected MessageHandler handler;
	
	/**
	 * The datasource to create the connections to the db.
	 */
	protected DataSource dataSource;

	/**
	 * Checks if the handler supports the messages by calling the 
	 * {@link MessageHandler#supportsType(String)} and 
	 * {@link MessageHandler#supportsDirection(Direction)} methods.
	 * If the handler supports the message, it delegates the operation to 
	 * the handler: {@link MessageHandler#insertMessage(Connection, Message)}
	 * if the message has not been persisted, or, 
	 * {@link MessageHandler#updateMessage(Connection, Message)} if it is
	 * already persisted. 
	 * 
	 * @throws StoreException wraps any underlying exception from the database.
	 * @throws RejectedException if the handler doesn't supports the message.
	 * @throws IllegalStateException if the dataSource is null.
	 * @throws IllegalArgumentException if the message is null
	 * @see Message#NOT_PERSISTED
	 */
	@Override
	public final void saveOrUpdate(Message message) throws StoreException, RejectedException,
			ObjectNotFoundException, IllegalStateException, IllegalArgumentException {
		
		// validations
		checkDataSourceNotNull();
		Validate.notNull(message);
		
		if (message.getId() == Message.NOT_PERSISTED) {
			save(message);
		} else {
			update(message);
		}
		
	}
	
	/**
	 * Helper method to insert a message.
	 * 
	 * @param message the message to be inserted.
	 * @throws StoreException wraps any underlying exception from the database.
	 * @throws RejectedException if the handler doesn't supports the message.
	 * @see #saveOrUpdate(Message)
	 */
	private void save(Message message) throws StoreException, RejectedException {
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			
			Direction direction = message.getDirection();
			
			// check if the handler supports the message
			if (handler.supportsDirection(direction)) {
				
				long id = handler.insertMessage(conn, message);				
				message.setId(id);

			} else {
				
				throw new RejectedException("this message store doesn't supports direction '" + direction + "'");
			}
			
		} catch (SQLException e) {
			throw new StoreException(e);
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}
	
	/**
	 * Helper method to update a message.
	 * 
	 * @param message the message to be updated
	 * @throws StoreException wraps any underlying exception from the database.
	 * @throws RejectedException if the handler doesn't supports the message.
	 * @throws ObjectNotFoundException if the message was not found.
	 */
	private void update(Message message) throws StoreException, RejectedException, 
			ObjectNotFoundException {
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			
			Direction direction = message.getDirection();
			
			// check if the handler supports the message
			if (handler.supportsDirection(direction)) {
			
				boolean found = handler.updateMessage(conn, message);
				if (!found) {
					throw new ObjectNotFoundException("message with id " + message.getId() + " not found");
				}
				
			} else {
				
				throw new RejectedException("this message store doesn't supports direction '" + direction + "'");
			}
			
		} catch (SQLException e) {
			throw new StoreException(e);
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
	}
	
	@Override
	public final void updateStatus(MessageCriteria criteria, byte newStatus) throws StoreException, 
			IllegalStateException {
		
		checkDataSourceNotNull();
		Validate.notNull(newStatus);
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			
			// check if the jdbcHandler supports the criteria
			boolean supports = supports(handler, criteria);
			
			if (supports) {
				handler.updateMessagesStatus(conn, criteria, newStatus);
			}
			
		} catch (SQLException e) {
			throw new StoreException(e);
		} finally {

			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
		
	}

	@Override
	public final Collection<Message> list(MessageCriteria criteria) throws StoreException, 
			IllegalStateException {
		
		checkDataSourceNotNull();
		
		Connection conn = null;
		
		try {
			
			conn = dataSource.getConnection();
			
			// check if the handler supports the criteria
			boolean supports = supports(handler, criteria);
			
			Collection<Message> ret = null;		
			if (supports) {
				ret = handler.listMessages(conn, criteria);
			}
			
			if (ret == null) {
				ret = new ArrayList<Message>();
			}
			
			return ret;
			
		} catch (SQLException e) {
			throw new StoreException(e);
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
		
	}
	
	/**
	 * Helper method to check if the {@link MessageHandler} supports the type
	 * and the direction specified in the criteria. If the type and/or
	 * direction are not set in the criteria, the handler supports them.
	 *  
	 * @param handler the message handler
	 * @param criteria the {@link MessageCriteria} that holds the type and 
	 * the direction.
	 * @return true if the handler supports the criteria, false otherwise.
	 */
	private boolean supports(MessageHandler handler, MessageCriteria criteria) {
		
		boolean supports = true;
		
		if (criteria != null && criteria.getDirection() != null) {
			supports = handler.supportsDirection(criteria.getDirection());
		}
		
		return supports;
	}
	
	/**
	 * Helper method to check that the dataSource is not null.
	 * 
	 * @throws IllegalStateException if the dataSource is null.
	 */
	private void checkDataSourceNotNull() throws IllegalStateException {
		if (dataSource == null) {
			throw new IllegalStateException();
		}
	}

	public final void setMessageHandler(MessageHandler handler) throws IllegalArgumentException {
		Validate.notNull(handler);
		
		this.handler = handler;
	}

	/**
	 * @param dataSource
	 * @throws IllegalArgumentException if the dataSource is null.
	 */
	public final void setDataSource(DataSource dataSource) 
			throws IllegalArgumentException {
		
		Validate.notNull(dataSource);
		
		this.dataSource = dataSource;
	}	
	
}
