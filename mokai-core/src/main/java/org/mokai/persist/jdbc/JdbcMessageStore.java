package org.mokai.persist.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.mokai.Message;
import org.mokai.ObjectNotFoundException;
import org.mokai.Message.Direction;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.RejectedException;
import org.mokai.persist.StoreException;

/**
 * <p>An implementation of a {@link MessageStore} used to persist messages 
 * in a relational database. </p>
 * 
 * @author German Escobar
 */
public class JdbcMessageStore implements MessageStore {
	
	/**
	 * The sql handler for sms messages.
	 */
	protected JdbcHandler jdbcHandler;
	
	/**
	 * The datasource to create the connections to the db.
	 */
	protected DataSource dataSource;

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
	
	private void save(Message message) throws StoreException, RejectedException {
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			
			String type = message.getType();
			Direction direction = message.getDirection();
			
			// check if the jdbcHandler supports the message
			if (jdbcHandler.supportsType(type) && jdbcHandler.supportsDirection(direction)) {
				long id = jdbcHandler.insertMessage(conn, message);
				
				if (id != -1) {
					message.setId(id);
				} else {
					throw new StoreException(jdbcHandler.getClass().getName() 
							+ ".insertMessage returned -1");
				}
			} else {
				throw new RejectedException("this message store doesn't supports type '" 
						+ type + "' and direction '" + direction + "'");
			}
			
		} catch (SQLException e) {
			throw new StoreException(e);
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}
	
	private void update(Message message) throws StoreException, ObjectNotFoundException {
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			
			String type = message.getType();
			Direction direction = message.getDirection();
			
			// check if the jdbcHandler supports the message
			if (jdbcHandler.supportsType(type) && jdbcHandler.supportsDirection(direction)) {
			
				boolean found = jdbcHandler.updateMessage(conn, message);

				if (!found) {
					throw new ObjectNotFoundException("message with id " + message.getId() + " not found");
				}
			} else {
				throw new RejectedException("this message store doesn't supports type '" 
						+ type + "' and direction '" + direction + "'");
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
	public void updateStatus(MessageCriteria criteria, Status newStatus) throws StoreException, 
			IllegalStateException {
		
		checkDataSourceNotNull();
		Validate.notNull(newStatus);
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			
			// check if the jdbcHandler supports the criteria
			boolean supports = supports(jdbcHandler, criteria);
			
			if (supports) {
				jdbcHandler.updateMessagesStatus(conn, criteria, newStatus);
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
			
			// check if the jdbcHandler supports the criteria
			boolean supports = supports(jdbcHandler, criteria);
			
			Collection<Message> ret = null;		
			if (supports) {
				ret = jdbcHandler.listMessages(conn, criteria);
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
	
	private boolean supports(JdbcHandler jdbcHandler, MessageCriteria criteria) {
		
		boolean supports = true;
		
		if (criteria != null && criteria.getType() != null) {
			supports = jdbcHandler.supportsType(criteria.getType());
		}
		
		if (criteria != null && criteria.getDirection() != null) {
			supports = jdbcHandler.supportsDirection(criteria.getDirection());
		}
		
		return supports;
	}
	
	private void checkDataSourceNotNull() throws IllegalStateException {
		if (dataSource == null) {
			throw new IllegalStateException();
		}
	}

	public void setJdbcHandler(JdbcHandler jdbcHandler) throws IllegalArgumentException {
		Validate.notNull(jdbcHandler);
		
		this.jdbcHandler = jdbcHandler;
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
