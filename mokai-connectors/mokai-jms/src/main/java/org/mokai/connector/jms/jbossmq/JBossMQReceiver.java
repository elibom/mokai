package org.mokai.connector.jms.jbossmq;

import java.util.Enumeration;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.Receiver;
import org.mokai.Serviceable;
import org.mokai.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author German Escobar
 */
public class JBossMQReceiver implements Receiver, ExposableConfiguration<JBossMQConfiguration>, 
		Serviceable, Monitorable, MessageListener, ExceptionListener {
	
	private Logger log = LoggerFactory.getLogger(JBossMQReceiver.class);
	
	@Resource
	private MessageProducer messageProducer;
	
	private Connection connection;
	private Session session;	
	private javax.jms.MessageConsumer consumer;
	
	private JBossMQConfiguration configuration;
	
	private boolean started = false;
	
	private Status status = MonitorStatusBuilder.unknown();
	
	public JBossMQReceiver() {
		this(new JBossMQConfiguration());
	}
	
	public JBossMQReceiver(JBossMQConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public final void doStart() throws Exception {
		
		log.debug("starting JBossMQReceiver ... ");
		
		started = true;
		
		// try to start in the same thread, dont retry
		new ConnectionThread(1, 0).run();
		
		// if we couldn't connect, start a thread to keep trying
		if (status.equals(Status.FAILED)) {
			new Thread(
					new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())
			).start();
		}
	}

	@Override
	public final void doStop() throws Exception {
		try {
			started = false;
			status = MonitorStatusBuilder.unknown();
			
			if (connection != null) {
				connection.stop();
			}
			if (consumer != null) {
				consumer.close();
			}
			if (session != null) {
				session.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			log.error("Exception while stopping JBossMQReceiver: " + e.getMessage(), e);
		}
	}
	
	@Override
	public final JBossMQConfiguration getConfiguration() {
		return configuration;
	}
	
	@Override
	public final void onMessage(javax.jms.Message jmsMessage) {
		
		try {
			// create the message
			Message message = new Message();
			
			// handle the message properties
			handleMessageProperties(jmsMessage, message);
			
			// handle jms message body
			handleMessageBody(jmsMessage, message);
			
			// finally, produce the message
			messageProducer.produce(message);
			
		} catch (JMSException e) {
			log.error("Exception receiving message from JBossMQ: " + e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void handleMessageProperties(javax.jms.Message jmsMessage, Message message) throws JMSException {
		
		// retrieve the type of the message
		String type = jmsMessage.getStringProperty("type");
		if (type != null) {
			message.setType(type);
		} else {
			message.setType(Message.SMS_TYPE);
		}
		
		// retrieve the reference
		String reference = jmsMessage.getStringProperty("reference");
		if (reference != null) {
			message.setReference(reference);
		}
		
		// retrieve the account and password
		String account = jmsMessage.getStringProperty("account");
		if (account != null) {
			message.setAccountId(account);
		}
		
		String password = jmsMessage.getStringProperty("password");
		if (password != null) {
			message.setPassword(password);
		}
		
		// retrieve properties
		Enumeration propertyNames = jmsMessage.getPropertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			Object propertyValue = jmsMessage.getObjectProperty(propertyName);
			
			String key = propertyName;
			
			// check if there is a mapper and apply it
			if (configuration.getMapper().containsKey(key)) {
				key = configuration.getMapper().get(key);
			}
			
			message.setProperty(key, propertyValue);
		}
	}
	
	private void handleMessageBody(javax.jms.Message jmsMessage, Message message) throws JMSException {
		// check if we have to map the body to a property
		boolean mapBodyToProperty = false;
		String bodyMapperKey = configuration.getBodyMapper();
		if (bodyMapperKey != null && !"".equals(bodyMapperKey)) {
			mapBodyToProperty = true;
		}
		
		// retrieve body
		if (javax.jms.TextMessage.class.isInstance(jmsMessage)) {
			
			javax.jms.TextMessage textJmsMessage = (javax.jms.TextMessage) jmsMessage;
			String body = textJmsMessage.getText();
			
			if (mapBodyToProperty) {
				message.setProperty(bodyMapperKey, body);
			} else {
				message.setBody(body);
			}
		} else if (javax.jms.ObjectMessage.class.isInstance(jmsMessage)) {
			
			javax.jms.ObjectMessage objectJmsMessage = (javax.jms.ObjectMessage) jmsMessage;
			Object body = objectJmsMessage.getObject();
			
			if (mapBodyToProperty) {
				message.setProperty(bodyMapperKey, body);
			} else {
				message.setBody(body);
			}
		}
	}

	@Override
	public final void onException(JMSException e) {
		status = MonitorStatusBuilder.failed("connection lost: " + e.getMessage(), e);
		
		new Thread(new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())).start();
	}

	@Override
	public final Status getStatus() {
		return status;
	}
	
	/**
	 * Connects to the JMS Server. 
	 * 
	 * @author German Escobar
	 */
	private class ConnectionThread implements Runnable {
		
		private int maxRetries;
		private long initialReconnectDelay;
		
		public ConnectionThread(int maxRetries, long initialReconnectDelay) {
			this.maxRetries = maxRetries;
			this.initialReconnectDelay = initialReconnectDelay;
		}


		@Override
		public void run() {
			
			log.info("schedule connect after " + initialReconnectDelay + " millis");
            try {
                Thread.sleep(initialReconnectDelay);
            } catch (InterruptedException e) {}
            
            int attempt = 0;
            boolean connected = false;
            while (attempt < maxRetries && started && !connected) {
            	log.info("trying to connect to " + getConfiguration().getHost() + " - attempt #" + (++attempt) + "...");
            	
            	// save the current thread class loader
            	ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
            	
            	// set this class class loader to the current thread class loader
            	Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            	
				try {					
					Properties properties = new Properties();
					properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
					properties.put(Context.URL_PKG_PREFIXES, "org.jnp.interfaces");
					properties.put(Context.PROVIDER_URL, configuration.getHost());
					
					
					InitialContext context = new InitialContext(properties);
					
					
					ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");
					connection = connectionFactory.createConnection();
					connection.setExceptionListener(JBossMQReceiver.this);
					
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					
					Destination queue = (Destination) context.lookup(configuration.getQueueName());
					consumer = session.createConsumer(queue);
					consumer.setMessageListener(JBossMQReceiver.this);
					
					connection.start();
					
					status = MonitorStatusBuilder.ok();
					connected = true;
					
					log.info("connected to queue '" + configuration.getQueueName() + "' (" + configuration.getHost() + ")");
					
				} catch (Exception e) {
					logException(e, attempt == 1);
					
					status = MonitorStatusBuilder.failed("could not connect", e);
					
					try {
                        Thread.sleep(configuration.getReconnectDelay());
                    } catch (InterruptedException ee) {}
				} finally {
					
					// change back the class loader
					Thread.currentThread().setContextClassLoader(threadLoader);
				}
            }
		}
		
		private void logException(Exception e, boolean firstTime) {
			// print the exception only one time
			String logError = "failed to connect to queue '" + configuration.getQueueName() 
					+ "' (" + configuration.getHost() + ")";
			
			if (firstTime) {
				log.error(logError, e);
			} else {
				log.error(logError + ": " + e.getMessage());
			}
		}
	}

}
