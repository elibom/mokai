package org.mokai.persist.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.mokai.Message;
import org.mokai.ObjectNotFoundException;
import org.mokai.Message.Direction;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.RejectedException;
import org.mokai.persist.jdbc.JdbcMessageStore;
import org.mokai.persist.jdbc.MessageHandler;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class JdbcMessageStoreTest {

	@Test
	public void testSaveMessage() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsType(anyString())).thenReturn(true);
		when(handler.supportsDirection(any(Direction.class))).thenReturn(true);
		when(handler.insertMessage(any(Connection.class), any(Message.class)))
			.thenReturn(10L);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Message message = new Message();
		messageStore.saveOrUpdate(message);
		
		Assert.assertEquals(10, message.getId());
		
		verify(handler).insertMessage(any(Connection.class), any(Message.class));
		verify(handler, never()).updateMessage(any(Connection.class), any(Message.class));
		
	}
	
	@Test(expectedExceptions=RejectedException.class)
	public void shouldFailSaveIfNotSupported() throws Exception {
		MessageHandler handler = mock(MessageHandler.class);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Message message = new Message();
		messageStore.saveOrUpdate(message);
	}
	
	@Test
	public void testUpdateMessage() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsType(anyString())).thenReturn(true);
		when(handler.supportsDirection(any(Direction.class))).thenReturn(true);
		when(handler.updateMessage(any(Connection.class), any(Message.class)))
			.thenReturn(true);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Message message = new Message();
		message.setId(10);
		messageStore.saveOrUpdate(message);
		
		verify(handler).updateMessage(any(Connection.class), any(Message.class));
		verify(handler, never()).insertMessage(any(Connection.class), any(Message.class));
	}
	
	@Test(expectedExceptions=ObjectNotFoundException.class)
	public void shouldFailToUpdateNonExistentId() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsType(anyString())).thenReturn(true);
		when(handler.supportsDirection(any(Direction.class))).thenReturn(true);
		when(handler.updateMessage(any(Connection.class), any(Message.class)))
			.thenReturn(false);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Message message = new Message();
		message.setId(10);
		messageStore.saveOrUpdate(message);
	}
	
	@Test(expectedExceptions=RejectedException.class)
	public void shouldFailToUpdateIfNotSupported() throws Exception {
		MessageHandler handler = mock(MessageHandler.class);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Message message = new Message();
		message.setId(10);
		messageStore.saveOrUpdate(message);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailToSaveOrUpdateWithNullMessage() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		messageStore.saveOrUpdate(null);
	}
	
	@Test
	public void testUpdateStatusWithEmptyCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		messageStore.updateStatus(new MessageCriteria(), Status.RETRYING);
		
		verify(handler)
			.updateMessagesStatus(any(Connection.class), any(MessageCriteria.class), any(Status.class));
		
	}
	
	@Test
	public void testUpdateStatusWithNullMessageCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		messageStore.updateStatus(null, Status.RETRYING);
		
		verify(handler)
			.updateMessagesStatus(any(Connection.class), any(MessageCriteria.class), any(Status.class));
	}
	
	@Test
	public void testUpdateStatusWithTypeAndDirectionCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsType(anyString())).thenReturn(true);
		when(handler.supportsDirection(any(Direction.class))).thenReturn(true);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		MessageCriteria criteria = new MessageCriteria()
			.type("test")
			.direction(Direction.INBOUND);
		messageStore.updateStatus(criteria, Status.RETRYING);
		
		verify(handler)
			.updateMessagesStatus(any(Connection.class), any(MessageCriteria.class), any(Status.class));
		
	}
	
	@Test
	public void testUpdateStatusWithNotSupportedTypeCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsType(anyString())).thenReturn(false);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		MessageCriteria criteria = new MessageCriteria()
			.type("test");
		messageStore.updateStatus(criteria, Status.RETRYING);
		
		verify(handler, never())
			.updateMessagesStatus(any(Connection.class), any(MessageCriteria.class), any(Status.class));
		
	}
	
	@Test
	public void testUpdateStatusWithNotSupportedDirectionCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsDirection(any(Direction.class))).thenReturn(false);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.INBOUND);
		messageStore.updateStatus(criteria, Status.RETRYING);
		
		verify(handler, never())
			.updateMessagesStatus(any(Connection.class), any(MessageCriteria.class), any(Status.class));
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailUpdateStatusNullStatus() throws Exception {
		MessageHandler handler = mock(MessageHandler.class);
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		messageStore.updateStatus(new MessageCriteria(), null);
	}
	
	@Test
	public void testListWithEmptyCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Collection<Message> messages = messageStore.list(new MessageCriteria());
		
		Assert.assertEquals(1, messages.size());
	}
	
	@Test
	public void testListWithNullCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Collection<Message> messages = messageStore.list(null);
		
		Assert.assertEquals(1, messages.size());
	}
	
	@Test
	public void testListWithTypeAndDirectionCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsType(anyString())).thenReturn(true);
		when(handler.supportsDirection(any(Direction.class))).thenReturn(true);
		when(handler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		MessageCriteria criteria = new MessageCriteria()
			.type("test")
			.direction(Direction.INBOUND);
		Collection<Message> messages = messageStore.list(criteria);
		
		Assert.assertEquals(1, messages.size());
	}
	
	@Test
	public void testListHandlerReturnsNull() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(null);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		Collection<Message> messages = messageStore.list(new MessageCriteria());
		
		Assert.assertNotNull(messages);
		Assert.assertEquals(0, messages.size());
		
		verify(handler).listMessages(any(Connection.class), any(MessageCriteria.class));
	}
	
	@Test
	public void testListWithNotSupportedTypeCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsType(anyString())).thenReturn(false);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		MessageCriteria criteria = new MessageCriteria()
			.type("test");
		Collection<Message> messages = messageStore.list(criteria);
		Assert.assertNotNull(messages);
		Assert.assertEquals(0, messages.size());
		
		verify(handler, never()).listMessages(any(Connection.class), any(MessageCriteria.class));
		
	}
	
	@Test
	public void testListWithNotSupportedDirectionCriteria() throws Exception {
		
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.supportsDirection(any(Direction.class))).thenReturn(false);
		
		DataSource dataSource = mockDataSource();
		JdbcMessageStore messageStore = createMessageStore(dataSource, handler);
		
		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.INBOUND);
		Collection<Message> messages = messageStore.list(criteria);
		Assert.assertNotNull(messages);
		Assert.assertEquals(0, messages.size());
		
		verify(handler, never()).listMessages(any(Connection.class), any(MessageCriteria.class));
		
	}
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailSaveOrUpdateWithNullDataSource() throws Exception {
		
		JdbcMessageStore messageStore = createMessageStoreNoDataSource();
		messageStore.saveOrUpdate(new Message());
	}
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailUpdateStatusWithNullDataSource() throws Exception {
		
		JdbcMessageStore messageStore = createMessageStoreNoDataSource();
		messageStore.updateStatus(new MessageCriteria(), Status.CREATED);
	}
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailListWithNullDataSource() throws Exception {
		
		JdbcMessageStore messageStore = createMessageStoreNoDataSource();
		messageStore.list(new MessageCriteria());
	}
	
	private DataSource mockDataSource() throws SQLException {
		Connection conn = mock(Connection.class);
		
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenReturn(conn);
		
		return dataSource;
	}
	
	private JdbcMessageStore createMessageStore(DataSource dataSource, MessageHandler handler) {
		
		JdbcMessageStore messageStore = new JdbcMessageStore();
		messageStore.setDataSource(dataSource);
		messageStore.setMessageHandler(handler);
		
		return messageStore;
	}
	
	private JdbcMessageStore createMessageStoreNoDataSource() {
	
		MessageHandler handler = mock(MessageHandler.class);
		JdbcMessageStore messageStore = new JdbcMessageStore();
		messageStore.setMessageHandler(handler);
		
		return messageStore;
	}
	
}
