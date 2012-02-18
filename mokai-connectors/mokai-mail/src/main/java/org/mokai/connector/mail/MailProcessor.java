package org.mokai.connector.mail;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;

/**
 * A connector that send email messages.
 * 
 * @author German Escobar
 */
@Name("Mail Processor")
@Description("Sends email messages")
public class MailProcessor implements Processor, ExposableConfiguration<MailProcessorConfig> {
	
	/**
	 * The configuration of the connector.
	 */
	private MailProcessorConfig configuration;
	
	/**
	 * Constructor. Creates a new instance with the default {@link MailProcessorConfig}
	 */
	public MailProcessor() {
		this(new MailProcessorConfig());
	}
	
	/**
	 * Constructor. Creates a new instance with the supplied {@link MailProcessorConfig} 
	 * 
	 * @param configuration the configuration that is going to be used.
	 */
	public MailProcessor(MailProcessorConfig configuration) {
		this.configuration = configuration;
	}

	@Override
	public void process(Message message) throws Exception {
		
		Properties props = new Properties();
		props.put("mail.smtp.host", configuration.getHost());
		props.put("mail.smtp.port", configuration.getPort());
		
		// check if tls is enabled
		if (configuration.isTls()) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		
		// check if authentication is enabled
		Session session = null;
		if (configuration.isAuth()) {
			
			props.put("mail.smtp.auth", "true");
			
			session = Session.getDefaultInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(configuration.getUsername(), configuration.getPassword());
				}
			});
			
		} else {
			
			session = Session.getDefaultInstance(props);
		}
		
		MimeMessage email = new MimeMessage(session);
		email.setFrom(new InternetAddress(getFrom(configuration, message)));
		email.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getProperty("to", String.class)));
		
		email.setSubject(getSubject(configuration, message));
		email.setText(message.getProperty("text", String.class));
		
		Transport.send(email);
	}
	
	/**
	 * Helper method. Tries to retrieve the "from" from the message or the configuration.
	 * 
	 * @param configuration
	 * @param message
	 * @return a String with the "from" that is going to be included in the email message.
	 */
	private String getFrom(MailProcessorConfig configuration, Message message) {
		
		if (message.getProperty("from") == null) {
			return configuration.getFrom();
		}
		
		return message.getProperty("from", String.class);
		
	}
	
	/**
	 * Helper method. Tries to retrieve the "subject" from the message of the configuration.
	 * 
	 * @param configuration
	 * @param message
	 * @return a String with the "subject" that is going to be included in the email message.
	 */
	private String getSubject(MailProcessorConfig configuration, Message message) {
		
		if (message.getProperty("subject") == null) {
			return configuration.getSubject();
		}
		
		return message.getProperty("subject", String.class);
	}

	@Override
	public boolean supports(Message message) {
		
		String to = message.getProperty("to", String.class);
		if (to == null) {
			return false;
		}
		
		Pattern pattern = Pattern.compile("^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.(([0-9]{1,3})|[a-zA-Z]{2,3}))?$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(to);
		
		return matcher.matches();
		
	}

	@Override
	public MailProcessorConfig getConfiguration() {
		return configuration;
	}

}
