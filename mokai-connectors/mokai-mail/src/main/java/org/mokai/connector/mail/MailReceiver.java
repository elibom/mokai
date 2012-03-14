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
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
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

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * A connector that fetches an email server using IMAP or IMAPS using push (IDLE command).
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
	
	private IMAPStore store;
	
	private IMAPFolder folder;
	
	
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
		
		started = true;
		
		new Thread(new ConnectionThread()).start();
		
	}

	@Override
	public void doStop() throws Exception {
		started = false;
		
		if (folder != null) {
			try { folder.close(true); } catch (Exception e) { log.error(getLogHead() + "Exception closing folder: " + e.getMessage(), e); }
		}
		
		status = MonitorStatusBuilder.unknown();
	}


	@Override
	public MailReceiverConfig getConfiguration() {
		return configuration;
	}
	
	/**
	 * This is the actual thread that connects to the email server and subscribes to the folder for incoming messages.
	 * 
	 * @author German Escobar
	 */
	private class ConnectionThread implements Runnable {
		
		
		@Override
		public void run() {

			int attempt = 0;
            while (started) {

            	try {
            		
            		String timeout = "15000"; // max connection timeout - we could make this customizable but this is enough for now
            		Properties props = buildProperties(timeout);
		        		
		        	Session session = Session.getInstance(props, null);
		        	store = (IMAPStore) session.getStore();
		        	
		        	if (configuration.getPort() > 0) {
		        		store.connect(configuration.getHost(), configuration.getPort(), configuration.getUsername(), configuration.getPassword());
		        	} else {
		        		store.connect(configuration.getHost(), configuration.getUsername(), configuration.getPassword());
		        	}
		        		
		        	folder = (IMAPFolder) store.getFolder(configuration.getFolder());
		        	folder.open(Folder.READ_WRITE);	
		        	folder.setSubscribed(true);
		        	
		        	folder.addMessageCountListener(new MessageCountListener() {

						@Override
						public void messagesAdded(MessageCountEvent e) {
							log.debug(getLogHead() + "messages added triggered, fetching and handling messages ... ");
							handleMessages(folder);
						}

						@Override
						public void messagesRemoved(MessageCountEvent e) {}
		        		
		        	});
		        	
		        	status = MonitorStatusBuilder.ok();
		        	
		        	// call the IDLE command in a while as it returns as soon as other command is called
		        	while (started) {
		        		folder.idle();
		        		Thread.yield();
		        	}
		            	
	            } catch (Exception e) {
	                	
	                // log the exception and change status
	            	logException(e, attempt % 5 == 0);
	            	status = MonitorStatusBuilder.failed("could not connect", e);
	            	
	            	attempt ++;
	                	
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
                    Thread.sleep(configuration.getReconnectDelay());
                } catch (InterruptedException ee) {
                }
	            
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
			String protocol = configuration.isTls() ? "imaps" : "imap";
        	props.setProperty("mail.store.protocol", protocol);
        	
        	if (configuration.isTls()) {
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
		 * Helper method. Called when the messages count changes in the folder
		 * 
		 * @param folder the folder from which we are retriving the messages
		 */
		private void handleMessages(Folder folder) {
			
			try { 
				
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
				
			} catch (Exception e) {
				log.error(getLogHead() + "Exception retrieving messages: " + e.getMessage(), e); 
			}
			
		}
		
		/**
		 * Helper method. Retrieves unseen or all  messages from the specified folder according to the configuration.  
		 * 
		 * @param folder the folder from which we are retrieving the messages.
		 * @return an array of javax.mail.Message objects.
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
		 * 
		 * @param e
		 * @param doLog
		 */
		private void logException(Exception e, boolean doLog) {
			
			String logError = getLogHead() + "failed to connect to '" + configuration.getHost() + "'";
			
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
	 * This is the default mail handler.
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
