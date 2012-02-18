package org.mokai.action.test;

import org.mokai.Message;
import org.mokai.action.UpdateAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateActionTest {
	
	@Test
	public void shouldAddField() throws Exception {
		Message message = new Message()
			.setProperty("to", "test-to")
			.setProperty("from", "test-from");
	
		UpdateAction updateAction = new UpdateAction();
		updateAction.setField("new");
		updateAction.setValue("test-new");
		
		updateAction.execute(message);
		
		Assert.assertEquals(message.getProperties().size(), 3);
		Assert.assertEquals(message.getProperty("new", String.class), "test-new");
		Assert.assertEquals(message.getProperty("to", String.class), "test-to");
		Assert.assertEquals(message.getProperty("from", String.class), "test-from");
		
	}

	@Test
	public void shouldUpdateField() throws Exception {
		
		Message message = new Message()
			.setProperty("to", "test-to")
			.setProperty("from", "test-from");
		
		UpdateAction updateAction = new UpdateAction();
		updateAction.setField("to");
		updateAction.setValue("new-to");
		
		updateAction.execute(message);
		
		Assert.assertEquals(message.getProperties().size(), 2);
		Assert.assertEquals(message.getProperty("to", String.class), "new-to");
		Assert.assertEquals(message.getProperty("from", String.class), "test-from");
		
	}
	
}
