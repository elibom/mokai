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
	public void doStart() throws Exception {
		
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
	public void doStop() throws Exception {
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
	public JBossMQConfiguration getConfiguration() {
		return configuration;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(javax.jms.Message jmsMessage) {
		
		try {
			// retrieve the type of the message
			String type = jmsMessage.getStringProperty("type");
			if (type == null) {
				type = Message.SMS_TYPE;
			}
			
			Message message = new Message(type);
			
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
			
			// handle jms message body
			handleMessageBody(jmsMessage, message);
			
			// finally, produce the message
			messageProducer.produce(message);
			
		} catch (JMSException e) {
			log.error("Exception receiving message from JBossMQ: " + e.getMessage(), e);
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
	public void onException(JMSException e) {
		status = MonitorStatusBuilder.failed("connection lost: " + e.getMessage(), e);
		
		new Thread(new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())).start();
	}

	@Override
	public Status getStatus() {
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
					
				} catch (Exception e) {
					log.error("failed to connect to queue '" + configuration.getQueueName() 
							+ "' (" + configuration.getHost() + ")");
					
					status = MonitorStatusBuilder.failed("could not connect", e);
					
					try {
                        Thread.sleep(configuration.getReconnectDelay());
                    } catch (InterruptedException ee) {}
				}
            }
		}
		
		
	}

}
