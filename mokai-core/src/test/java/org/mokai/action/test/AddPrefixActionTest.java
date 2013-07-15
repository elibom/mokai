package org.mokai.action.test;

import org.mokai.Message;
import org.mokai.action.AddPrefixAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AddPrefixActionTest {

	@Test
	public void shouldAddPrefixIfNotPresent() throws Exception {
		AddPrefixAction action = new AddPrefixAction();
		action.setField("to");
		action.setPrefix("57");

		Message message = new Message();
		message.setProperty("to", "11");

		action.execute(message);

		Assert.assertEquals(message.getProperty("to", String.class), "5711");
	}

	@Test
	public void shouldAddPrefixIfFieldDoesntExists() throws Exception {
		AddPrefixAction action = new AddPrefixAction();
		action.setField("to");
		action.setPrefix("57");

		Message message = new Message();

		action.execute(message);

		Assert.assertEquals(message.getProperty("to", String.class), "57");

	}

	@Test
	public void shouldNotAddPrefixIfPresent() throws Exception {
		AddPrefixAction action = new AddPrefixAction();
		action.setField("to");
		action.setPrefix("57");

		Message message = new Message();
		message.setProperty("to", "5711");

		action.execute(message);

		Assert.assertEquals(message.getProperty("to", String.class), "5711");

	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailIfPrefixNotSet() throws Exception {
		AddPrefixAction action = new AddPrefixAction();
		action.setField("to");

		action.execute(new Message());
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailIfFieldNotSet() throws Exception {
		AddPrefixAction action = new AddPrefixAction();
		action.setPrefix("57");

		action.execute(new Message());
	}

}
