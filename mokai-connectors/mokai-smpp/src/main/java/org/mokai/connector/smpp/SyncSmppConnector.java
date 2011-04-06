package org.mokai.connector.smpp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
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
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.Processor;
import org.mokai.ProcessorContext;
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
 * This implementation is based on JSMPP.
 * 
 * @author German Escobar
 */
@Name("SyncSMPP")
@Description("Sends and receives messages using SMPP protocol synchronously.")
public class SyncSmppConnector implements Processor, Serviceable, Monitorable, 
		ExposableConfiguration<SmppConfiguration> {
	
	private Logger log = LoggerFactory.getLogger(SyncSmppConnector.class);
	
	/**
	 * Holds information about the processor like the assigned id. It is used to add a 
	 * header on every log message and to create the file that will hold the sequence
	 * number.
	 */
	@Resource
	private ProcessorContext context;
	
	/**
	 * To produce the message we receive from the SMSC
	 */
	@Resource
	private MessageProducer messageProducer;
	
	/**
	 * Used to find messages and update them when a submit response or delivery receipt
	 * is received.
	 */
	@Resource
	private MessageStore messageStore;
	
	/**
	 * This is the JSmpp session object used to connect to the SMSC.
	 */
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
	 * Stores the delivery receipts until they are processed by the DeliveryReceiptThread 
	 * (defined in this same class)
	 */
	private List<DLRWrapper> deliveryReceipts = Collections.synchronizedList(new ArrayList<DLRWrapper>());
	
	/**
	 * Constructor. Creates an instance with the default configuration
	 * information.
	 * 
	 * @see SmppConfiguration
	 */
	public SyncSmppConnector() {
		this(new SmppConfiguration());
	}
	
	/**
	 * Constructor. Creates an instance with the supplied configuration
	 * object.
	 * @param configuration the configuration for this instance.
	 */
	public SyncSmppConnector(SmppConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Binds to the SMSC with the parameters of the {@link SmppConfiguration} 
	 * instance. If the first attempt fails, it will start a new thread to keep 
	 * trying the connection as long as the connector is started.
	 */
	@Override
	public final void doStart() throws Exception {
		
		log.debug(getLogHead() + "starting SmppConnector ... ");
		
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
	 * 
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
                    log.warn(getLogHead() + "loosing connection - trying to reconnect...");
                    
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
		session.setTransactionTimer(5000);
		log.debug(getLogHead() + "connection bound");
		
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
			log.error(getLogHead() + "Exception closing session: " + e.getMessage(), e);
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
	 * 
	 * This method is synchronized as it shouldn't be called concurrently.
	 */
	@Override
	public synchronized final void process(Message message) throws Exception {
		
		log.debug(getLogHead() + "processing message: " + message.getProperty("to", String.class) 
				+ " - " + message.getProperty("text", String.class));
		
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
			message.setProperty("commandStatus", e.getCommandStatus());
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
		if (configuration.isRequestDeliveryReceipts()) {
			submitSm.setRegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE.value());
			submitSm.setOptionalParametes(
					new OptionalParameter.Null(OptionalParameter.Tag.ALERT_ON_MESSAGE_DELIVERY)
			);
		}
		
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
			log.debug(getLogHead() + "original messageId: '" + messageId + "', new messageId: '" + decimalMessageId + "'");
			
			messageId = decimalMessageId + "";
		} catch (Exception e) {
			// we are leaving the messageId intact
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
	 * 
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
	
	private String getLogHead() {
		return "[" + context.getId() + "] ";
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
			
			String logError = getLogHead() + "failed to connect";
			
			// print the exception only the first time
			if (firstTime) {
				log.error(logError, e);
			} else {
				log.error(logError + ": " + e.getMessage());
			}
		}
		
	}
	
	/**
	 * The default message handler of incoming messages from the SMSC. It handles short messages
	 * and delivery receipts.
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
			
			if (deliverSm.isSmscDeliveryReceipt()) {
				
				// this is a delivery receipt, handle it
				handleDeliveryReceipt(deliverSm);
				
			} else {
				
				// this is a short message, handle it
				handleShortMessage(deliverSm);
			}
		}
		
		/**
		 * Helper method to handle a delivery receipt.
		 * 
		 * @param deliverSm
		 * @see SmsMessageTranslator#createDeliveryReceipt(DeliverSm)
		 */
		private void handleDeliveryReceipt(DeliverSm deliverSm) {
			log.debug("DeliveryReceipt short message: " + new String(deliverSm.getShortMessage()));
			
			try {
			
				// create the message based on the delivery receipt
				Message message = createDeliveryReceipt(deliverSm);
				
				DLRWrapper dlrWrapper = new DLRWrapper();
				dlrWrapper.message = message;
				
				deliveryReceipts.add(dlrWrapper);
				synchronized (deliveryReceipts) {
					deliveryReceipts.notifyAll();
				}
				
			} catch (InvalidDeliveryReceiptException e) {
				log.error("InvalidDeliveryReceiptException while trying to parse deliveSm: " 
						+ e.getMessage(), e);
			}
		}
		
		/**
		 * Helper method to handle a short message
		 * 
		 * @param deliverSm
		 * @see SmsMessageTranslator#createShortMessage(DeliverSm)
		 */
		private void handleShortMessage(DeliverSm deliverSm) {
			
			log.debug("DeliverySm short message: " + new String(deliverSm.getShortMessage()));
			
			// create the message based on the deliverSm
			Message message = new Message(Message.SMS_TYPE);
			message.setProperty("to", deliverSm.getDestAddress());
			message.setProperty("from", deliverSm.getSourceAddr());
			message.setProperty("text", new String(deliverSm.getShortMessage()));
			
			// produce the message
			produceMessage(message);
		}
		
		/**
		 * Creates a delivery receipt based on the DeliverSm argument.
		 * 
		 * @param deliverSm holds the information used to create the delivery
		 * receipt message.
		 * @return a {@link Message} with the following properties:
		 * <ul>
		 *   <li>to (String) - the destination of the delivery receipt</li>
		 *   <li>from (String) - the source of the delivery receipt</li>
		 *   <li>messageId (String) - the id of the message returned by the submit_sm operation.</li>
		 *   <li>submitted (Integer) - the number of submitted messages</li>
		 *   <li>submitDate (Date) - the date of the submit_sm operation.</li>
		 *   <li>delivered (Integer) - the number of delivered messages.</li>
		 *   <li>doneDate (Date) - the date where the message reached a final status.</li>
		 *   <li>finalStatus (String) - the final status of the message.</li>
		 * </ul>
		 * 
		 * @throws InvalidDeliveryReceiptException if the deliverSm argument can't be parsed
		 * to a delivery receipt.
		 */
		private Message createDeliveryReceipt(DeliverSm deliverSm) throws InvalidDeliveryReceiptException {
			
			DeliveryReceipt deliveryReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
			
			// create the message
			Message message = new Message(Message.DELIVERY_RECEIPT_TYPE);
			
			String to = deliverSm.getDestAddress();
			String from = deliverSm.getSourceAddr();
			
			message.setProperty("to", to);
			message.setProperty("from", from);
			
			// set the id
			message.setProperty("messageId", deliveryReceipt.getId());
			
			// set the number of submitted and submit date
			message.setProperty("submitted", deliveryReceipt.getSubmitted());
			message.setProperty("submitDate", deliveryReceipt.getSubmitDate());
			
			// set the number of delivered
			message.setProperty("delivered", deliveryReceipt.getDelivered());
			
			// set done date
			message.setProperty("doneDate", deliveryReceipt.getDoneDate());
			
			// set final status
			message.setProperty("finalStatus", deliveryReceipt.getFinalStatus().toString());
			
			return message;
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
		
		
	}
	
	/**
	 * This thread is started in the {@link #doStart()} method and it process the delivery receipts
	 * that are queued in the deliverReceipts list.
	 * 
	 * @author German Escobar
	 */
    class DeliveryReceiptThread extends Thread {
		
		public void run() {

			while (started) {
				try {
					if (status.equals(Status.OK)) {
						process();
						
						synchronized (deliveryReceipts) {
							deliveryReceipts.wait(500);
						}
					}
				} catch (Exception e) {
					
					log.warn(getLogHead() + "Exception receiving: " + e.getMessage(), e);
				}
			}
		}
		
		/**
		 * Helper method that will retrieve all the queued delivery receipt and will process them if
		 * the lastProcessTime is null or it hasn't been checked in the last 900 ms.
		 */
		private void process() {
			
			List<DLRWrapper> deliveryReceiptsCopy = new ArrayList<DLRWrapper>(deliveryReceipts);
			
			for (DLRWrapper dr : deliveryReceiptsCopy) { 
				if (dr.lastProcessedTime == null || (new Date().getTime() - dr.lastProcessedTime.getTime()) > 900) {
					process(dr);
				}
				
			}
		}
		
		/**
		 * Helper method that will process a delivery receipt. It will try to find the message that matches the
		 * id of the delivery receipt and update its status.
		 * 
		 * @param dr the delivery receipt to be processed.
		 */
		private void process(DLRWrapper dr) {
			
			if (messageStore == null) {
				log.warn("MessageStore is null: ignoring delivery receipt");
				return;
			}
		
			// retrieve the delivery receipt message and try to find the original message that originated the dr
			Message drMessage = dr.message;
		
			Message originalMessage = findOriginalMessage(drMessage); 
				
			if (originalMessage != null) {
				
				deliveryReceipts.remove(dr);
					
				// update original message
				String receiptStatus = drMessage.getProperty("finalStatus", String.class);
				Date doneDate = drMessage.getProperty("doneDate", Date.class);
					
				originalMessage.setProperty("receiptStatus", receiptStatus);
				originalMessage.setProperty("receiptTime", doneDate);
				messageStore.saveOrUpdate(originalMessage);
					
				log.debug("message with id " + originalMessage.getId() + " updated with receiptStatus: " 
						+ receiptStatus);
						
				// update the reference of the delivery receipt
				drMessage.setProperty("originalReference", originalMessage.getReference());
					
				messageProducer.produce(drMessage);
					
			} else {
				
				log.trace(getLogHead() + " could not find message with messageId: " + drMessage.getProperty("messageId", String.class) + " ... trying later");
				
				// we couldn't find a matching message, try later 
				dr.lastProcessedTime = new Date();
				dr.retries++;
			}
				
		}
		
		/**
		 * Helper method that will try to find the original message of the delivery receipt.
		 * 
		 * @param drMessage the delivery receipt message.
		 * @return the Message that originated the delivery receipt, or null if not found.
		 */
		private Message findOriginalMessage(Message drMessage) {
			
			Message originalMessage = null; // this is what we are returning 
			
			String messageId = drMessage.getProperty("messageId", String.class);
			String to = drMessage.getProperty("to", String.class);
			String from = drMessage.getProperty("from", String.class);
				
			MessageCriteria criteria = new MessageCriteria()
				.addProperty("destination", context.getId())
				.addProperty("smsc_messageid", messageId);

			long startTime = new Date().getTime();
			Collection<Message> messages = messageStore.list(criteria);
			long endTime = new Date().getTime();
			
			log.trace(getLogHead() + "retrieve message with smsc_messageid " + messageId + " took " 
					+ (endTime - startTime) + " milis");
				
			if (messages.size() > 1) {
				log.debug(messages.size() + " messages matched the id: " + messageId);
			}

			// iterate through the matched messages to find one that matches exactly
			Iterator<Message> iterMessages = messages.iterator();
			while (iterMessages.hasNext() && originalMessage == null) {		
				Message message = iterMessages.next();
					
				String mTo = message.getProperty("to", String.class);
				if (mTo.equals(to) || mTo.equals(from)) {
					originalMessage = message;
				}
			}
			
			return originalMessage;
			
		}
		
	}
	
	private class BindRequest {
		
		public TypeOfNumber ton;
		public NumberingPlanIndicator npi;
		public BindType bindType;
		
	}
	
	private class DLRWrapper {
		
		public Message message;
		public Date lastProcessedTime;
		public int retries = 0;
	}
}
