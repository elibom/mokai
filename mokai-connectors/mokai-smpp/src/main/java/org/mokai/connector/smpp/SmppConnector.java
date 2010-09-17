package org.mokai.connector.smpp;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.mokai.ExecutionException;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.Processor;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connector that sends and receives messages to an SMSC using the SMPP protocol.
 * When a message is received, it will create a {@link Message} object and will 
 * add the following properties to it:
 * 
 * <ul>
 * 	<li>"to" - the destination address of the SMS message.</li>
 *  <li>"from" - the source address of the SMS message.</li>
 *  <li>"text" - the text of the SMS message.</li>
 * </ul>
 * 
 * To check the properties that this component expects check the {@link #process(Message)}
 * method documentation.
 * 
 * @author German Escobar
 */
@Name("SMPP")
@Description("Sends and receives messages using SMPP protocol")
public class SmppConnector implements Processor, Serviceable, Monitorable, 
		ExposableConfiguration<SmppConfiguration> {
	
	private Logger log = LoggerFactory.getLogger(SmppConnector.class);
	
	/**
	 * To produce the message we receive from the SMSC
	 */
	@Resource
	private MessageProducer messageProducer;
	
	/**
	 * Used to retrieve processed messages when a delivey receipt arrives
	 */
	@Resource
	private MessageStore messageStore;
	
	private SMPPSession session = new SMPPSession();
	
	/**
	 * The configuration of the connector
	 */
	private SmppConfiguration configuration;
	
	/**
	 * Tells if the connector is started so we keep trying to connect
	 * in case of failure.
	 */
	private boolean started = false;
	
	/**
	 * The status of the connector.
	 */
	private Status status = MonitorStatusBuilder.unknown();
	
	/**
	 * Constructor. Creates an instance with the default configuration
	 * information.
	 * 
	 * @see SmppConfiguration
	 */
	public SmppConnector() {
		this(new SmppConfiguration());
	}
	
	/**
	 * Constructor. Creates an instance with the supplied configuration
	 * object.
	 * @param configuration the configuration for this instance.
	 */
	public SmppConnector(SmppConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Binds to the SMSC with the parameters of the {@link SmppConfiguration} 
	 * instance. If the first attempt fails, it will start a new thread to keep 
	 * trying the connection as long as the connector is started.
	 */
	@Override
	public final void doStart() throws Exception {
		
		log.debug("starting SmppConnector ... ");
		
		started = true;
		
		// try to connect in this same thread (only one attempt)
		// the reason for this is that if the processor service
		// has queued messages and it is started without setting
		// up the connection, some messages will fail until the 
		// connection is up
		new ConnectionThread(1, 0).run();
		
		// if we couldn't connect the first time, start a thread to keep trying
		if (status.equals(Status.FAILED)) {
			new Thread(
				new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())
			).start();
		}
	}
	
	/**
	 * Helper method that creates a org.jsmpp.session.SMPPSession and tries 
	 * to bind.
	 * @return the bound SMPPSession
	 * @throws IOException
	 */
	private SMPPSession createAndBindSession() throws IOException {

		session = new SMPPSession();
		
		session.setEnquireLinkTimer(configuration.getEnquireLinkTimer());
		
		// this will handle received messages
		session.setMessageReceiverListener(new MessageReceiverListenerImpl());
		
		// this will handle connection problems
		session.addSessionStateListener(new SessionStateListener() { 
            public void onStateChange(SessionState newState, SessionState oldState, Object source) {
                if (newState.equals(SessionState.CLOSED)) {
                    log.warn("loosing connection to: " + getConfiguration().getHost() + " - trying to reconnect...");
                    
                    status = MonitorStatusBuilder.failed("connection lost");
                    
                    session.close();
                    
                    // start a new thread to try to reconnect
                    new Thread(
                    		new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())
                    		).start();
                }
            }
        });
		
		// build the bind request
		BindRequest bindRequest = buildBindRequest();
		
		// bind
		log.debug("starting bind ... ");
		session.connectAndBind(
                configuration.getHost(),
                configuration.getPort(),
                new BindParameter(
                        bindRequest.bindType,
                        configuration.getSystemId(),
                        configuration.getPassword(), 
                        configuration.getSystemType(),
                        bindRequest.ton,
                        bindRequest.npi,
                        ""));
		log.debug("connection bound");
		
		return session;
	}
	
	/**
	 * Helper method to build a BindRequest object that we'll use to bind.
	 * 
	 * @return the built BindRequest object.
	 */
	private BindRequest buildBindRequest() {
		
		BindRequest bindRequest = new BindRequest();
		
		// retrieve the bind TON from the configuration
		bindRequest.ton = TypeOfNumber.UNKNOWN;
		if (configuration.getBindTON() != null && !"".equals(configuration.getBindTON())) {
			bindRequest.ton = TypeOfNumber.valueOf(Byte.decode(configuration.getBindTON()));
		}
		
		// retrieve the bind NPI from the configuration
		bindRequest.npi = NumberingPlanIndicator.UNKNOWN;
		if (configuration.getBindNPI() != null && !"".equals(configuration.getBindNPI())) {
			bindRequest.npi = NumberingPlanIndicator.valueOf(Byte.valueOf(configuration.getBindNPI()));
		}
		
		// retrieve the bind type from the configuration
		bindRequest.bindType = BindType.BIND_TRX;
		if (configuration.getBindType().equals("r")) {
			bindRequest.bindType = BindType.BIND_RX;
		} else if (configuration.getBindType().equals("t")) {
			bindRequest.bindType = BindType.BIND_TX;
		}
		
		return bindRequest;
	}

	/**
	 * Unbinds from the SMSC.
	 */
	@Override
	public final void doStop() throws Exception {
		
		started = false;
		
		try {
			session.unbindAndClose();
		} catch (Exception e) {
			log.error("Exception closing session: " + e.getMessage(), e);
		}
		
		status = MonitorStatusBuilder.unknown();
	}
	
	/**
	 * Returns the status of the connector. 
	 * 
	 * <ul>
	 * 	<li>Status.UNKNOWN - if the connector is stopped.</li>
	 * 	<li>Status.OK - if the connector is bound to the SMSC.</li>
	 * 	<li>Status.FAILED - if the connection to the SMSC has failed.</li>
	 * </ul>
	 */
	@Override
	public final Status getStatus() {
		return status;
	}

	/**
	 * Sends an SMS message to the SMSC. If not connected, an IllegalStateException
	 * will be thrown. This method expects the following properties keys:
	 * 
	 * <ul>
	 * 	<li>"to" - the destination of the SMS message</li>
	 * 	<li>"from - the short code of the SMS message</li>
	 * 	<li>"text" - the text of the SMS message</li>
	 * </ul>
	 * 
	 * The following parameters will be added as properties of the message after
	 * it is processed:
	 * <ul>
	 * 	<li>"commandStatus" - the command status returned by the SMSC.</li>
	 * 	<li>"messageId" - the generated message id from the SMSC if successful.</li>
	 * </ul>
	 */
	@Override
	public final void process(Message message) throws Exception {
		if (!status.equals(Status.OK)) {
			throw new IllegalStateException("SMPP client not connected.");
		}

		try {
		
			// convert the message into a SubmitSm operation
			SubmitSm submitSm = buildSubmitSm(message);
			
			// submit the short message
			String messageId = session.submitShortMessage(
	                submitSm.getServiceType(), 
	                TypeOfNumber.valueOf(submitSm.getSourceAddrTon()),
	                NumberingPlanIndicator.valueOf(submitSm.getSourceAddrNpi()),
	                submitSm.getSourceAddr(),
	                TypeOfNumber.valueOf(submitSm.getDestAddrTon()),
	                NumberingPlanIndicator.valueOf(submitSm.getDestAddrNpi()),
	                submitSm.getDestAddress(),
	                new ESMClass(),
	                submitSm.getProtocolId(),
	                submitSm.getPriorityFlag(),
	                submitSm.getScheduleDeliveryTime(),
	                submitSm.getValidityPeriod(),
	                new RegisteredDelivery(submitSm.getRegisteredDelivery()),
	                submitSm.getReplaceIfPresent(),
	                new GeneralDataCoding(
	                        false,
	                        false,
	                        MessageClass.CLASS1,
	                        Alphabet.ALPHA_DEFAULT),
	                (byte) 0,
	                submitSm.getShortMessage());
			
			// handle the messageId - it can be in hexadecimal format
			messageId = handleMessageId(messageId);
			
			// update the message
			message.setProperty("messageId", messageId);
			message.setProperty("commandStatus", 0);
			
		} catch (NegativeResponseException e) {
			
			log.warn("NegativeResponseException while submitting a message: " + e.getMessage(), e);
			message.setProperty("commandStatus", e.getCommandStatus());
			
			throw e;
			
		} catch (Exception e) {
			
			log.error("Exception while submitting a message: " + e.getMessage(), e);
			throw new ExecutionException(e);
			
		}
	}
	
	/**
	 * Helper method to create the SubmitSm operation based on the message we need to
	 * send. 
	 * 
	 * @param message the message we are processing
	 * @return a SubmitSm object with the information from the message.
	 */
	private SubmitSm buildSubmitSm(Message message) {
		
		SubmitSm submitSm = new SubmitSm();
		
		// set the short message
		if (message.getProperty("text", String.class) != null) {
			submitSm.setShortMessage(message.getProperty("text", String.class).getBytes());
		} else {
			submitSm.setShortMessage("".getBytes());
		}
		
		// set the registered delivery and alert on message delivery params
		submitSm.setRegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE.value());
		submitSm.setOptionalParametes(
				new OptionalParameter.Null(OptionalParameter.Tag.ALERT_ON_MESSAGE_DELIVERY)
		);
		
		// set the source address
		submitSm.setSourceAddr(message.getProperty("from", String.class));
		if (notEmpty(configuration.getSourceNPI())) {
			submitSm.setSourceAddrNpi(Byte.valueOf(configuration.getSourceNPI()));
		}
		if (notEmpty(configuration.getSourceTON())) {
			submitSm.setSourceAddrTon(Byte.valueOf(configuration.getSourceTON()));
		}
		
		// set the destination address
		submitSm.setDestAddress(message.getProperty("to", String.class));
		if (notEmpty(configuration.getDestNPI())) {
			submitSm.setDestAddrNpi(Byte.valueOf(configuration.getDestNPI()));
		}
		if (notEmpty(configuration.getDestTON())) {
			submitSm.setDestAddrTon(Byte.valueOf(configuration.getDestTON()));
		}
		
		return submitSm;
	}
	
	/**
	 * Helper method to convert hexadecimal message id into a decimal message id.
	 * The strategy we're taking here is to try to parse the message id as a 
	 * hexadecimal number. If an exception is thrown, we'll leave it as it was.
	 * 
	 * @param messageId the message id returned by the SMSC.
	 * @return the decimal messageId if it was hexadecimal or the same message
	 * id if it wasn't.
	 */
	private String handleMessageId(String messageId) {
		try {
			int decimalMessageId = Integer.parseInt(messageId, 16);
			log.debug("original messageId: '" + messageId + "', new messageId: '" + decimalMessageId + "'");
			
			messageId = decimalMessageId + "";
		} catch (Exception e) {
			log.debug("cannot convert message id " + messageId + " to decimal: " + e.getMessage());
		}
		
		return messageId;
	}

	/**
	 * This component supports messages with type "sms". Additional type of 
	 * messages can be supported overriding the {@link #supportsMessage(Message)} 
	 * method.
	 */
	@Override
	public final boolean supports(Message message) {
		if (message.isType(Message.SMS_TYPE)) {
			return true;
		}
		
		return supportsMessage(message);
	}
	
	/**
	 * Helper method that subclases can override to support additional type of messages
	 * @param message the Message object to be tested for support
	 * @return true if supports the message, false otherwise
	 */
	protected boolean supportsMessage(Message message) {
		return false;
	}

	/**
	 * Returns the information used to configure this connector.
	 */
	@Override
	public final SmppConfiguration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Helper method.
	 * @param s the String to validate
	 * @return true if the the String is not null or empty, false otherwise
	 */
	private boolean notEmpty(String s) {
		if (s != null && !"".equals(s)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Helper class to connect and bind to the SMSC. It implements java.lang.Runnable so it can run
	 * asynchronously.   
	 * 
	 * @author German Escobar
	 */
	private class ConnectionThread implements Runnable {
		
		/**
		 * The maximum number of retries before it gives up
		 */
		private int maxRetries;
		
		/**
		 * The initial delay before attempting the first connection
		 */
		private long initialDelay;
		
		/**
		 * Constructor. Creates an instance with the specified parameters.
		 * 
		 * @param maxRetries the maximum number of retries.
		 * @param initialDelay the initial delay before the first try.
		 */
		public ConnectionThread(int maxRetries, long initialDelay) {
			this.maxRetries = maxRetries;
			this.initialDelay = initialDelay;
		}

		@Override
		public void run() {
			log.info("schedule connect after " + initialDelay + " millis");
            try {
                Thread.sleep(initialDelay);
            } catch (InterruptedException e) {
            }

            // keep trying while the connector is started and until it exceeds the max retries or successfully connects 
            int attempt = 0;
            while (attempt < maxRetries 
            		&& started 
            		&& (session == null || session.getSessionState().equals(SessionState.CLOSED))) {
            	
                try {
                    log.info("trying to connect to " + getConfiguration().getHost() + " - attempt #" + (++attempt) + "...");
                    
                    // try to bind
                    session = createAndBindSession();
                    
                    // if bound, change the status and show log that we are connected
                    status = MonitorStatusBuilder.ok();
                    log.info("connected to '" + configuration.getHost() + ":" + configuration.getPort() + "'");
                    
                } catch (IOException e) {
                	
                	// log the exception and change status
                    logException(e, attempt == 1);
                    status = MonitorStatusBuilder.failed("could not connect", e);
                    
                    // close session just in case
                    session.close();
                    
                    // wait the configured delay between reconnects
                    try {
                        Thread.sleep(configuration.getReconnectDelay());
                    } catch (InterruptedException ee) {
                    }
                }
            }
		}
		
		/**
		 * Helper method to log the exception when fail. We don't want to print the exception 
		 * on every attempt,
		 * just the first time
		 * @param e
		 * @param firstTime
		 */
		private void logException(Exception e, boolean firstTime) {
			
			String logError = "failed to connect to '" + configuration.getHost() + ":" 
					+ configuration.getPort() + "'";
			
			// print the exception only the first time
			if (firstTime) {
				log.error(logError, e);
			} else {
				log.error(logError + ": " + e.getMessage());
			}
		}
		
	}
	
	/**
	 * The default message handler of incoming messages from the SMSC.
	 * 
	 * @author German Escobar
	 */
	private class MessageReceiverListenerImpl implements MessageReceiverListener {

		@Override
		public void onAcceptAlertNotification(AlertNotification alertNotification) {
			log.debug("received deliverSm: " + alertNotification);
		}

		@Override
		public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
			
			log.info("received deliverSm: " + deliverSm);
			
			if (deliverSm.isSmscDeliveryReceipt()) {
				log.info("DeliveryReceipt short message: " + new String(deliverSm.getShortMessage()));
				
				try {
				
					// create the message based on the delivery receipt
					Message message = SmsMessageTranslator.createDeliveryReceipt(deliverSm);
					
					if (messageStore != null) {
						findAndUpdateOriginalMessage(message, messageStore);
					}
					
					// producer the message
					produceMessage(message);
					
				} catch (InvalidDeliveryReceiptException e) {
					log.error("InvalidDeliveryReceiptException while trying to parse deliveSm: " 
							+ e.getMessage(), e);
				}
			} else {
				
				log.info("DeliverySm short message: " + new String(deliverSm.getShortMessage()));
				
				// create the message based on the deliverSm
				Message message = SmsMessageTranslator.createDeliverSm(deliverSm);
				
				// produce the message
				produceMessage(message);
			}
		}

		@Override
		public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
			log.debug("received dataSm: " + dataSm);
			return null;
		}
		
		/**
		 * Helper method to produce a message. If the {@link MessageProducer}
		 * is null, it just logs and ignores the message.
		 * 
		 * @param message the {@link Message} we are going to produce
		 */
		private void produceMessage(Message message) {
			
			if (messageProducer != null) {
				messageProducer.produce(message);
			} else {
				// this should not happen
				log.error("MessageProducer is null ... ignoring message");
			}
		}
		
		private void findAndUpdateOriginalMessage(Message message, MessageStore messageStore) {
			
			String messageId = message.getProperty("messageId", String.class);
			log.debug("looking for message with SMSC message id: " + messageId);
			
			// create the criteria
			MessageCriteria criteria = new MessageCriteria();
			criteria.addProperty("smsc_messageid", messageId);
			
			// retrieve the messages
			Collection<Message> messages = messageStore.list(criteria);
			if (messages != null && !messages.isEmpty()) {
				
				Message originalMessage = messages.iterator().next();
				log.debug("message with SMSC message id: " + messageId + " found");
				
				// set the information in the message we are routing
				message.setProperty("smsId", originalMessage.getId());
				message.setReference(originalMessage.getReference());
				
				// update original message
				String receiptStatus = message.getProperty("finalStatus", String.class);
				originalMessage.setProperty("receiptStatus", receiptStatus);
				originalMessage.setProperty("receiptTime", message.getProperty("doneDate", Date.class));
				messageStore.saveOrUpdate(originalMessage);
				
				log.debug("message with id " + originalMessage.getId() + " updated with receiptStatus: " 
						+ receiptStatus);
			}
		}	
	}
	
	private class BindRequest {
		
		public TypeOfNumber ton;
		public NumberingPlanIndicator npi;
		public BindType bindType;
		
	}
}
