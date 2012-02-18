package org.mokai.connector.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.search.FlagTerm;

import org.mokai.Connector;
import org.mokai.ConnectorContext;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connector that fetches an email server using IMAP, IMAPS, POP3 or POP3S.
 * 
 * @author German Escobar
 */
@Name("Mail Receiver")
@Description("Fetches an email server to retrieve messages.")
public class MailReceiver implements Connector, ExposableConfiguration<MailReceiverConfig>, Serviceable, Monitorable {
	
	private Logger log = LoggerFactory.getLogger(MailReceiver.class);
	
	@Resource
	private ConnectorContext context;

	/**
	 * Used to route email messages inside the gateway.
	 */
	@Resource
	private MessageProducer messageProducer;
	
	/**
	 * The status of the connector.
	 */
	private Status status = Status.UNKNOWN;
	
	/**
	 * The configuration of the connector.
	 */
	private MailReceiverConfig configuration;
	
	/**
	 * This is what actually handles the fetched email messages (i.e. produces messages that are routed inside Mokai)
	 */
	private MailHandler mailHandler;
	
	/**
	 * Tells if the processor is started so we keep trying to connect
	 * in case of failure.
	 */
	private boolean started = false;
	
	/**
	 * Used to decide if the stack trace is logged or not - we don't want to log the same stack trace every time
	 */
	private int failedRetries = 0;
	
	/**
	 * Constructor. Creates an instance with a default {@link MailReceiverConfig} and {@link MailHandler}.
	 */
	public MailReceiver() {
		this(new MailReceiverConfig());
	}
	
	/**
	 * Constructor. Creates an instance with a default {@link MailReceiverConfig} and the supplied {@link MailHandler}.
	 * 
	 * @param mailHandler a custom object to handle the email messages.
	 */
	public MailReceiver(MailHandler mailHandler) {
		this(new MailReceiverConfig(), mailHandler);
	}
	
	/**
	 * Constructor. Creates an instance with the supplied {@link MailReceiverConfig} and a default {@link MailHandler}.
	 * 
	 * @param configuration the configuration that this instance is going to use.
	 */
	public MailReceiver(MailReceiverConfig configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * Constructor. Creates an instance with the supplied {@link MailReceiverConfig} and {@link MailHandler}. 
	 * 
	 * @param configuration the configuration that this instance is going to use.
	 * @param mailHandler a custom object to handle the email messages.
	 */
	public MailReceiver(MailReceiverConfig configuration, MailHandler mailHandler) {
		this.configuration = configuration;
		this.mailHandler = mailHandler;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void doStart() throws Exception {
		
		log.debug(getLogHead() + "starting MailReceiver ... ");
		
		// select parser
		mailHandler = new DefaultMailHandler();
		if (configuration.isEmailToSms()) {
			mailHandler = new EmailToSmsHandler();
		}
		
		started = true;
		
		new Thread(new WorkerThread()).start();
		
	}

	@Override
	public void doStop() throws Exception {
		started = false;
		status = MonitorStatusBuilder.unknown();
	}


	@Override
	public MailReceiverConfig getConfiguration() {
		return configuration;
	}
	
	/**
	 * This is the actual thread that fetches the email server according to the specified interval.
	 * 
	 * @author German Esocbar
	 */
	private class WorkerThread implements Runnable {

		@Override
		public void run() {

            while (started) {
            	
            	log.info(getLogHead() + "fetching messages from '" + configuration.getHost() + ":" + configuration.getUsername() + "'");
            	
            	Store store = null;
            	Folder folder = null;
	            	
            	try {
            		
            		long startTime = System.currentTimeMillis();
            		
            		// max connection timeout - we could make this customizable but this is enough for now
            		String timeout = "15000";
		            	
            		Properties props = buildProperties(timeout);
		        		
		        	Session session = Session.getInstance(props, null);
		        	store = session.getStore(configuration.getProtocol());
		        	
		        	if (configuration.getPort() > 0) {
		        		store.connect(configuration.getHost(), configuration.getPort(), configuration.getUsername(), configuration.getPassword());
		        	} else {
		        		store.connect(configuration.getHost(), configuration.getUsername(), configuration.getPassword());
		        	}
		        		
		        	folder = store.getFolder(configuration.getFolder());
		        	folder.open(Folder.READ_WRITE);	
		        	
		        	// retrieve and process messages
		        	javax.mail.Message[] messages = retrieveMessages(folder);
		        	
		    		for (javax.mail.Message m : messages) {
		    			
		    			// mark as seen
		    			m.setFlag(Flags.Flag.SEEN, true);
		    			
		    			// delete if configured that way
		    			if (configuration.isDelete()) {
		    				m.setFlag(Flags.Flag.DELETED, true);
		    			}
		    			
		    			mailHandler.handle(messageProducer, m);
		    			
		    		}
		    		
		    		long endTime = System.currentTimeMillis();
		    		log.info(getLogHead() + "fetching messages took " + (endTime - startTime) + " millis");
		            	
		            status = MonitorStatusBuilder.unknown();
		            
		            failedRetries = 0;
		            	
	            } catch (Exception e) {
	                	
	                // log the exception and change status
	            	logException(e, failedRetries % 5 == 0);
	            	status = MonitorStatusBuilder.failed("could not connect", e);
	            	
	            	failedRetries ++;
	                	
	            } finally {
	            	
	            	if (folder != null) {
	            		try { folder.close(true); } catch (Exception e) {}
	            	}
	            	if (store != null) {
	            		try { store.close(); } catch (Exception e) {}
	            	}
	            	
	            }
	            
		        // wait the configured delay between reconnects
	            try {
	            	Thread.sleep(configuration.getInterval() * 1000);
	            } catch (InterruptedException ee) {}
	            
            }   
			
		}
		
		/**
		 * Helper method. Creates and fills the properties that the java.mail.Session will use to connect. 
		 * 
		 * @param timeout a String with the timeout of the connection.
		 * @return a filled Properties object ready to be used be the javax.mail.Session.
		 */
		private Properties buildProperties(String timeout) {
			
			Properties props = System.getProperties();
        	props.setProperty("mail.store.protocol", configuration.getProtocol());
        	
        	if (configuration.getProtocol().endsWith("s")) {
                props.put("mail.pop3.starttls.enable", Boolean.TRUE);
                props.put("mail.imap.starttls.enable", Boolean.TRUE);
            }
        	
        	props.setProperty("mail.imap.connectiontimeout", timeout);
            props.setProperty("mail.imaps.connectiontimeout", timeout);
            props.setProperty("mail.pop3.connectiontimeout", timeout);
            props.setProperty("mail.pop3s.connectiontimeout", timeout);
            props.setProperty("mail.imap.timeout", timeout);
            props.setProperty("mail.imaps.timeout", timeout);
            props.setProperty("mail.pop3.timeout", timeout);
            props.setProperty("mail.pop3s.timeout", timeout);
            
            return props;
		}
		
		/**
		 * Helper method. Retrieves the messages 
		 * 
		 * @param folder
		 * @return
		 * @throws MessagingException
		 */
		private javax.mail.Message[] retrieveMessages(Folder folder) throws MessagingException {
			
			javax.mail.Message[] messages = null;
			
			if (configuration.isUnseen()) {
        		FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        		messages = folder.search(ft);
        	} else {
        		messages = folder.getMessages();
        	}
			
			return messages;
		}
		
		/**
		 * Helper method to log the exception when fail. We don't want to print the exception 
		 * on every attempt.
		 * @param e
		 * @param doLog
		 */
		private void logException(Exception e, boolean doLog) {
			
			String logError = getLogHead() + "failed to connect";
			
			// print the stacktrace only if doLog is true
			if (doLog) {
				log.error(logError, e);
			} else {
				log.error(logError + ": " + e.getMessage());
			}
		}
		
	}
	
	/**
	 * Helper method that returns the header that should be appended to all log messages.
	 * 
	 * @return the log header.
	 */
	private String getLogHead() {
		return "[processor=" + context.getId() + "] ";
	}
	
	/**
	 * This is the default mail handler (i.e. when {@link MailReceiverConfig#isEmailToSms()} is false).
	 * 
	 * @author German Escobar
	 */
	private class DefaultMailHandler implements MailHandler {

		/**
		 * Builds and produces a {@link Message} object using the supplied {@link MessageProducer}. It maps the email information into the 
		 * {@link Message} as it comes.
		 */
		@Override
		public void handle(MessageProducer messageProducer, javax.mail.Message email) throws MessagingException, IOException {
			
			String subject = email.getSubject();
			String from = stringAddress( email.getFrom() );
			String recipients = stringAddress( email.getAllRecipients() );
			String toRecipients = stringAddress( email.getRecipients(RecipientType.TO) );
			String ccRecipients = stringAddress( email.getRecipients(RecipientType.CC) );
			String bccRecipients = stringAddress( email.getRecipients(RecipientType.BCC) );
			
			String text = retrieveText(email);
			
			Message message = new Message();
			message.setProperty("recipients", recipients);
			message.setProperty("to", toRecipients);
			message.setProperty("cc", ccRecipients);
			message.setProperty("bcc", bccRecipients);
			message.setProperty("from", from);
			message.setProperty("subject", subject);
			message.setProperty("text", text);
			
			messageProducer.produce(message);
			
		}
		
	}
	
	/**
	 * This is the email-to-sms handler (i.e. when {@link MailReceiverConfig#isEmailToSms()} is false).
	 * 
	 * @author German Escobar
	 */
	private class EmailToSmsHandler implements MailHandler {

		/**
		 * Builds and produces a {@link Message} object using the supplied {@link MessageProducer}. It maps the email information into the 
		 * {@link Message} so that the "subject" of the message is expected to have a mobile number that will be set on the "to" property
		 * of the message.
		 */
		@Override
		public void handle(MessageProducer messageProducer, javax.mail.Message email) throws MessagingException, IOException {
			
			String subject = email.getSubject();
			String from = stringAddress( email.getFrom() );
			String recipients = stringAddress( email.getAllRecipients() );
			String toRecipients = stringAddress( email.getRecipients(RecipientType.TO) );
			String ccRecipients = stringAddress( email.getRecipients(RecipientType.CC) );
			String bccRecipients = stringAddress( email.getRecipients(RecipientType.BCC) );
			
			String text = retrieveText(email);
			
			Message message = new Message();
			message.setProperty("recipients", recipients);
			message.setProperty("emailTo", toRecipients);
			message.setProperty("cc", ccRecipients);
			message.setProperty("bcc", bccRecipients);
			message.setProperty("emailFrom", from);
			message.setProperty("subject", subject);
			message.setProperty("text", text);
			
			message.setProperty("to", subject);
			message.setProperty("from", configuration.getSmsFrom());
			
			messageProducer.produce(message);
			
		}
		
	}
	
	/**
	 * Helper method. Converts the addresses array to a string - separated by coma (,).
	 * 
	 * @param addresses an array of javax.mail.Address objects which we want to join.
	 * 
	 * @return a String with the addresses joined.
	 * @throws MessagingException
	 * @throws IOException
	 */
	private String stringAddress(Address[] addresses) throws MessagingException, IOException {
		
		if (addresses == null) {
			return "";
		}
		
		String ret = "";
		for (Address address : addresses) {
			ret += address.toString() + ",";
		}
		
		return ret.length() > 0 ? ret.substring(0, ret.length() - 1) : ret;
	}
	
	/**
	 * Helper Method. Retrieves the body of the email as a String.
	 * 
	 * @param message the javax.mail.Message from which we are going to retrieve the body.
	 * 
	 * @return A String with the body of the email message.
	 * @throws MessagingException
	 * @throws IOException
	 */
	private String retrieveText(javax.mail.Message message) throws MessagingException, IOException {
		
		Part part = message;
		
		if (Multipart.class.isInstance(message.getContent())) {
			part = ((Multipart) message.getContent()).getBodyPart(0);
		}

		// get the content type
		String contentType = part.getContentType();
		log.debug("Email contentType: " + contentType);

		InputStream is = part.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		    
		String text = "";
			
		String line = null;
		while ((line = reader.readLine()) != null) {
		    text += line;
		}
		    
		return text;
	}
	
}
