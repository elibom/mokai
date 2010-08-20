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

import org.mokai.ObjectNotFoundException;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.spi.Message;
import org.mokai.spi.Message.Status;
import org.mokai.spi.message.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcMessageStore implements MessageStore {
	
	private Logger log = LoggerFactory.getLogger(JdbcMessageStore.class);
	
	protected DataSource dataSource;

	@Override
	public void saveOrUpdate(Message message) throws StoreException {

		// only save sms messages
		if (!SmsMessage.class.isInstance(message)) {
			return;
		}
		
		SmsMessage smsMessage = (SmsMessage) message;
		if (smsMessage.getId() == Message.NOT_PERSISTED) {
			save(smsMessage);
		} else {
			update(smsMessage);
		}
		
	}
	
	private void save(SmsMessage smsMessage) throws StoreException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rsKeys = null;
		try {
			conn = dataSource.getConnection();
			
			String strSQL = "INSERT INTO message (to_message, from_message, text_message, " +
					"payload_message, status_message, creation_time) VALUES (?, ?, ?, ?, ?, ?)";
			
			stmt = conn.prepareStatement(strSQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, smsMessage.getTo());
			stmt.setString(2, smsMessage.getFrom());
			stmt.setString(3, smsMessage.getText());
			stmt.setString(4, "");
			stmt.setByte(5, smsMessage.getStatus().getId());
			stmt.setTimestamp(6, new Timestamp(smsMessage.getCreationTime().getTime()));
			stmt.executeUpdate();
			
			rsKeys = stmt.getGeneratedKeys();
			if (rsKeys.next()) {
				long id = rsKeys.getLong(1);
				smsMessage.setId(id);
			}
			
		} catch (SQLException e) {
			throw new StoreException(e);
		}
	}
	
	private void update(SmsMessage smsMessage) throws StoreException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			
			String strSQL = "UPDATE message SET status_message = ? WHERE id_message = ?";
			
			stmt = conn.prepareStatement(strSQL);
			stmt.setByte(1, smsMessage.getStatus().getId());
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
	public Collection<Message> list(MessageCriteria criteria) throws StoreException {
		
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
						
						params.put(i+1, st.getId());
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
				
				strSQL += " LIMIT " + firstRecord + "," + numRecords;
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
				SmsMessage message = new SmsMessage();
				message.setId(rs.getLong("id_message"));
				message.setStatus(Status.getSatus(rs.getByte("status_message")));
				message.setTo(rs.getString("to_message"));
				message.setFrom(rs.getString("from_message"));
				message.setText(rs.getString("text_message"));
				
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
	
	public String addOperator(boolean existsCriteria) {
		String ret = "";
		if (!existsCriteria) {
			ret = " WHERE";
		} else {
			ret = " AND";
		}
		
		return ret;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}	
	
}
