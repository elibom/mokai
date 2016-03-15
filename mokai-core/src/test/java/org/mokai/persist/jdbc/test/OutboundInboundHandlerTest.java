package org.mokai.persist.jdbc.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.RejectedException;
import org.mokai.persist.jdbc.MessageHandler;
import org.mokai.persist.jdbc.OutboundInboundHandler;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author German Escobar
 */
public class OutboundInboundHandlerTest {

	@Test
	public void testSaveOutboundMessage() throws Exception {
		MessageHandler outHandler = mockHandlerForSaveMessage();
		MessageHandler inHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Message message = new Message();
		message.setDirection(Direction.TO_CONNECTIONS);

		handler.insertMessage(conn, message);

		verify(outHandler).insertMessage(any(Connection.class), any(Message.class));
		verify(inHandler, never()).insertMessage(any(Connection.class), any(Message.class));
	}

	@Test
	public void testSaveInboundMessage() throws Exception {
		MessageHandler inHandler = mockHandlerForSaveMessage();
		MessageHandler outHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Message message = new Message();
		message.setDirection(Direction.TO_APPLICATIONS);

		handler.insertMessage(conn, message);

		verify(inHandler).insertMessage(any(Connection.class), any(Message.class));
		verify(outHandler, never()).insertMessage(any(Connection.class), any(Message.class));
	}

	private MessageHandler mockHandlerForSaveMessage() throws Exception {
		MessageHandler handler = mock(MessageHandler.class);
		when(handler.insertMessage(any(Connection.class), any(Message.class)))
			.thenReturn(10L);

		return handler;
	}

	@Test(expectedExceptions=RejectedException.class)
	public void testSaveWithUnknownDirection() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);
		MessageHandler inHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Message message = new Message();
		message.setDirection(Direction.UNKNOWN);

		handler.insertMessage(conn, message);
	}

	@Test
	public void testUpdateOutboundMessage() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);
		when(outHandler.updateMessage(any(Connection.class), any(Message.class)))
			.thenReturn(true);

		MessageHandler inHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Message message = new Message();
		message.setId(10);
		message.setDirection(Direction.TO_CONNECTIONS);

		handler.updateMessage(conn, message);

		verify(outHandler).updateMessage(any(Connection.class), any(Message.class));
		verify(inHandler, never()).updateMessage(any(Connection.class), any(Message.class));
	}

	@Test
	public void testUpdateInboundMessage() throws Exception {
		MessageHandler inHandler = mock(MessageHandler.class);
		when(inHandler.updateMessage(any(Connection.class), any(Message.class)))
			.thenReturn(true);

		MessageHandler outHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Message message = new Message();
		message.setId(10);
		message.setDirection(Direction.TO_APPLICATIONS);

		handler.updateMessage(conn, message);

		verify(inHandler).updateMessage(any(Connection.class), any(Message.class));
		verify(outHandler, never()).updateMessage(any(Connection.class), any(Message.class));
	}

	@Test(expectedExceptions=RejectedException.class)
	public void testUpdateWithUnknownDirection() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);
		MessageHandler inHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Message message = new Message();
		message.setDirection(Direction.UNKNOWN);

		handler.updateMessage(conn, message);
	}

	@Test
	public void testUpdateStatusOfOutboundMessages() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);
		MessageHandler inHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.TO_CONNECTIONS);
		handler.updateMessagesStatus(conn, criteria, Message.STATUS_RETRYING);

		verify(outHandler).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
		verify(inHandler, never()).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
	}

	@Test
	public void testUpdateStatusOfInboundMessages() throws Exception {
		MessageHandler inHandler = mock(MessageHandler.class);
		MessageHandler outHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.TO_APPLICATIONS);
		handler.updateMessagesStatus(conn, criteria, Message.STATUS_RETRYING);

		verify(inHandler).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
		verify(outHandler, never()).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
	}

	@Test
	public void testUpdateStatusOfAllMessages() throws Exception {
		MessageHandler inHandler = mock(MessageHandler.class);
		MessageHandler outHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		handler.updateMessagesStatus(conn, new MessageCriteria(), Message.STATUS_RETRYING);

		verify(inHandler).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
		verify(outHandler).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
	}

	@Test
	public void testUpdateStatusUnknownDirection() throws Exception {
		MessageHandler inHandler = mock(MessageHandler.class);
		MessageHandler outHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		handler.updateMessagesStatus(conn, new MessageCriteria().direction(Direction.UNKNOWN),
				Message.STATUS_RETRYING);

		verify(inHandler, never()).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
		verify(outHandler, never()).updateMessagesStatus(any(Connection.class),
				any(MessageCriteria.class), anyByte());
	}

	@Test
	public void testListAllMessages() throws Exception {
		MessageHandler inHandler = mock(MessageHandler.class);
		when(inHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		MessageHandler outHandler = mock(MessageHandler.class);
		when(outHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Collection<Message> message = handler.listMessages(conn, new MessageCriteria());
		Assert.assertEquals(2, message.size());
	}

	@Test
	public void testListOutboundMessages() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);
		when(outHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));
		MessageHandler inHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Collection<Message> message = handler.listMessages(conn,
				new MessageCriteria().direction(Direction.TO_CONNECTIONS));
		Assert.assertEquals(1, message.size());

		verify(inHandler, never()).listMessages(any(Connection.class), any(MessageCriteria.class));
	}

	@Test
	public void testListInboundMessages() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);

		MessageHandler inHandler = mock(MessageHandler.class);
		when(inHandler.listMessages(any(Connection.class), any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(new Message()));

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Collection<Message> message = handler.listMessages(conn,
				new MessageCriteria().direction(Direction.TO_APPLICATIONS));
		Assert.assertEquals(1, message.size());

		verify(outHandler, never()).listMessages(any(Connection.class), any(MessageCriteria.class));
	}

	@Test
	public void testListHandlersReturnsNull() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);
		MessageHandler inHandler = mock(MessageHandler.class);

		Connection conn = mock(Connection.class);
		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Collection<Message> message = handler.listMessages(conn, new MessageCriteria());
		Assert.assertEquals(0, message.size());

		verify(outHandler).listMessages(any(Connection.class), any(MessageCriteria.class));
		verify(inHandler).listMessages(any(Connection.class), any(MessageCriteria.class));
	}

	@Test
	public void testSupportsOutboundInboundDirection() throws Exception {
		MessageHandler outHandler = mock(MessageHandler.class);
		MessageHandler inHandler = mock(MessageHandler.class);

		MessageHandler handler = createMessageHandler(outHandler, inHandler);

		Assert.assertTrue(handler.supportsDirection(Direction.TO_CONNECTIONS));
		Assert.assertTrue(handler.supportsDirection(Direction.TO_APPLICATIONS));
		Assert.assertFalse(handler.supportsDirection(Direction.UNKNOWN));
		Assert.assertFalse(handler.supportsDirection(null));
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailSetNullOutboundHandler() throws Exception {
		OutboundInboundHandler handler = new OutboundInboundHandler();
		handler.setOutboundHandler(null);
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailSetNullInboundHandler() throws Exception {
		OutboundInboundHandler handler = new OutboundInboundHandler();
		handler.setInboundHandler(null);
	}

	private MessageHandler createMessageHandler(MessageHandler outHandler, MessageHandler inHandler) {
		OutboundInboundHandler handler = new OutboundInboundHandler();
		handler.setOutboundHandler(outHandler);
		handler.setInboundHandler(inHandler);

		return handler;
	}

}
