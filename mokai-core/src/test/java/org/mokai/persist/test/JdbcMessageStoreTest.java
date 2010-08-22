package org.mokai.persist.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Collection;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.commons.dbcp.BasicDataSource;
import org.mokai.Message;
import org.mokai.Message.SourceType;
import org.mokai.Message.Status;
import org.mokai.Message.Type;
import org.mokai.message.SmsMessage;
import org.mokai.persist.jdbc.JdbcMessageStore;
import org.mokai.persist.jdbc.util.DerbyInitializer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class JdbcMessageStoreTest {
	
	private DataSource dataSource;

	@BeforeMethod
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
	
	@AfterMethod
	public void tearDown() throws Exception {
		// drop messages table
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			stmt.execute("DROP TABLE message");
		} finally {
			if (stmt != null) {
				try { stmt.close(); } catch (Exception e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (Exception e) {}
			}
		}
		
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (Exception e) {}
	}
	
	@Test
	public void testSaveRetrieveMessage() throws Exception {
		JdbcMessageStore messageStore = new JdbcMessageStore();
		messageStore.setDataSource(dataSource);
		
		SmsMessage message = new SmsMessage();
		message.setType(Type.OUTBOUND);
		message.setSource("test");
		message.setSourceType(SourceType.RECEIVER);
		message.setStatus(Status.CREATED);
		message.setFrom("1111");
		message.setTo("2222");
		message.setText("text");
		
		messageStore.saveOrUpdate(message);
		
		Collection<Message> messages = messageStore.list(null);
		Assert.assertEquals(1, messages.size());
		
		SmsMessage smsMessage = (SmsMessage) messages.iterator().next();
		Assert.assertEquals(Type.OUTBOUND, smsMessage.getType());
		Assert.assertEquals("test", smsMessage.getSource());
		Assert.assertEquals(SourceType.RECEIVER, smsMessage.getSourceType());
		Assert.assertEquals(Status.CREATED, smsMessage.getStatus());
		Assert.assertEquals("1111", smsMessage.getFrom());
		Assert.assertEquals("2222", smsMessage.getTo());
		Assert.assertEquals("text", smsMessage.getText());
			
		
	}
}
