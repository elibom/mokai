package org.mokai.action.test;

import org.mokai.Message;
import org.mokai.action.AddSpacesAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AddSpacesActionTest {

	@Test
	public void shouldFillWithSpaces() throws Exception {
		AddSpacesAction action = new AddSpacesAction();
		action.setField("message");
		action.setLength(160);

		Message message = new Message();
		message.setProperty("message", "a");

		action.execute(message);

		Assert.assertEquals(message.getProperty("message", String.class).length(), 160);

		String compare = "a";
		for (int i=0; i < 159; i++) {
			compare += " ";
		}
		Assert.assertEquals(message.getProperty("message", String.class), compare);
	}

	@Test
	public void shouldFillWithSpacesIfFieldIsNull() throws Exception {
		AddSpacesAction action = new AddSpacesAction();
		action.setField("message");
		action.setLength(160);

		Message message = new Message();

		action.execute(message);

		Assert.assertEquals(message.getProperty("message", String.class).length(), 160);

		String compare = "";
		for (int i=0; i < 160; i++) {
			compare += " ";
		}
		Assert.assertEquals(message.getProperty("message", String.class), compare);
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailIfFieldNotSet() throws Exception {
		AddSpacesAction action = new AddSpacesAction();
		action.setLength(1);

		action.execute(new Message());
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailIfLengthNotPositive() throws Exception {
		AddSpacesAction action = new AddSpacesAction();
		action.setField("message");

		action.execute(new Message());
	}
}
