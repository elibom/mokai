package org.mokai.persist.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Collections;

import junit.framework.Assert;

import org.mockito.Mockito;
import org.mokai.Message;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.impl.MultiMessageStore;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class MultiMessageStoreTest {

	@Test
	public void testNoAdditionalMessageStores() throws Exception {
		
		// mock the default message store 
		MessageStore defaultMessageStore = mock(MessageStore.class);
		
		// create the multi message store
		MultiMessageStore messageStore = new MultiMessageStore();
		messageStore.setDefaultMessageStore(defaultMessageStore);
		
		messageStore.saveOrUpdate(new Message());
		verify(defaultMessageStore).saveOrUpdate(any(Message.class));
		
		messageStore.updateStatus(new MessageCriteria(), Message.STATUS_CREATED);
		verify(defaultMessageStore).updateStatus(any(MessageCriteria.class), anyByte());
		
		messageStore.list(new MessageCriteria());
		verify(defaultMessageStore).list(any(MessageCriteria.class));
	}
	
	@Test
	public void testSaveOrUpdateMultipleMessageStore() throws Exception {
		
		// mock the message stores
		MessageStore test1MessageStore = mock(MessageStore.class);
		MessageStore test2MessageStore = mock(MessageStore.class);
		MessageStore defaultMessageStore = mock(MessageStore.class);
		
		// create the multi message store
		MultiMessageStore messageStore = new MultiMessageStore();
		messageStore.setDefaultMessageStore(defaultMessageStore);
		messageStore.addMessageStore("test1", test1MessageStore);
		messageStore.addMessageStore("test2", test2MessageStore);
		
		// should call test1MessageStore
		messageStore.saveOrUpdate(new Message("test1"));
		verify(test1MessageStore).saveOrUpdate(any(Message.class));
		verify(test2MessageStore, times(0)).saveOrUpdate(any(Message.class));
		verify(defaultMessageStore, times(0)).saveOrUpdate(any(Message.class));
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		
		// should call test2MessageStore
		messageStore.saveOrUpdate(new Message("test2"));
		verify(test1MessageStore, times(0)).saveOrUpdate(any(Message.class));
		verify(test2MessageStore).saveOrUpdate(any(Message.class));
		verify(defaultMessageStore, times(0)).saveOrUpdate(any(Message.class));
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		
		// should call defaultMessageStore
		messageStore.saveOrUpdate(new Message("other"));
		verify(test1MessageStore, times(0)).saveOrUpdate(any(Message.class));
		verify(test2MessageStore, times(0)).saveOrUpdate(any(Message.class));
		verify(defaultMessageStore).saveOrUpdate(any(Message.class));
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		
		// should call defaultMessageStore
		messageStore.saveOrUpdate(new Message());
		verify(test1MessageStore, times(0)).saveOrUpdate(any(Message.class));
		verify(test2MessageStore, times(0)).saveOrUpdate(any(Message.class));
		verify(defaultMessageStore).saveOrUpdate(any(Message.class));
	}
	
	@Test
	public void testUpdateStatusMultipleMessageStores() throws Exception {

		// mock the message stores
		MessageStore test1MessageStore = mock(MessageStore.class);
		MessageStore test2MessageStore = mock(MessageStore.class);
		MessageStore defaultMessageStore = mock(MessageStore.class);
		
		// create the multi message store
		MultiMessageStore messageStore = new MultiMessageStore();
		messageStore.setDefaultMessageStore(defaultMessageStore);
		messageStore.addMessageStore("test1", test1MessageStore);
		messageStore.addMessageStore("test2", test2MessageStore);
		
		messageStore.updateStatus(new MessageCriteria(), Message.STATUS_RETRYING);
		verify(test1MessageStore).updateStatus(any(MessageCriteria.class), anyByte());
		verify(test2MessageStore).updateStatus(any(MessageCriteria.class), anyByte());
		verify(defaultMessageStore).updateStatus(any(MessageCriteria.class), anyByte());
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		
		messageStore.updateStatus(new MessageCriteria().type("test1"), Message.STATUS_RETRYING);
		verify(test1MessageStore).updateStatus(any(MessageCriteria.class), anyByte());
		verify(test2MessageStore, times(0)).updateStatus(any(MessageCriteria.class), anyByte());
		verify(defaultMessageStore, times(0)).updateStatus(any(MessageCriteria.class), anyByte());
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		
		messageStore.updateStatus(new MessageCriteria().type("test2"), Message.STATUS_RETRYING);
		verify(test1MessageStore, times(0)).updateStatus(any(MessageCriteria.class), anyByte());
		verify(test2MessageStore).updateStatus(any(MessageCriteria.class), anyByte());
		verify(defaultMessageStore, times(0)).updateStatus(any(MessageCriteria.class), anyByte());
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		
		messageStore.updateStatus(new MessageCriteria().type("other"), Message.STATUS_RETRYING);
		verify(test1MessageStore, times(0)).updateStatus(any(MessageCriteria.class), anyByte());
		verify(test2MessageStore, times(0)).updateStatus(any(MessageCriteria.class), anyByte());
		verify(defaultMessageStore).updateStatus(any(MessageCriteria.class), anyByte());
	}
	
	@Test
	public void testListMultipleMessageStores() throws Exception {
		
		// mock the message stores
		MessageStore test1MessageStore = mock(MessageStore.class);
		Mockito.when(test1MessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		
		MessageStore test2MessageStore = mock(MessageStore.class);
		Mockito.when(test2MessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		
		MessageStore defaultMessageStore = mock(MessageStore.class);
		Mockito.when(defaultMessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		
		// create the multi message store
		MultiMessageStore messageStore = new MultiMessageStore();
		messageStore.setDefaultMessageStore(defaultMessageStore);
		messageStore.addMessageStore("test1", test1MessageStore);
		messageStore.addMessageStore("test2", test2MessageStore);
		
		Collection<Message> messages = messageStore.list(new MessageCriteria());
		verify(test1MessageStore).list(any(MessageCriteria.class));
		verify(test2MessageStore).list(any(MessageCriteria.class));
		verify(defaultMessageStore).list(any(MessageCriteria.class));
		Assert.assertEquals(3, messages.size());
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		Mockito.when(test1MessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		Mockito.when(test2MessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		Mockito.when(defaultMessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		
		messages = messageStore.list(new MessageCriteria().type("test1"));
		verify(test1MessageStore).list(any(MessageCriteria.class));
		verify(test2MessageStore, times(0)).list(any(MessageCriteria.class));
		verify(defaultMessageStore, times(0)).list(any(MessageCriteria.class));
		Assert.assertEquals(1, messages.size());
		
		// reset the mocks
		Mockito.reset(test1MessageStore, test2MessageStore, defaultMessageStore);
		Mockito.when(test1MessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		Mockito.when(test2MessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		Mockito.when(defaultMessageStore.list(any(MessageCriteria.class)))
			.thenReturn(Collections.singletonList(new Message()));
		
		messages = messageStore.list(new MessageCriteria().type("other"));
		verify(test1MessageStore, times(0)).list(any(MessageCriteria.class));
		verify(test2MessageStore, times(0)).list(any(MessageCriteria.class));
		verify(defaultMessageStore).list(any(MessageCriteria.class));
		Assert.assertEquals(1, messages.size());
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullAdditionalMessageStore() throws Exception {
		MultiMessageStore messageStore = new MultiMessageStore();
		messageStore.addMessageStore("sms", null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithEmptyTypeMessageStore() throws Exception {
		
		MessageStore mockMessageStore = mock(MessageStore.class);
		
		MultiMessageStore messageStore = new MultiMessageStore();
		messageStore.addMessageStore(null, mockMessageStore);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailWithNullDefaultMessageStore() throws Exception {
		MultiMessageStore messageStore = new MultiMessageStore();
		messageStore.setDefaultMessageStore(null);
	}
}
