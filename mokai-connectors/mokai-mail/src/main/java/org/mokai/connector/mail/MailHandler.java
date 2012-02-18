package org.mokai.connector.mail;

import java.io.IOException;

import javax.mail.MessagingException;

import org.mokai.MessageProducer;

/**
 * Provides a flexible mechanism to handle a received email message. Used by {@link MailReceiver}
 * 
 * @author German Escobar
 */
public interface MailHandler {

	/**
	 * This method is called by the {@link MailReceiver} when an email arrives.
	 * 
	 * @param messageProducer
	 * @param email
	 * @throws MessagingException
	 * @throws IOException
	 */
	void handle(MessageProducer messageProducer, javax.mail.Message email) throws MessagingException, IOException;
	
}
