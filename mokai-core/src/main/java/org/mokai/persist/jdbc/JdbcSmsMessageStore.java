package org.mokai.persist.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.mokai.Message;
import org.mokai.ObjectNotFoundException;
import org.mokai.Message.DestinationType;
import org.mokai.Message.Flow;
import org.mokai.Message.SourceType;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;
import org.mokai.persist.MessageCriteria.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of a {@link MessageStore} used to persist SMS messages in 
 * a relational database.
 * 
 * @author German Escobar
 */
public class JdbcSmsMessageStore implements MessageStore {
	
	private Logger log = LoggerFactory.getLogger(JdbcSmsMessageStore.class);
	
	protected DataSource dataSource;

	@Override
	public final void saveOrUpdate(Message message) throws StoreException {

		// only save sms messages
		if (!message.isType(Message.SMS_TYPE)) {
			return;
		}

		if (message.getId() == Message.NOT_PERSISTED) {
			save(message);
		} else {
			update(message);
		}
		
	}
	
	private void save(Message smsMessage) throws StoreException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rsKeys = null;
		try {
			conn = dataSource.getConnection();
			
			stmt = generateSaveStatement(conn, smsMessage);
			
			stmt.executeUpdate();
			
			rsKeys = stmt.getGeneratedKeys();
			if (rsKeys.next()) {
				long id = rsKeys.getLong(1);
				smsMessage.setId(id);
			}
			
		} catch (SQLException e) {
			throw new StoreException(e);
		} finally {
			if (rsKeys != null) {
				try { rsKeys.close(); } catch (SQLException e) {}
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}
	
	protected PreparedStatement generateSaveStatement(Connection conn, Message smsMessage) throws SQLException {
		String strSQL = "INSERT INTO message (" +
				"account_message, " +
				"reference_message, " +
				"flow_message, " +
				"source_message, " +
				"sourcetype_message, " +
				"destination_message, " +
				"destinationtype_message, " +
				"status_message, " +
				"to_message, " +
				"from_message, " +
				"text_message, " +
				"messageid_message, " +
				"commandstatus_message, " +
				"creation_time) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement stmt = conn.prepareStatement(strSQL, Statement.RETURN_GENERATED_KEYS);
		
		stmt.setString(1, smsMessage.getAccountId());
		stmt.setString(2, smsMessage.getReference());
		stmt.setByte(3, smsMessage.getFlow().value());
		stmt.setString(4, smsMessage.getSource());
		stmt.setByte(5, smsMessage.getSourceType().value());
		stmt.setString(6, smsMessage.getDestination());
		stmt.setByte(7, smsMessage.getDestinationType().value());
		stmt.setByte(8, smsMessage.getStatus().value());
		
		stmt.setString(9, smsMessage.getProperty("to", String.class));
		stmt.setString(10, smsMessage.getProperty("from", String.class));
		stmt.setString(11, smsMessage.getProperty("text", String.class));
		stmt.setString(12, smsMessage.getProperty("messageId", String.class));
		if (smsMessage.getProperty("commandStatus", Integer.class) != null) { 
			stmt.setInt(13, smsMessage.getProperty("commandStatus", Integer.class));
		} else {
			stmt.setInt(13, 0);
		}
		stmt.setTimestamp(14, new Timestamp(smsMessage.getCreationTime().getTime()));
		
		return stmt;
	}
	
	private void update(Message smsMessage) throws StoreException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			
			String strSQL = "UPDATE message SET status_message = ? WHERE id_message = ?";
			
			stmt = conn.prepareStatement(strSQL);
			stmt.setByte(1, smsMessage.getStatus().value());
			stmt.setLong(2, smsMessage.getId());
			
			int affected = stmt.executeUpdate();
			if (affected == 0) {
				throw new ObjectNotFoundException();
			}
			
		} catch (SQLException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public final Collection<Message> list(MessageCriteria criteria) throws StoreException {
		
		List<Message> messages = new ArrayList<Message>();
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String strSQL = "SELECT * FROM message";
			
			Map<Integer,Object> params = new HashMap<Integer,Object>(); 
			
			if (criteria != null) {
				boolean existsCriteria = false;
				
				// status
				List<Status> status = criteria.getStatus();
				if (status != null && !status.isEmpty()) {
					strSQL += addOperator(existsCriteria);
					
					boolean existsStatusCriteria = false;
					int i = 0;
					for (Status st : status) {
						if (!existsStatusCriteria) {
							strSQL += " status_message = ?";
						} else {
							strSQL += " or status_message = ?";
						}
						
						params.put(i+1, st.value());
						existsStatusCriteria = true;
						
						existsCriteria = true;
						i++;
					}
				}
				
				// order by
				if (criteria.getOrderBy() != null && !"".equals(criteria.getOrderBy())) {
					String orderBy = criteria.getOrderBy();
					strSQL += " ORDER BY " + orderBy;
					
					if (criteria.getOrderType() == OrderType.UPWARDS) {
						strSQL += " ASC";
					} else {
						strSQL += " DESC";
					}
				}
				
				// limit
				int firstRecord = criteria.getFirstRecord();
				int numRecords = 500;
				if (criteria.getNumRecords() > 0) {
					numRecords = criteria.getNumRecords();
				}
				
				//strSQL += " LIMIT " + firstRecord + "," + numRecords;
			}
			
			long startTime = new Date().getTime();
			
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement(strSQL);
			
			if (!params.isEmpty()) {
				for (Entry<Integer,Object> entry : params.entrySet()) {
					stmt.setObject(entry.getKey(), entry.getValue());
				}
			}
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				Message message = new Message(Message.SMS_TYPE);
				message.setId(rs.getLong("id_message"));
				message.setAccountId(rs.getString("account_message"));
				message.setReference(rs.getString("reference_message"));
				message.setFlow(Flow.getFlow(rs.getByte("flow_message")));
				message.setSource(rs.getString("source_message"));
				message.setSourceType(SourceType.getSourceType(rs.getByte("sourcetype_message")));
				message.setDestination(rs.getString("destination_message"));
				message.setDestinationType(DestinationType.getDestinationType(rs.getByte("destinationtype_message")));
				message.setStatus(Status.getSatus(rs.getByte("status_message")));
				
				message.setProperty("to", rs.getString("to_message"));
				message.setProperty("from", rs.getString("from_message"));
				message.setProperty("text", rs.getString("text_message"));
				message.setProperty("messageId", rs.getString("messageid_message"));
				message.setProperty("commandStatus", rs.getInt("commandstatus_message"));
				
				message.setCreationTime(rs.getTimestamp("creation_time"));
				
				messages.add(message);
			}
			
			long endTime = new Date().getTime();
			log.debug("list messages took " + (endTime - startTime) + " millis");
			
		} catch (SQLException e) {
			throw new StoreException(e);
		} finally {
			if (rs != null) { 
				try { rs.close(); } catch (Exception e) {}
			}
			if (stmt != null) {
				try { stmt.close(); } catch (Exception e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
		
		return messages;
	}
	
	private String addOperator(boolean existsCriteria) {
		String ret = "";
		if (!existsCriteria) {
			ret = " WHERE";
		} else {
			ret = " AND";
		}
		
		return ret;
	}

	public final void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}	
	
}
