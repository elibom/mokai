package org.mokai.persist.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

import junit.framework.Assert;

import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.jdbc.DirectionJdbcHandler;
import org.mokai.persist.jdbc.JdbcHandler;
import org.testng.annotations.Test;


/**
 * 
 * @author German Escobar
 */
public class DirectionJdbcHandlerTest {
	
	@Test
	public void testSaveOutboundMessage() throws Exception {
		
		JdbcHandler outHandler = mockHandlerForSaveMessage();
		JdbcHandler inHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Message message = new Message();
		message.setDirection(Direction.OUTBOUND);
		
		jdbcHandler.insertMessage(conn, message);
		
		verify(outHandler).insertMessage(any(Connection.class), any(Message.class));
		verify(inHandler, never()).insertMessage(any(Connection.class), any(Message.class));
	}
	
	@Test
	public void testSaveInboundMessage() throws Exception {
		
		JdbcHandler inHandler = mockHandlerForSaveMessage();
		JdbcHandler outHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Message message = new Message();
		message.setDirection(Direction.INBOUND);
		
		jdbcHandler.insertMessage(conn, message);
		
		verify(inHandler).insertMessage(any(Connection.class), any(Message.class));
		verify(outHandler, never()).insertMessage(any(Connection.class), any(Message.class));
	}
	
	private JdbcHandler mockHandlerForSaveMessage() throws Exception {
		
		JdbcHandler handler = mock(JdbcHandler.class);
		when(handler.insertMessage(any(Connection.class), any(Message.class)))
			.thenReturn(10L);
		
		return handler;
	}
	
	@Test
	public void testSaveWithUnknownDirection() throws Exception {
		
		JdbcHandler outHandler = mock(JdbcHandler.class);
		JdbcHandler inHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Message message = new Message();
		message.setDirection(Direction.UNKNOWN);
		
		jdbcHandler.insertMessage(conn, message);
		Assert.assertEquals(-1, message.getId());
	}
	
	@Test
	public void testUpdateOutboundMessage() throws Exception {

		JdbcHandler outHandler = mock(JdbcHandler.class);
		when(outHandler.updateMessage(any(Connection.class), any(Message.class)))
			.thenReturn(true);
		
		JdbcHandler inHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Message message = new Message();
		message.setId(10);
		message.setDirection(Direction.OUTBOUND);
		
		jdbcHandler.updateMessage(conn, message);
	
		verify(outHandler).updateMessage(any(Connection.class), any(Message.class));
		verify(inHandler, never()).updateMessage(any(Connection.class), any(Message.class));
		
	}
	
	@Test
	public void testUpdateInboundMessage() throws Exception {
		
		JdbcHandler inHandler = mock(JdbcHandler.class);
		when(inHandler.updateMessage(any(Connection.class), any(Message.class)))
			.thenReturn(true);
		
		JdbcHandler outHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Message message = new Message();
		message.setId(10);
		message.setDirection(Direction.INBOUND);
		
		jdbcHandler.updateMessage(conn, message);
		
		verify(inHandler).updateMessage(any(Connection.class), any(Message.class));
		verify(outHandler, never()).updateMessage(any(Connection.class), any(Message.class));
	}
	
	@Test
	public void testUpdateStatusOfOutboundMessages() throws Exception {
		
		JdbcHandler outHandler = mock(JdbcHandler.class);
		JdbcHandler inHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.OUTBOUND);
		jdbcHandler.updateMessagesStatus(conn, criteria, Status.RETRYING);	
		
		verify(outHandler).updateMessagesStatus(any(Connection.class), 
				any(MessageCriteria.class), any(Status.class));
		verify(inHandler, never()).updateMessagesStatus(any(Connection.class),  
				any(MessageCriteria.class), any(Status.class));
	}
	
	@Test
	public void testUpdateStatusOfInboundMessages() throws Exception {
		
		JdbcHandler inHandler = mock(JdbcHandler.class);
		JdbcHandler outHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.INBOUND);
		jdbcHandler.updateMessagesStatus(conn, criteria, Status.RETRYING);	
		
		verify(inHandler).updateMessagesStatus(any(Connection.class),  
				any(MessageCriteria.class), any(Status.class));
		verify(outHandler, never()).updateMessagesStatus(any(Connection.class),  
				any(MessageCriteria.class), any(Status.class));
	}
	
	@Test
	public void testUpdateStatusOfAllMessages() throws Exception {
		
		JdbcHandler inHandler = mock(JdbcHandler.class);		
		JdbcHandler outHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
	
		jdbcHandler.updateMessagesStatus(conn, new MessageCriteria(), Status.RETRYING);
		jdbcHandler.updateMessagesStatus(conn, new MessageCriteria().direction(Direction.UNKNOWN), 
				Status.RETRYING);
		
		verify(inHandler, times(2)).updateMessagesStatus(any(Connection.class), 
				any(MessageCriteria.class), any(Status.class));
		verify(outHandler, times(2)).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), any(Status.class));
	}
	
	@Test
	public void testListAllMessages() throws Exception {
		
		JdbcHandler inHandler = mock(JdbcHandler.class);
		when(inHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		JdbcHandler outHandler = mock(JdbcHandler.class);
		when(outHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Collection<Message> message = jdbcHandler.listMessages(conn, new MessageCriteria());
		Assert.assertEquals(2, message.size());
		
	}
	
	@Test
	public void testListOutboundMessages() throws Exception {
		
		JdbcHandler outHandler = mock(JdbcHandler.class);
		when(outHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		JdbcHandler inHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Collection<Message> message = jdbcHandler.listMessages(conn, 
				new MessageCriteria().direction(Direction.OUTBOUND));
		Assert.assertEquals(1, message.size());
		
		verify(inHandler, never()).listMessages(any(Connection.class), any(MessageCriteria.class));
	}
	
	@Test
	public void testListInboundMessages() throws Exception {
		JdbcHandler outHandler = mock(JdbcHandler.class);
		
		JdbcHandler inHandler = mock(JdbcHandler.class);
		when(inHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Collection<Message> message = jdbcHandler.listMessages(conn, 
				new MessageCriteria().direction(Direction.INBOUND));
		Assert.assertEquals(1, message.size());
		
		verify(outHandler, never()).listMessages(any(Connection.class), any(MessageCriteria.class));	
		
	}
	
	@Test
	public void testListHandlersReturnsNull() throws Exception {
		
		JdbcHandler outHandler = mock(JdbcHandler.class);	
		JdbcHandler inHandler = mock(JdbcHandler.class);
		
		Connection conn = mock(Connection.class);
		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Collection<Message> message = jdbcHandler.listMessages(conn, new MessageCriteria());
		Assert.assertEquals(0, message.size());
		
		verify(outHandler).listMessages(any(Connection.class), any(MessageCriteria.class));
		verify(inHandler).listMessages(any(Connection.class), any(MessageCriteria.class));
	}
	
	@Test
	public void testOutboundHandlerSupportsType() throws Exception {
		
		JdbcHandler outHandler = mock(JdbcHandler.class);
		when(outHandler.supportsType("test")).thenReturn(true);
		JdbcHandler inHandler = mock(JdbcHandler.class);

		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Assert.assertTrue(jdbcHandler.supportsType("test"));
		Assert.assertFalse(jdbcHandler.supportsType("other"));
	}
	
	@Test
	public void testInboundHandlerSupportsType() throws Exception {
		JdbcHandler outHandler = mock(JdbcHandler.class);
		JdbcHandler inHandler = mock(JdbcHandler.class);
		when(inHandler.supportsType("test")).thenReturn(true);

		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Assert.assertTrue(jdbcHandler.supportsType("test"));
		Assert.assertFalse(jdbcHandler.supportsType("other"));
	}
	
	@Test
	public void testNoHandlerSupportsType() throws Exception {
		JdbcHandler outHandler = mock(JdbcHandler.class);
		JdbcHandler inHandler = mock(JdbcHandler.class);

		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Assert.assertFalse(jdbcHandler.supportsType("test"));
		Assert.assertFalse(jdbcHandler.supportsType("other"));
	}
	
	@Test
	public void testOutboundHandlerSupportsDirection() throws Exception {
		JdbcHandler outHandler = mock(JdbcHandler.class);
		when(outHandler.supportsDirection(Direction.OUTBOUND)).thenReturn(true);
		JdbcHandler inHandler = mock(JdbcHandler.class);

		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Assert.assertTrue(jdbcHandler.supportsDirection(Direction.OUTBOUND));
		Assert.assertFalse(jdbcHandler.supportsDirection(Direction.INBOUND));
		Assert.assertFalse(jdbcHandler.supportsDirection(Direction.UNKNOWN));
		Assert.assertFalse(jdbcHandler.supportsDirection(null));
	}
	
	@Test
	public void testInboundHandlerSupportsDirection() throws Exception {
		JdbcHandler outHandler = mock(JdbcHandler.class);
		JdbcHandler inHandler = mock(JdbcHandler.class);
		when(inHandler.supportsDirection(Direction.INBOUND)).thenReturn(true);

		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Assert.assertFalse(jdbcHandler.supportsDirection(Direction.OUTBOUND));
		Assert.assertTrue(jdbcHandler.supportsDirection(Direction.INBOUND));
		Assert.assertFalse(jdbcHandler.supportsDirection(Direction.UNKNOWN));
		Assert.assertFalse(jdbcHandler.supportsDirection(null));
	}
	
	@Test
	public void testNoHandlerSupportsDirection() throws Exception {
		JdbcHandler outHandler = mock(JdbcHandler.class);
		JdbcHandler inHandler = mock(JdbcHandler.class);

		JdbcHandler jdbcHandler = createJdbcHandler(outHandler, inHandler);
		
		Assert.assertFalse(jdbcHandler.supportsDirection(Direction.OUTBOUND));
		Assert.assertFalse(jdbcHandler.supportsDirection(Direction.INBOUND));
		Assert.assertFalse(jdbcHandler.supportsDirection(Direction.UNKNOWN));
		Assert.assertFalse(jdbcHandler.supportsDirection(null));
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailSetNullOutboundHandler() throws Exception {
		DirectionJdbcHandler handler = new DirectionJdbcHandler();
		handler.setOutboundJdbcHandler(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailSetNullInboundHandler() throws Exception {
		DirectionJdbcHandler handler = new DirectionJdbcHandler();
		handler.setInboundJdbcHandler(null);
	}
	
	private JdbcHandler createJdbcHandler(JdbcHandler outHandler, JdbcHandler inHandler) {
		
		DirectionJdbcHandler jdbcHandler = new DirectionJdbcHandler();
		jdbcHandler.setOutboundJdbcHandler(outHandler);
		jdbcHandler.setInboundJdbcHandler(inHandler);
		
		return jdbcHandler;
	}
	
}
