package org.mokai.persist.jdbc.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.util.log.Log;
import org.mokai.Message;
import org.mokai.Message.DestinationType;
import org.mokai.Message.Direction;
import org.mokai.Message.SourceType;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.persist.jdbc.JdbcHelper;
import org.mokai.persist.jdbc.MessageHandler;

/**
 * A {@link MessageHandler} implementation that supports messages with 
 * type {@link Message#SMS_TYPE} and {@link Direction#OUTBOUND}.
 * 
 * @author German Escobar
 */
public class OutboundSmsHandler implements MessageHandler {
	
	public static final String DEFAULT_TABLENAME = "OUTBOUND_SMS";
	
	private String tableName = DEFAULT_TABLENAME;
	
	@Override
	public final boolean supportsType(String type) {
		
		if (type != null && type.equals(Message.SMS_TYPE)) {
			return true;
		}
		
		return false;
	}

	@Override
	public final boolean supportsDirection(Direction direction) {
		
		if (direction != null && direction.equals(Direction.OUTBOUND)) {
			return true;
		}
		
		return false;
	}

	@Override
	public final long insertMessage(Connection conn, Message message) throws SQLException {
		
		// create the SQL
		String strSQL = "INSERT INTO " + tableName + " (" +
			"account, " +
			"reference, " +
			"source, " +
			"sourcetype, " +
			"destination, " +
			"destinationtype, " +
			"status, " +
			"smsc_to, " +
			"smsc_from, " +
			"smsc_text, " +
			"smsc_messageid, " +
			"smsc_commandstatus, " +
			"smsc_receiptstatus, " +
			"smsc_receipttime, " +
			"creation_time) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// create the prepared statement
		PreparedStatement stmt = conn.prepareStatement(strSQL, Statement.RETURN_GENERATED_KEYS);

		// populate the prepared statement
		populateInsertStatement(stmt, message);

		// execute the statement
		stmt.executeUpdate();
		
		// retrieve and return the generated id
		return JdbcHelper.retrieveGeneratedId(stmt);
	}
	
	private void populateInsertStatement(PreparedStatement stmt, Message message) throws SQLException {
		
		stmt.setString(1, message.getAccountId());
		stmt.setString(2, message.getReference());
		stmt.setString(3, message.getSource());
		stmt.setByte(4, message.getSourceType().value());
		stmt.setString(5, message.getDestination());
		stmt.setByte(6, message.getDestinationType().value());
		stmt.setByte(7, message.getStatus().value());
		stmt.setString(8, message.getProperty("to", String.class));
		stmt.setString(9, message.getProperty("from", String.class));
		stmt.setString(10, message.getProperty("text", String.class));
		stmt.setString(11, message.getProperty("messageId", String.class));
		
		Integer commandStatus = message.getProperty("commandStatus", Integer.class);
		stmt.setInt(12, commandStatus == null ? 0 : commandStatus);
		
		stmt.setString(13, message.getProperty("receiptStatus", String.class));
		
		Date receiptTime = message.getProperty("receiptTime", Date.class);
		stmt.setTimestamp(14, receiptTime != null ? new Timestamp(receiptTime.getTime()) : null);
		
		stmt.setTimestamp(15, new Timestamp(message.getCreationTime().getTime()));
	}
	
	@Override
	public final boolean updateMessage(Connection conn, Message message) throws SQLException {
		
		String strSQL = "UPDATE " + tableName + " SET " +
				"status = ?, " +
				"destination = ?, " +
				"destinationtype = ?, " +
				"smsc_commandstatus = ?, " +
				"smsc_messageid = ?, " +
				"smsc_receiptstatus = ?, " +
				"smsc_receipttime = ?, " +
				"modification_time = ? " +
				"WHERE id = ?";
		
		PreparedStatement stmt = conn.prepareStatement(strSQL);
		
		stmt.setByte(1, message.getStatus().value());
		stmt.setString(2, message.getDestination());
		stmt.setByte(3, message.getDestinationType().value());
		
		Integer commandStatus = message.getProperty("commandStatus", Integer.class);
		stmt.setInt(4, commandStatus == null ? 0 : commandStatus);
		
		stmt.setString(5, message.getProperty("messageId", String.class));
		stmt.setString(6, message.getProperty("receiptStatus", String.class));
		
		Date receiptTime = message.getProperty("receiptTime", Date.class);
		if (receiptTime != null) {
			stmt.setTimestamp(7, new Timestamp(receiptTime.getTime()));
		} else {
			stmt.setTimestamp(7, null);
		}
		
		if (message.getModificationTime() != null) {
			stmt.setTimestamp(8, new Timestamp(message.getModificationTime().getTime()));
		} else {
			stmt.setTimestamp(8, null);
		}
			
		stmt.setLong(9, message.getId());
		
		
		int affected = stmt.executeUpdate();
		
		if (affected == 0) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public final void updateMessagesStatus(Connection conn, MessageCriteria criteria, 
			Status newStatus) throws SQLException {
		
		List<Object> params = new ArrayList<Object>();
		
		String strSQL = "UPDATE " + tableName + " SET status = ?";		
		strSQL += addCommonCriteria(criteria, params);
		
		PreparedStatement stmt = conn.prepareStatement(strSQL);
		
		stmt.setByte(1, newStatus.value());
		
		if (!params.isEmpty()) {
			int index = 2;
			for (Object param : params) {
				stmt.setObject(index, param);
				index++;
			}
		}
		
		stmt.executeUpdate();
	}

	@Override
	public final Collection<Message> listMessages(Connection conn, MessageCriteria criteria) throws SQLException {
		
		List<Object> params = new ArrayList<Object>(); 
		
		String strSQL = "SELECT * FROM " + tableName;
		strSQL += addCommonCriteria(criteria, params);
		Log.debug(strSQL);
		
		PreparedStatement stmt = conn.prepareStatement(strSQL);
		
		if (!params.isEmpty()) {
			int index = 1;
			for (Object param : params) {
				stmt.setObject(index, param);
				index++;
			}
		}
		
		ResultSet rs = stmt.executeQuery();
		
		return createMessages(rs);
	}
	
	private String addCommonCriteria(MessageCriteria criteria, List<Object> params) {
		
		String strSQL = "";
		
		if (criteria != null) {
			boolean existsCriteria = false;
			
			// status
			List<Status> status = criteria.getStatus();
			if (status != null && !status.isEmpty()) {
				strSQL += addOperator(existsCriteria);
				
				boolean existsStatusCriteria = false;
				for (Status st : status) {
					if (!existsStatusCriteria) {
						strSQL += " status = ?";
					} else {
						strSQL += " or status = ?";
					}
					
					params.add(st.value());
					existsStatusCriteria = true;
					
					existsCriteria = true;
				}
			}
			
			// destination
			if (criteria.getDestination() != null) {
				strSQL += addOperator(existsCriteria);
				
				strSQL += " destination = ?";
				params.add(criteria.getDestination());
				
				existsCriteria = true;
			}
			
			// destination type
			if (criteria.getDestinationType() != null) {
				strSQL += addOperator(existsCriteria);
				
				strSQL += " destinationtype = ?";
				params.add(criteria.getDestinationType().value());
				
				existsCriteria = true;
			}
			
			// add additional properties
			for (Map.Entry<String,Object> entry : criteria.getProperties().entrySet()) {
				strSQL += addOperator(existsCriteria);
				
				strSQL += " " + entry.getKey() + " = ?";
				params.add(entry.getValue());
				
				existsCriteria = true;
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
			
		}
		
		return strSQL;
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
	
	private Collection<Message> createMessages(ResultSet rs) throws SQLException {
		
		Collection<Message> messages = new ArrayList<Message>();
		
		while (rs.next()) {
			Message message = new Message(Message.SMS_TYPE);
			
			message.setDirection(Message.Direction.OUTBOUND);
			
			message.setId(rs.getLong("id"));
			message.setAccountId(rs.getString("account"));
			message.setReference(rs.getString("reference"));
			message.setSource(rs.getString("source"));
			message.setSourceType(SourceType.getSourceType(rs.getByte("sourcetype")));
			message.setDestination(rs.getString("destination"));
			message.setDestinationType(DestinationType.getDestinationType(rs.getByte("destinationtype")));
			message.setStatus(Status.getSatus(rs.getByte("status")));
			
			message.setProperty("to", rs.getString("smsc_to"));
			message.setProperty("from", rs.getString("smsc_from"));
			message.setProperty("text", rs.getString("smsc_text"));
			message.setProperty("messageId", rs.getString("smsc_messageid"));
			message.setProperty("commandStatus", rs.getInt("smsc_commandstatus"));
			message.setProperty("receiptStatus", rs.getString("smsc_receiptstatus"));
			message.setProperty("receiptTime", rs.getTimestamp("smsc_receipttime"));
			
			message.setCreationTime(rs.getTimestamp("creation_time"));
			message.setModificationTime(rs.getTimestamp("modification_time"));
			
			messages.add(message);
		}
		
		return messages;
	}

	public final String getTableName() {
		return tableName;
	}

	public final void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
}
