package org.mokai.connector.mail;

import org.mokai.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class MailProcessorTest {

	@Test
	public void shouldSupportValidEmails() throws Exception {
		MailProcessor processor = new MailProcessor();

		Assert.assertTrue(processor.supports(new Message().setProperty("to", "info@example.com")));
		Assert.assertTrue(processor.supports(new Message().setProperty("to", "info@49.98.23.3")));
		Assert.assertTrue(processor.supports(new Message().setProperty("to", "info@localhost")));
		Assert.assertTrue(processor.supports(new Message().setProperty("to", "german.escobar@exampletest.com.co")));
		Assert.assertTrue(processor.supports(new Message().setProperty("to", "german_escobar@example-test.com")));
	}

	@Test
	public void shouldNotSupportInvalidEmails() throws Exception {
		MailProcessor processor = new MailProcessor();

		Assert.assertFalse(processor.supports(new Message().setProperty("to", "invalid")));
		Assert.assertFalse(processor.supports(new Message().setProperty("to", "@invalid.com")));
	}

	@Test
	public void shouldSendEmailWithDefaultConfig() throws Exception {
		SimpleSmtpServer server = SimpleSmtpServer.start(2000); // this starts a fake STMP Server on the specified port

		MailProcessorConfig configuration = new MailProcessorConfig();
		configuration.setPort(2000);
		configuration.setAuth(false);
		MailProcessor processor = new MailProcessor(configuration);

		Message message = new Message();
		message.setProperty("to", "german.escobarc@gmail.com");
		message.setProperty("text", "This is a test");

		processor.process(message);

		Assert.assertTrue(server.getReceivedEmailSize() == 1);

		SmtpMessage email = (SmtpMessage) server.getReceivedEmail().next();
		Assert.assertTrue(email.getHeaderValue("Subject").equals("Mokai Message"));
		Assert.assertTrue(email.getHeaderValue("From").equals("mokai@localhost.com"));
		Assert.assertTrue(email.getBody().equals("This is a test"));

		server.stop();
	}

	@Test
	public void shouldSendEmailOverridingConfig() throws Exception {
		SimpleSmtpServer server = SimpleSmtpServer.start(2000); // this starts a fake STMP Server on the specified port

		MailProcessorConfig configuration = new MailProcessorConfig();
		configuration.setPort(2000);
		configuration.setAuth(false);
		MailProcessor processor = new MailProcessor(configuration);

		Message message = new Message();
		message.setProperty("to", "german.escobarc@gmail.com");
		message.setProperty("from", "test@localhost");
		message.setProperty("subject", "This is the subject");
		message.setProperty("text", "This is a test");

		processor.process(message);

		Assert.assertTrue(server.getReceivedEmailSize() == 1);

		SmtpMessage email = (SmtpMessage) server.getReceivedEmail().next();
		Assert.assertTrue(email.getHeaderValue("Subject").equals("This is the subject"));
		Assert.assertTrue(email.getHeaderValue("From").equals("test@localhost"));
		Assert.assertTrue(email.getBody().equals("This is a test"));

		server.stop();
	}
}
