package org.mokai.persist.jdbc.sms.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.Message;
import org.mokai.Message.DestinationType;
import org.mokai.Message.Direction;
import org.mokai.Message.SourceType;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.jdbc.JdbcHelper;
import org.mokai.persist.jdbc.sms.ConnectionsSmsHandler;
import org.mokai.persist.jdbc.sms.DerbyEngine;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConnectionsSmsHandlerTest {

	private DataSource dataSource;
	private Connection connection;
	
	private String tableName = ConnectionsSmsHandler.DEFAULT_TABLENAME;
	
	@BeforeClass
	public void setUp() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dataSource.setUrl("jdbc:derby:data\\derby\\mokai;create=true");
		dataSource.setUsername("");
		dataSource.setPassword("");
		
		this.dataSource = dataSource;
		
		DerbyEngine derbyEngine = new DerbyEngine();
		derbyEngine.setDataSource(dataSource);
		
		derbyEngine.init();
	}
	
	@BeforeMethod
	public void cleanDb() throws Exception {
		// drop messages table
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM " + tableName);
		} finally {
			if (stmt != null) {
				try { stmt.close(); } catch (Exception e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
		
		connection = dataSource.getConnection();
	}
	
	@AfterMethod
	public void closeConnection() {
		if (connection != null){
			try { connection.close(); } catch (Exception e) {}
		}
	}
	
	@AfterClass
	public void tearDown() {
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (Exception e) {}
	}
	
	@Test
	public void testSupportsSmsType() throws Exception {
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		Assert.assertTrue(handler.supportsType(Message.SMS_TYPE));
		Assert.assertFalse(handler.supportsType("other"));
		Assert.assertFalse(handler.supportsType("null"));
	}
	
	public void testSupportsOutboundDirection() throws Exception {
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		Assert.assertTrue(handler.supportsDirection(Direction.TO_CONNECTIONS));
		Assert.assertFalse(handler.supportsDirection(Direction.TO_APPLICATIONS));
		Assert.assertFalse(handler.supportsDirection(Direction.UNKNOWN));
		Assert.assertFalse(handler.supportsDirection(null));
	}
	
	@Test
	public void testInsertMessage() throws Exception {

		final Message message = new Message(Message.SMS_TYPE);
		message.setDirection(Direction.TO_CONNECTIONS);
		message.setSource("test");
		message.setSourceType(SourceType.RECEIVER);
		message.setStatus(Status.CREATED);
		message.setProperty("from", "1111");
		message.setProperty("to", "2222");
		message.setProperty("text", "text");
		message.setProperty("other", "other value");
			
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		long id = handler.insertMessage(connection, message);

		validateMessage(id, new MessageValidator() {

			@Override
			public void validate(ResultSet rs) throws SQLException {
				Assert.assertEquals(rs.getString("source"), message.getSource());
				Assert.assertEquals(rs.getByte("sourcetype"), message.getSourceType().value());
				Assert.assertEquals(rs.getByte("status"), message.getStatus().value());
				Assert.assertEquals(rs.getString("smsc_from"), message.getProperty("from", String.class));
				Assert.assertEquals(rs.getString("smsc_to"), message.getProperty("to", String.class));
				Assert.assertEquals(rs.getString("smsc_text"), message.getProperty("text", String.class));
				try { 
					Assert.assertEquals(rs.getString("other"), new JSONObject().put("other", "other value").toString());
				} catch (JSONException e) {
					Assert.fail("shouldn't had to throw exception", e);
				}
			}
			
		});
	}
	
	@Test
	public void testUpdateMessage() throws Exception {
		long id = generateRecordToUpdate();
		
		final Message message = new Message(Message.SMS_TYPE);
		message.setId(id);
		message.setStatus(Status.RETRYING);
		message.setDestination("test");
		message.setDestinationType(DestinationType.PROCESSOR);
		message.setProperty("receiptStatus", "DELIVRD");
		message.setProperty("other", "other value");
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		boolean found = handler.updateMessage(connection, message);
		
		Assert.assertTrue(found);
		
		validateMessage(id, new MessageValidator() {

			@Override
			public void validate(ResultSet rs) throws SQLException {
				Assert.assertEquals(rs.getByte("status"), message.getStatus().value());
				Assert.assertEquals(rs.getString("destination"), message.getDestination());
				Assert.assertEquals(rs.getByte("destinationtype"), message.getDestinationType().value());
				Assert.assertEquals(rs.getString("smsc_receiptstatus"), message.getProperty("receiptStatus", String.class));
				Assert.assertNull(rs.getTimestamp("smsc_receipttime"));
				try { 
					Assert.assertEquals(rs.getString("other"), new JSONObject().put("other", "other value").toString());
				} catch (JSONException e) {
					Assert.fail("shouldn't had to throw exception", e);
				}
			}
			
		});
		
	}

	@Test
	public void testUpdateNotFoundMessage() throws Exception {

		final Message message = new Message(Message.SMS_TYPE);
		message.setId(1);
		message.setStatus(Status.RETRYING);
		message.setDestination("test");
		message.setDestinationType(DestinationType.PROCESSOR);
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		boolean found = handler.updateMessage(connection, message);
		
		Assert.assertFalse(found);
	}
	
	@Test
	public void testUpdateStatusToAllMessages() throws Exception {
		generateTestData();
		Assert.assertEquals(getNumMessagesByStatus(Status.FAILED), 3);
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		handler.updateMessagesStatus(connection, null, Status.RETRYING);
		
		Assert.assertEquals(getNumMessagesByStatus(Status.FAILED), 0);
		Assert.assertEquals(getNumMessagesByStatus(Status.RETRYING), 9);
	}
	
	@Test
	public void testUpdateStatusToFailedMessages() throws Exception {
		generateTestData();
		Assert.assertEquals(getNumMessagesByStatus(Status.FAILED), 3);
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		handler.updateMessagesStatus(connection, new MessageCriteria().addStatus(Status.FAILED), Status.RETRYING);
		
		Assert.assertEquals(getNumMessagesByStatus(Status.FAILED), 0);
		Assert.assertEquals(getNumMessagesByStatus(Status.RETRYING), 3);
	}
	
	@Test
	public void testRetrieveAllMessages() throws Exception {
		generateTestData();
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		handler.setSqlEngine(new DerbyEngine(dataSource));
		
		Collection<Message> messages = handler.listMessages(connection, null);
		
		Assert.assertFalse(messages.isEmpty());
		Assert.assertEquals(messages.size(), 9);
	}
	
	@Test
	public void testRetrieveSomeMessages() throws Exception {
		generateTestData();
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		handler.setSqlEngine(new DerbyEngine(dataSource));
		
		MessageCriteria criteria = new MessageCriteria() 
			.lowerLimit(3)
			.numRecords(3);
		Collection<Message> messages = handler.listMessages(connection, criteria);
		
		Assert.assertFalse(messages.isEmpty());
		Assert.assertEquals(messages.size(), 3);
	}
	
	@Test
	public void testRetrieveMessageId() throws Exception {
		generateTestData();
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		handler.setSqlEngine(new DerbyEngine(dataSource));
		
		MessageCriteria criteria = new MessageCriteria()
			.addProperty("smsc_messageid", "8");
		
		Collection<Message> messages = handler.listMessages(connection, criteria);
		
		Assert.assertEquals(messages.size(), 1);
	}
	
	@Test
	public void testRetrieveMessagesByStatus() throws Exception {
		generateTestData();
		
		ConnectionsSmsHandler handler = new ConnectionsSmsHandler();
		handler.setSqlEngine(new DerbyEngine(dataSource));
		
		MessageCriteria criteria = new MessageCriteria()
			.addStatus(Status.FAILED);
		
		Collection<Message> messages = handler.listMessages(connection, criteria);
		
		Assert.assertEquals(messages.size(), 3);
	}
	
	private int getNumMessagesByStatus(Status status) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement("select count(*) from " + tableName + " where status = " + status.value());
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				return rs.getInt(1);
			}
			
			return 0;
		} finally {
			closeResources(rs, stmt, conn);
		}
		
	}
	
	private long generateRecordToUpdate() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement("INSERT INTO " + tableName + " (" +
					"source, " +
					"sourcetype, " +
					"destinationtype, " +
					"status, " +
					"creation_time) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, "test");
			stmt.setByte(2, SourceType.RECEIVER.value());
			stmt.setByte(3, DestinationType.UNKNOWN.value());
			stmt.setByte(4, Status.FAILED.value());
			stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
			
			stmt.executeUpdate();
			
			return JdbcHelper.retrieveGeneratedId(stmt);
			
		} finally {
			closeResources(null, stmt, conn);
		}
	}
	
	private void generateTestData() throws SQLException, JSONException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement("INSERT INTO " + tableName + " (" +
					"source, " +
					"sourcetype, " +
					"destinationtype, " +
					"status, " +
					"smsc_messageid, " +
					"other, " +
					"creation_time) VALUES (?, ?, ?, ?, ?, ?, ?)");
			
			// create 3 failed messages
			createMessage(stmt, Status.FAILED.value(), "1", false);
			createMessage(stmt, Status.FAILED.value(), "2", false);
			createMessage(stmt, Status.FAILED.value(), "3", true);
			
			// create 3 processed messages
			createMessage(stmt, Status.PROCESSED.value(), "4", false);
			createMessage(stmt, Status.PROCESSED.value(), "5", false);
			createMessage(stmt, Status.PROCESSED.value(), "6", true);
			
			// create 3 unroutable messages
			createMessage(stmt, Status.UNROUTABLE.value(), "7", false);
			createMessage(stmt, Status.UNROUTABLE.value(), "8", false);
			createMessage(stmt, Status.UNROUTABLE.value(), "9", true);
			
		} finally {
			closeResources(null, stmt, conn);
		}
	}
	
	private void createMessage(PreparedStatement stmt, byte status, String messageId, boolean nullOther) throws SQLException, JSONException {
		
		stmt.setString(1, "test");
		stmt.setByte(2, SourceType.RECEIVER.value());
		stmt.setByte(3, DestinationType.UNKNOWN.value());
		stmt.setByte(4, status);
		stmt.setString(5, messageId);
		if (nullOther) {
			stmt.setString(6, null);
		} else {
			stmt.setString(6, new JSONObject().put("other", "other value").toString());
		}
		
		stmt.setTimestamp(7, new Timestamp(new Date().getTime()));
		
		stmt.execute();
	}
	
	private void validateMessage(long id, MessageValidator validator) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement("select * from " + tableName + " where id = " + id);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				validator.validate(rs);
			} else {
				Assert.fail("no message was found with id " + id);
			}
			
		} finally {
			closeResources(rs, stmt, conn);
		}
	}
	
	private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
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
	
	interface MessageValidator {
		void validate(ResultSet rs) throws SQLException;
	}
	
}
