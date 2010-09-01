package org.mokai.persist.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.commons.dbcp.BasicDataSource;
import org.mokai.Message;
import org.mokai.Message.Flow;
import org.mokai.Message.SourceType;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.jdbc.JdbcSmsMessageStore;
import org.mokai.persist.jdbc.util.DerbyInitializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class JdbcMessageStoreTest {
	
	private DataSource dataSource;

	@BeforeClass
	public void setUp() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dataSource.setUrl("jdbc:derby:data\\derby\\mokai;create=true");
		dataSource.setUsername("");
		dataSource.setPassword("");
		
		this.dataSource = dataSource;
		
		DerbyInitializer dbInitializer = new DerbyInitializer();
		dbInitializer.setDataSource(dataSource);
		
		dbInitializer.init();
	}
	
	@BeforeMethod
	public void cleanDb() throws Exception {
		// drop messages table
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM message");
		} finally {
			if (stmt != null) {
				try { stmt.close(); } catch (Exception e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
	}
	
	@AfterClass
	public void tearDown() {
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (Exception e) {}
	}
	
	@Test
	public void testSaveRetrieveMessage() throws Exception {
		JdbcSmsMessageStore messageStore = new JdbcSmsMessageStore();
		messageStore.setDataSource(dataSource);
		
		Message message = new Message(Message.SMS_TYPE);
		message.setFlow(Flow.OUTBOUND);
		message.setSource("test");
		message.setSourceType(SourceType.RECEIVER);
		message.setStatus(Status.CREATED);
		message.setProperty("from", "1111");
		message.setProperty("to", "2222");
		message.setProperty("text", "text");
		
		messageStore.saveOrUpdate(message);
		
		Collection<Message> messages = messageStore.list(null);
		Assert.assertEquals(1, messages.size());
		
		Message smsMessage = (Message) messages.iterator().next();
		Assert.assertEquals(Flow.OUTBOUND, smsMessage.getFlow());
		Assert.assertEquals("test", smsMessage.getSource());
		Assert.assertEquals(SourceType.RECEIVER, smsMessage.getSourceType());
		Assert.assertEquals(Status.CREATED, smsMessage.getStatus());
		Assert.assertEquals("1111", smsMessage.getProperty("from", String.class));
		Assert.assertEquals("2222", smsMessage.getProperty("to", String.class));
		Assert.assertEquals("text", smsMessage.getProperty("text", String.class));
	}
	
	@Test
	public void testRetrieveMessages() throws Exception {
		generateTestData();
		
		JdbcSmsMessageStore messageStore = new JdbcSmsMessageStore();
		messageStore.setDataSource(dataSource);
		
		// retrieve failed messages
		MessageCriteria criteria = new MessageCriteria();
		criteria.addStatus(Status.FAILED);
		Collection<Message> messages = messageStore.list(criteria);
		Assert.assertEquals(3, messages.size());
		
		// retrieve processed messages
		criteria = new MessageCriteria();
		criteria.addStatus(Status.PROCESSED);
		messages = messageStore.list(criteria);
		Assert.assertEquals(3, messages.size());
		
		// retrieve unroutable messages
		criteria = new MessageCriteria();
		criteria.addStatus(Status.UNROUTABLE);
		messages = messageStore.list(criteria);
		Assert.assertEquals(3, messages.size());
		
	}
	
	@Test
	public void testUpdateMessagesToRetry() throws Exception {
		generateTestData();
		
		JdbcSmsMessageStore messageStore = new JdbcSmsMessageStore();
		messageStore.setDataSource(dataSource);
		
		messageStore.updateFailedToRetrying();
		
		// retrieve failed messages
		MessageCriteria criteria = new MessageCriteria();
		criteria.addStatus(Status.FAILED);
		Collection<Message> messages = messageStore.list(criteria);
		Assert.assertEquals(0, messages.size());
		
		// retrieve retrying messages
		criteria = new MessageCriteria();
		criteria.addStatus(Status.RETRYING);
		messages = messageStore.list(criteria);
		Assert.assertEquals(3, messages.size());
	}
	
	private void generateTestData() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement("INSERT INTO message (flow_message, source_message, sourcetype_message, destinationtype_message, status_message) VALUES (?, ?, ?, ?, ?)");
			
			// create 3 failed messages
			createMessage(stmt, (byte) 3);
			createMessage(stmt, (byte) 3);
			createMessage(stmt, (byte) 3);
			
			// create 3 processed messages
			createMessage(stmt, (byte) 2);
			createMessage(stmt, (byte) 2);
			createMessage(stmt, (byte) 2);
			
			// create 3 unroutable messages
			createMessage(stmt, (byte) 4);
			createMessage(stmt, (byte) 4);
			createMessage(stmt, (byte) 4);
			
		} finally {
			if (stmt != null) {
				try { stmt.close(); } catch (Exception e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
	}
	
	private void createMessage(PreparedStatement stmt, byte status) throws Exception {
		// create failed message
		stmt.setByte(1, (byte) 2);
		stmt.setString(2, "test");
		stmt.setByte(3, (byte) 1);
		stmt.setByte(4, (byte) -1);
		stmt.setByte(5, status);
		
		stmt.execute();
	}
}
