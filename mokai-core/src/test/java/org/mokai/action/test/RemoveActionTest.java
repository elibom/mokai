package org.mokai.action.test;

import org.mokai.Message;
import org.mokai.action.RemoveAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RemoveActionTest {

	@Test
	public void shouldRemoveField() throws Exception {
		Message message = new Message()
			.setProperty("to", "test-to")
			.setProperty("from", "test-from");

		RemoveAction removeAction = new RemoveAction();
		removeAction.setField("to");

		removeAction.execute(message);

		Assert.assertEquals(message.getProperties().size(), 1);
		Assert.assertNull(message.getProperty("to"));
		Assert.assertEquals(message.getProperty("from", String.class), "test-from");
	}

	@Test
	public void shouldNotRemoveFieldIfDoesntExists() throws Exception {
		Message message = new Message()
			.setProperty("to", "test-to")
			.setProperty("from", "test-from");

		RemoveAction removeAction = new RemoveAction();
		removeAction.setField("unexistent");

		removeAction.execute(message);

		Assert.assertEquals(message.getProperties().size(), 2);
		Assert.assertEquals(message.getProperty("to", String.class), "test-to");
		Assert.assertEquals(message.getProperty("from", String.class), "test-from");
	}

}
