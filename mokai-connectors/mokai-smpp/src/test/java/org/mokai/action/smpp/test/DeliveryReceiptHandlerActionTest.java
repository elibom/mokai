package org.mokai.action.smpp.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.mockito.Mockito;
import org.mokai.Message;
import org.mokai.action.smpp.DeliveryReceiptHandlerAction;
import org.mokai.annotation.Resource;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeliveryReceiptHandlerActionTest {
	
	@Test
	public void shouldUpdateMessageWithDeliveryReceiptInfo() throws Exception {
		
		Message m = new Message(Message.SMS_TYPE);
		m.setReference("1234");
		m.setId(1);
		m.setProperty("to", "3002175604");
		
		MessageStore messageStore = Mockito.mock(MessageStore.class);
		Mockito.when(messageStore.list(Mockito.any(MessageCriteria.class)))
				.thenReturn(Collections.singleton(m));
		
		Message deliveryReceipt = new Message(Message.DELIVERY_RECEIPT_TYPE);
		deliveryReceipt.setProperty("messageId", "1");
		deliveryReceipt.setProperty("finalStatus", "DELIVRD");
		Date doneDate = new Date();
		deliveryReceipt.setProperty("doneDate", doneDate);
		deliveryReceipt.setProperty("to", "3002175604");
		deliveryReceipt.setProperty("from", "3542");
		
		DeliveryReceiptHandlerAction action = new DeliveryReceiptHandlerAction();
		injectResource(messageStore, action);
		action.configure();
		
		action.execute(deliveryReceipt);
		
		Mockito.verify(messageStore, Mockito.timeout(2000)).saveOrUpdate(m);
		
		Assert.assertNotNull(m.getProperty("receiptStatus"));
		Assert.assertEquals(m.getProperty("receiptStatus"), "DELIVRD");
		
		Assert.assertNotNull(m.getProperty("receiptTime"));
		Assert.assertEquals(m.getProperty("receiptTime"), doneDate);
		
		action.destroy();
		
	}
	
	@Test
	public void shouldWaitForMessageToArrive() throws Exception {
		
		Message m = new Message(Message.SMS_TYPE);
		m.setReference("1234");
		m.setId(1);
		m.setProperty("to", "3002175604");
		
		MessageStore messageStore = Mockito.mock(MessageStore.class);
		Mockito.when(messageStore.list(Mockito.any(MessageCriteria.class)))
				.thenReturn(new ArrayList<Message>());
		
		final Message deliveryReceipt = new Message(Message.DELIVERY_RECEIPT_TYPE);
		deliveryReceipt.setProperty("messageId", "1");
		deliveryReceipt.setProperty("finalStatus", "DELIVRD");
		Date doneDate = new Date();
		deliveryReceipt.setProperty("doneDate", doneDate);
		deliveryReceipt.setProperty("to", "3542");
		deliveryReceipt.setProperty("from", "3002175604");
		
		final DeliveryReceiptHandlerAction action = new DeliveryReceiptHandlerAction();
		action.configure();
		injectResource(messageStore, action);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					action.execute(deliveryReceipt);
				} catch (Exception e) {
					Assert.fail(e.getMessage(), e);
				}
				
			}
			
		}).start();
		
		// wait for 2 seconds so we are sure no message arrived
		Thread.sleep(2000);
		
		Mockito.verify(messageStore, Mockito.never()).saveOrUpdate(Mockito.any(Message.class));
		
		Mockito.when(messageStore.list(Mockito.any(MessageCriteria.class)))
			.thenReturn(Collections.singleton(m));
		
		Mockito.verify(messageStore, Mockito.timeout(5000)).saveOrUpdate(m);
		
		action.destroy();
		
	}

	private void injectResource(Object resource, Object object) throws Exception {
		
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			
			if (field.isAnnotationPresent(Resource.class) 
					&& field.getType().isInstance(resource)) {
				field.setAccessible(true);
				field.set(object, resource);

			}
		}
	}
}
