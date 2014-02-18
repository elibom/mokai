package org.mokai.persist.jdbc.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.persist.jdbc.JdbcHelper;
import org.mokai.persist.jdbc.MessageHandler;
import org.mokai.persist.jdbc.SqlEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author German Escobar
 */
public abstract class AbstractSmsHandler implements MessageHandler {

	private Logger log = LoggerFactory.getLogger(AbstractSmsHandler.class);

	private SqlEngine sqlEngine;

	private String tableName = getDefaultTableName();

	private static final String[] KNOWN_PROPERTIES = { "to", "from", "text", "sequenceNumber", "messageId", "commandStatus", "receiptStatus", "receiptTime" };

	@Override
	public final long insertMessage(Connection conn, Message message) throws SQLException {
		// create the SQL
		final String strSQL = "INSERT INTO " + tableName + " (" +
			"reference, " +
			"source, " +
			"destination, " +
			"status, " +
			"smsc_to, " +
			"smsc_from, " +
			"smsc_text, " +
			"smsc_sequencenumber, " +
			"smsc_messageid, " +
			"smsc_commandstatus, " +
			"smsc_receiptstatus, " +
			"smsc_receipttime, " +
			"other, " +
			"creation_time) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(strSQL, Statement.RETURN_GENERATED_KEYS);

			// populate the prepared statement
			populateInsertStatement(stmt, message);

			// execute the statement
			stmt.executeUpdate();

			// retrieve and return the generated id
			return JdbcHelper.retrieveGeneratedId(stmt);
		} finally {
			if (stmt != null) {
				try { stmt.close(); } catch (Exception e) {}
			}
		}
	}

	private void populateInsertStatement(PreparedStatement stmt, Message message) throws SQLException {
		stmt.setString(1, message.getReference());
		stmt.setString(2, message.getSource());
		stmt.setString(3, message.getDestination());
		stmt.setByte(4, message.getStatus());
		stmt.setString(5, message.getProperty("to", String.class));
		stmt.setString(6, message.getProperty("from", String.class));
		stmt.setString(7, message.getProperty("text", String.class));

		Integer sequenceNumber = message.getProperty("sequenceNumber", Integer.class);
		log.trace("sequenceNumber: " + sequenceNumber);
		if (sequenceNumber != null) {
			stmt.setInt(8, sequenceNumber);
		} else {
			stmt.setNull(8, Types.INTEGER);
		}

		stmt.setString(9, message.getProperty("messageId", String.class));

		Integer commandStatus = message.getProperty("commandStatus", Integer.class);
		if (commandStatus != null) {
			stmt.setInt(10, commandStatus);
		} else {
			stmt.setNull(10, Types.INTEGER);
		}

		stmt.setString(11, message.getProperty("receiptStatus", String.class));

		Date receiptTime = message.getProperty("receiptTime", Date.class);
		stmt.setTimestamp(12, receiptTime != null ? new Timestamp(receiptTime.getTime()) : null);

		stmt.setString(13, buildJSON(message));

		stmt.setTimestamp(14, new Timestamp(message.getCreationTime().getTime()));
	}

	/**
	 * Helper method. Builds a JSON representation of all the "unknown" properties of the message (ie. properties that don't
	 * have an associated column in the db).
	 *
	 * @param message the message from which we are retreiving the properties to build the JSON object.
	 * @return a String representation of the JSON object.
	 */
	private String buildJSON(Message message)  {
		JSONObject obj = new JSONObject();

		for (Map.Entry<String, Object> entry : message.getProperties().entrySet()) {
			if (!isKnownProperty(entry.getKey())) {
				try {
					obj.put(entry.getKey(), entry.getValue());
				} catch (JSONException e) {
					log.error("Could not persist JSON property '" + entry.getKey() + "': " + e.getMessage(), e);
				}
			}
		}

		return obj.toString();
	}

	/**
	 * Helper method. Tells if a property is in the list of known properties (ie. the properties that don't have an associated
	 * column in the db).
	 *
	 * @param property the property we are testing against the known properties.
	 * @return true if it is a "known property", false otherwise.
	 */
	private boolean isKnownProperty(String property) {
		for (String knownProperty : KNOWN_PROPERTIES) {
			if (property.equals(knownProperty)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public final boolean updateMessage(Connection conn, Message message) throws SQLException {
		if (message.getModificationTime() == null) {
			message.setModificationTime(new Date());
		}

		String strSQL = "UPDATE " + tableName + " SET " +
				"status = ?, " +
				"destination = ?, " +
				"smsc_sequencenumber = ?, " +
				"smsc_commandstatus = ?, " +
				"smsc_messageid = ?, " +
				"smsc_receiptstatus = ?, " +
				"smsc_receipttime = ?, " +
				"other = ?, " +
				"modification_time = ? " +
				"WHERE id = ?";

		PreparedStatement stmt = conn.prepareStatement(strSQL);

		stmt.setByte(1, message.getStatus());
		stmt.setString(2, message.getDestination());

		Integer sequenceNumber = message.getProperty("sequenceNumber", Integer.class);
		if (sequenceNumber != null) {
			stmt.setInt(3, sequenceNumber);
		} else {
			stmt.setNull(3, Types.INTEGER);
		}

		Integer commandStatus = message.getProperty("commandStatus", Integer.class);
		if (commandStatus != null) {
			stmt.setInt(4, commandStatus);
		} else {
			stmt.setNull(4, Types.INTEGER);
		}

		stmt.setString(5, message.getProperty("messageId", String.class));
		stmt.setString(6, message.getProperty("receiptStatus", String.class));

		Date receiptTime = message.getProperty("receiptTime", Date.class);
		if (receiptTime != null) {
			stmt.setTimestamp(7, new Timestamp(receiptTime.getTime()));
		} else {
			stmt.setTimestamp(7, null);
		}

		stmt.setString(8, buildJSON(message));
		stmt.setTimestamp(9, new Timestamp(message.getModificationTime().getTime()));

		stmt.setLong(10, (Long) message.getId());

		int affected = stmt.executeUpdate();

		if (affected == 0) {
			return false;
		}

		return true;
	}

	@Override
	public final void updateMessagesStatus(Connection conn, MessageCriteria criteria, byte newStatus) throws SQLException {
		List<Object> params = new ArrayList<Object>();

		String strSQL = "UPDATE " + tableName + " SET status = ?";
		strSQL += addCommonCriteria(criteria, params);

		PreparedStatement stmt = conn.prepareStatement(strSQL);

		stmt.setByte(1, newStatus);

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
		log.trace("List messages SQL: " + strSQL);

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement(strSQL);

			if (!params.isEmpty()) {
				int index = 1;
				for (Object param : params) {
					stmt.setObject(index, param);
					index++;
				}
			}

			rs = stmt.executeQuery();
			return createMessages(rs);
		} finally {
			if (rs != null) {
				try { rs.close(); } catch (Exception e) {}
			}
			if (stmt != null) {
				try { stmt.close(); } catch (Exception e) {}
			}
		}
	}

	private String addCommonCriteria(MessageCriteria criteria, List<Object> params) {
		StringBuffer strSQL = new StringBuffer();

		if (criteria != null) {
			boolean existsCriteria = false;

			// status
			List<Byte> status = criteria.getStatus();
			if (status != null && !status.isEmpty()) {
				strSQL.append(addOperator(existsCriteria));

				boolean existsStatusCriteria = false;
				for (Byte st : status) {
					if (!existsStatusCriteria) {
						strSQL.append(" status = ?");
					} else {
						strSQL.append(" or status = ?");
					}

					params.add(st);
					existsStatusCriteria = true;

					existsCriteria = true;
				}
			}

			// destination
			if (criteria.getDestination() != null) {
				strSQL.append(addOperator(existsCriteria));

				strSQL.append(" destination = ?");
				params.add(criteria.getDestination());

				existsCriteria = true;
			}

			// add additional properties
			for (Map.Entry<String,Object> entry : criteria.getProperties().entrySet()) {
				strSQL.append(addOperator(existsCriteria));

				// map known values -- this is a hack while we deprecate the relational databases
				String key = entry.getKey();
				if ("to".equals(key)) {
					key = "smsc_to";
				} else if ("from".equals(key)) {
					key = "smsc_from";
				}

				strSQL.append(" " + key + " = ?");
				params.add(entry.getValue());

				existsCriteria = true;
			}

			// order by
			if (criteria.getOrderBy() != null && !"".equals(criteria.getOrderBy())) {
				String orderBy = criteria.getOrderBy();
				strSQL.append(" ORDER BY ").append(orderBy);

				if (criteria.getOrderType() == OrderType.UPWARDS) {
					strSQL.append(" ASC");
				} else {
					strSQL.append(" DESC");
				}
			}

			// limit
			int lowerLimit = criteria.getLowerLimit();
			int numRecords = criteria.getNumRecords();
			if (numRecords > 0) {
				sqlEngine.addLimitToQuery(strSQL, lowerLimit, numRecords);
			}

		}

		return strSQL.toString();
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

	@SuppressWarnings("rawtypes")
	private Collection<Message> createMessages(ResultSet rs) throws SQLException {
		Collection<Message> messages = new ArrayList<Message>();

		while (rs.next()) {
			Message message = new Message();

			message.setDirection(getMessageDirection());

			message.setId(rs.getLong("id"));
			message.setReference(rs.getString("reference"));
			message.setSource(rs.getString("source"));
			message.setDestination(rs.getString("destination"));
			message.setStatus(rs.getByte("status"));

			message.setProperty("to", rs.getString("smsc_to"));
			message.setProperty("from", rs.getString("smsc_from"));
			message.setProperty("text", rs.getString("smsc_text"));
			message.setProperty("sequenceNumber", rs.getInt("smsc_sequencenumber"));
			message.setProperty("messageId", rs.getString("smsc_messageid"));
			message.setProperty("commandStatus", rs.getInt("smsc_commandstatus"));
			message.setProperty("receiptStatus", rs.getString("smsc_receiptstatus"));
			message.setProperty("receiptTime", rs.getTimestamp("smsc_receipttime"));

			String jsonString = rs.getString("other");
			if (jsonString != null && !"".equals(jsonString)) {
				try {
					JSONObject json = new JSONObject(jsonString);

					Iterator iterator = json.keys();
					while (iterator.hasNext()) {
						String key = (String) iterator.next();
						message.setProperty(key, json.get(key));
					}

				} catch (JSONException e) {
					log.error("JSONException while retreiving string: " + jsonString + ": " + e.getMessage(), e);
				}
			}

			message.setCreationTime(rs.getTimestamp("creation_time"));
			message.setModificationTime(rs.getTimestamp("modification_time"));

			messages.add(message);
		}

		return messages;
	}

	public SqlEngine getSqlEngine() {
		return sqlEngine;
	}

	public void setSqlEngine(SqlEngine sqlEngine) {
		this.sqlEngine = sqlEngine;
	}

	public final String getTableName() {
		return tableName;
	}

	public final void setTableName(String tableName) {
		this.tableName = tableName;
	}

	protected abstract String getDefaultTableName();

	protected abstract Direction getMessageDirection();
}
