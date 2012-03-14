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
		
	}
	
}
