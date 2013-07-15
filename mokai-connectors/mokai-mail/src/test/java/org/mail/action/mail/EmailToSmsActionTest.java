package org.mail.action.mail;

import org.mokai.Message;
import org.mokai.action.mail.EmailToSmsAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmailToSmsActionTest {

	@Test
	public void shouldChangeMessageWithSmsTo() throws Exception {
		Message message = new Message()
			.setProperty("to", "german.escobarc@gmail.com")
			.setProperty("from", "test@localhost.com")
			.setProperty("subject", "574003222222")
			.setProperty("text", "This is a test");

		EmailToSmsAction action = new EmailToSmsAction();
		action.setSmsTo("573001222222");

		action.execute(message);

		Assert.assertEquals(message.getProperty("to", String.class), "573001222222");
		Assert.assertEquals(message.getProperty("emailTo", String.class), "german.escobarc@gmail.com");
		Assert.assertEquals(message.getProperty("from", String.class), "12345");
		Assert.assertEquals(message.getProperty("emailFrom", String.class), "test@localhost.com");
		Assert.assertEquals(message.getProperty("text", String.class), "574003222222 - This is a test");
	}

	@Test
	public void shouldChangeMessageWithUseSubjectAsTo() throws Exception {
		Message message = new Message()
			.setProperty("to", "german.escobarc@gmail.com")
			.setProperty("from", "test@localhost.com")
			.setProperty("subject", "574003222222")
			.setProperty("text", "This is a test");

		EmailToSmsAction action = new EmailToSmsAction();
		action.setUseSubjectAsTo(true);
		action.setSmsFrom("54321");

		action.execute(message);

		Assert.assertEquals(message.getProperty("to", String.class), "574003222222");
		Assert.assertEquals(message.getProperty("emailTo", String.class), "german.escobarc@gmail.com");
		Assert.assertEquals(message.getProperty("from", String.class), "54321");
		Assert.assertEquals(message.getProperty("emailFrom", String.class), "test@localhost.com");
		Assert.assertEquals(message.getProperty("text", String.class), "This is a test");
	}

	@Test
	public void shouldCutLongMessages() throws Exception {
		Message message = new Message()
			.setProperty("to", "german.escobarc@gmail.com")
			.setProperty("from", "test@localhost.com")
			.setProperty("subject", "574003222222")
			.setProperty("text", getText(201));

		EmailToSmsAction action = new EmailToSmsAction();
		action.setUseSubjectAsTo(true);
		action.setMaxTextLength(200);

		action.execute(message);

		Assert.assertEquals(message.getProperty("text", String.class).length(), 200);
	}

	@Test
	public void shouldNotCutIfTextLengthLessThanMaxTextLength() throws Exception {
		Message message = new Message()
			.setProperty("to", "german.escobarc@gmail.com")
			.setProperty("from", "test@localhost.com")
			.setProperty("subject", "574003222222")
			.setProperty("text", getText(199));

		EmailToSmsAction action = new EmailToSmsAction();
		action.setUseSubjectAsTo(true);
		action.setMaxTextLength(200);

		action.execute(message);

		Assert.assertEquals(message.getProperty("text", String.class).length(), 199);
	}

	private String getText(int length) {
		StringBuilder buffer = new StringBuilder();
		for (int i=0; i < length; i++) {
			buffer.append("a");
		}

		return buffer.toString();
	}

}
