package org.mokai.connector.smpp;

import ie.omk.smpp.Address;
import ie.omk.smpp.Connection;
import ie.omk.smpp.event.ConnectionObserver;
import ie.omk.smpp.event.ReceiverExceptionEvent;
import ie.omk.smpp.event.ReceiverExitEvent;
import ie.omk.smpp.event.SMPPEvent;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.tlv.TLVTable;
import ie.omk.smpp.message.tlv.Tag;
import ie.omk.smpp.net.TcpLink;
import ie.omk.smpp.util.ASCIIEncoding;
import ie.omk.smpp.util.AlphabetEncoding;
import ie.omk.smpp.util.DefaultAlphabetEncoding;
import ie.omk.smpp.util.Latin1Encoding;
import ie.omk.smpp.util.UCS2Encoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.mokai.ConnectorContext;
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
import org.mokai.connector.smpp.SmppConfiguration.BindType;
import org.mokai.connector.smpp.SmppConfiguration.DlrIdConversion;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connector that sends and receives messages to an SMSC using the SMPP protocol.
 * This implementation is based on the SMPP API (http://smppapi.sourceforge.net/).
 *
 * @author German Escobar
 */
@Name("SMPP Connector")
@Description("Sends and receives messages using SMPP protocol")
public class SmppConnector implements Processor, Serviceable, Monitorable,
		ExposableConfiguration<SmppConfiguration> {

	private Logger log = LoggerFactory.getLogger(SmppConnector.class);

	/**
	 * The folder in which we will save the file with the sequence number.
	 */
	private static final String SEQUENCE_NUMBER_FOLDER = "data/connectors/smpp/";

	/**
	 * The extension of the file that will store the sequence number.
	 */
	private static final String SEQUENCE_NUMBER_EXT = ".seq";

	/**
	 * Holds information about the processor like the assigned id. It is used to add a
	 * header on every log message and to create the file that will hold the sequence
	 * number.
	 */
	@Resource
	private ConnectorContext context;

	/**
	 * Used to produce received messages from the SMSC into the gateway.
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
	 * The configuration of the connector
	 */
	private SmppConfiguration configuration;

	/**
	 * The status of the connector.
	 */
	private Status status = MonitorStatusBuilder.unknown();

	/**
	 * The smppapi Connection used to connect to the SMSC.
	 */
	private Connection connection;

	/**
	 * Tells if the processor is started so we keep trying to connect
	 * in case of failure.
	 */
	private volatile boolean started = false;

	private volatile boolean connecting = false;

	/**
	 * Tells if the session is bound.
	 */
	private volatile boolean bound;

	/**
	 * Stores the submit responses until they are processed by the SubmitSmResponseThread
	 * (defined in this same class).
	 */
	private List<SubmitSmResp> submitSmResponses = Collections.synchronizedList(new ArrayList<SubmitSmResp>());

	/**
	 * Stores the delivery receipts until they are processed by the DeliveryReceiptThread
	 * (defined in this same class)
	 */
	private List<DeliveryReceipt> deliveryReceipts = Collections.synchronizedList(new ArrayList<DeliveryReceipt>());

	/**
	 * Constructor. Creates an instance with the default configuration information.
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
	 * Binds to the SMSC with the parameters of the {@link LogicaConfiguration}
	 * instance. If the first attempt fails, it will start a new thread to keep
	 * trying the connection as long as the connector is started.
	 */
	@Override
	public void doStart() throws Exception {
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

		// sends enquire links periodically to check connection
		new EnquireLinkThread().start();

		// process the submit_sm response
		new SubmitSmResponseThread().start();

		// process the delivery receipts
		new DeliveryReceiptThread().start();
	}

	/**
	 * Unbinds from the SMSC.
	 */
	@Override
	public void doStop() throws Exception {
		started = false;
		status = MonitorStatusBuilder.unknown();

		if (!bound) {
			log.debug(getLogHead() + " connection not bound");
			return;
		}

		bound = false;
		try {
			connection.unbind();
		} catch (Exception e) {
			log.warn(getLogHead() + "Exception unbinding: " + e.getMessage(), e);
		}
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
	 */
	@Override
	public void process(Message message) throws Exception {
		int sequenceNumber = getSequenceNumber();
		log.debug(getLogHead() + "processing message with sequence '" + sequenceNumber + "', to '" + message.getProperty("to", String.class)
				+ "' and text '" + message.getProperty("text", String.class) + "'");

		if (!status.equals(Status.OK)) {
			throw new IllegalStateException("SMPP client not connected.");
		}

		int msgRefNum = new Random().nextInt(10000); // used if we send more than one segment

		String[] texts = getMessageTexts( message.getProperty("text", String.class) );
		for (int i=0; i < texts.length; i++) {
			log.trace("sending segment " + (i+1) + " of " + texts.length);

			SubmitSM request = buildSubmitSM(message);

			request.setMessage( getEncodedBytes(texts[i]) );
			request.setDataCoding( getDataCoding().getDataCoding() );
			request.setSequenceNum( sequenceNumber );

			if (texts.length > 1) {
				request.setOptionalParameter(Tag.SAR_TOTAL_SEGMENTS, texts.length);
				request.setOptionalParameter(Tag.SAR_SEGMENT_SEQNUM, i + 1);
				request.setOptionalParameter(Tag.SAR_MSG_REF_NUM, msgRefNum);
			}

			if (i == 0) {
				message.setProperty("sequenceNumber", request.getSequenceNum());
			}

			connection.sendRequest(request);

		}
	}

	private SubmitSM buildSubmitSM(Message message) {
		SubmitSM request = new SubmitSM();

		Address sourceAr = new Address();
		if (notEmpty(configuration.getSourceNPI())) {
			sourceAr.setNPI(Byte.valueOf(configuration.getSourceNPI()));
		}
		if (notEmpty(configuration.getSourceTON())) {
			sourceAr.setTON(Byte.valueOf(configuration.getSourceTON()));
		}
		sourceAr.setAddress(message.getProperty("from").toString());
		request.setSource(sourceAr);

		Address destAr = new Address();
		if (notEmpty(configuration.getDestNPI())) {
			destAr.setNPI(Byte.valueOf(configuration.getDestNPI()));
		}
		if (notEmpty(configuration.getDestTON())) {
			destAr.setTON(Byte.valueOf(configuration.getDestTON()));
		}
		destAr.setAddress(message.getProperty("to", String.class));
		request.setDestination(destAr);

		if (configuration.isRequestDeliveryReceipts()) {
			request.setRegistered((byte) 0x01);
		}

		return request;
	}

	private String[] getMessageTexts(String text) {
		if (text == null) {
			return new String[] { "" };
		}

		return text.split("(?<=\\G.{160})");
	}

	private byte[] getEncodedBytes(String text) throws IllegalStateException, UnsupportedEncodingException {
		AlphabetEncoding enc = getDataCoding();
		byte[] encodedBytes = enc.encodeString(text);

		StringBuffer strBytes = new StringBuffer();
		for (int i=0; i < encodedBytes.length; i++) {
			strBytes.append(" : ").append(encodedBytes[i]);
		}
		log.trace("encoded bytes of the message: " + strBytes);

		return encodedBytes;
	}

	private AlphabetEncoding getDataCoding() throws IllegalStateException, UnsupportedEncodingException {
		int dataCoding = configuration.getDataCoding();

		if (dataCoding == 0) {
			return new DefaultAlphabetEncoding();
		} else if (dataCoding == 1) {
			return new ASCIIEncoding();
		} else if (dataCoding == 3) {
			return new Latin1Encoding();
		} else if (dataCoding == 8) {
			return new UCS2Encoding();
		}

		throw new IllegalStateException("Data Coding " + configuration.getDataCoding() + " not recognized");
	}

	/**
	 * Helper method that will return a sequence number that is sent with the request to the
	 * SMSC. It reads the number from a file, increments it, saves it again in the file and
	 * returns it.
	 *
	 * @return the sequence number to use in the request that is going to be sent to the SMSC.
	 * @throws Exception if anything goes wrong.
	 */
	private synchronized int getSequenceNumber() throws Exception {
		int sequence = 1;

		// check if the file exists and read the number
		File file = new File(SEQUENCE_NUMBER_FOLDER + context.getId() + SEQUENCE_NUMBER_EXT);
		if (file.exists()) {
			BufferedReader in = null;

			try {
				in = new BufferedReader(new FileReader(file));
				sequence = Integer.parseInt(in.readLine());
			} finally {
				if (in != null) {
					try { in.close(); } catch (Exception e) {}
				}
			}
		}

		// create the directories if they aren't created already
		File fDir = new File(SEQUENCE_NUMBER_FOLDER);
		fDir.mkdirs();

		// increment the number and save it in the file
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(SEQUENCE_NUMBER_FOLDER + context.getId() + SEQUENCE_NUMBER_EXT));
			out.println(sequence + 1);
		} finally {
			if (out != null) {
				try { out.close(); } catch (Exception e) {}
			}
		}

		return sequence;
	}

	@Override
	public boolean supports(Message message) {
		String to = message.getProperty("to", String.class);

		if (to == null || "".equals(to)) {
			return false;
		}

		return to.matches("[0-9]*");
	}

	@Override
	public SmppConfiguration getConfiguration() {
		return configuration;
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
	public Status getStatus() {
		return status;
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
	 * Helper method that returns the header that should be appended to all log messages.
	 *
	 * @return the log header.
	 */
	private String getLogHead() {
		return "[processor=" + context.getId() + "] ";
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
			if (connecting) {
				log.debug(getLogHead() + "wont start connection thread because it looks like there is another already running");
				return;
			}

			connecting = true;
			log.info(getLogHead() + "schedule connect after " + initialDelay + " millis");
			try {
				Thread.sleep(initialDelay);
			} catch (InterruptedException e) {
			}

			// keep trying while the connector is started and until it exceeds the max retries or successfully connects
			int attempt = 0;
			while (attempt < maxRetries
					&& started
					&& !bound) {

				try {
					log.info(getLogHead() +  "trying to connect to " + getConfiguration().getHost() + " - attempt #" + (++attempt) + "...");

					// try to bind
					connection = bind();

					// if bound, change the status and show log that we are connected
					bound = true;
					status = MonitorStatusBuilder.ok();
					log.info(getLogHead() +  "connected to '" + configuration.getHost() + ":" + configuration.getPort() + "'");
					connecting = false;

				} catch (Exception e) {
					// log the exception and change status
					logException(e, attempt == 1);
					status = MonitorStatusBuilder.failed("could not connect", e);

					// close session just in case
					try { connection.closeLink(); } catch (Exception f) {}

					// wait the configured delay between reconnects
					try {
						Thread.sleep(configuration.getReconnectDelay());
					} catch (InterruptedException ee) {
					}

				}
			}

		}

		/**
		 * Helper method that creates an Connection and tries to bind.
		 *
		 * @return
		 * @throws Exception
		 */
		private Connection bind() throws Exception {
			TcpLink link = null;

			try {
				link = new TcpLink(configuration.getHost(), configuration.getPort());
				connection = new Connection(link, true);
				autoAckMessages(connection);

				MessageListener messageListener = new MessageListener();
				connection.addObserver(messageListener);

				connection.bind(getConnectionType(configuration), configuration.getSystemId(), configuration.getPassword(), configuration.getSystemType(),
						getBindTON(), getBindNPI(), null);

				BindResp bindResp = messageListener.getBindResponse(configuration.getBindTimeout());
				if (bindResp == null || bindResp.getCommandStatus() != 0) {
					// close the link if no response
					if (link != null) {
						try { link.close(); } catch (Exception f) {}
					}
					throw new Exception("Bind Response failed: " + bindResp);
				}

				return connection;
			} catch (Exception e) {
				// close the link if an exception was thrown
				if (link != null) {
					try { link.close(); } catch (Exception f) {}
				}

				throw e;
			}
		}

		private void autoAckMessages(Connection connection) {
			connection.autoAckLink(true);
			connection.autoAckMessages(true);
		}

		private int getConnectionType(SmppConfiguration configuration) {
			int type = Connection.TRANSCEIVER;
			if (configuration.getBindType().equals(BindType.RECEIVER)) {
				type = Connection.RECEIVER;
			} else if (configuration.getBindType().equals(BindType.TRANSMITTER)) {
				type = Connection.TRANSMITTER;
			}

			return type;
		}

		private int getBindNPI() {
			int	bindNPI = 0;
			if (configuration.getBindNPI() != null && !"".equals(configuration.getBindNPI())) {
				bindNPI = Integer.parseInt(configuration.getBindNPI());
			}

			return bindNPI;
		}

		private int getBindTON() {
			int bindTON = 0;
			if (configuration.getBindTON() != null && !"".equals(configuration.getBindTON())) {
				bindTON = Integer.parseInt(configuration.getBindTON());
			}

			return bindTON;
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
	 * Sends a enquire_link request to the server to check the connection periodically. The interval
	 * between requests is determined by the {@link LogicaConfiguration#getEnquireLinkTimer()} method.
	 *
	 * @author German Escobar
	 */
	class EnquireLinkThread extends Thread {

		public void run() {
			while (started) {
				try {
					// sleep the amount of time determined in the configuration
					Thread.sleep(configuration.getEnquireLinkTimer());

					if (bound) {
						// send the request
						boolean success = enquireLink();

						// if not success, try to restart the connection
						if (!success) {
							bound = false;
							status = MonitorStatusBuilder.failed("enquire link failed");

							try { connection.closeLink(); } catch (Exception e) {}

							log.info("creating new ConnectionThread after a enquire link failed");
							new Thread(
								new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())
							).start();
						}
					}
				} catch (InterruptedException e) {}
			}
		}

		/**
		 * Helper method that actually sends the request.
		 *
		 * @return true if the connection is alive, false otherwise.
		 */
		private boolean enquireLink() {
			try {
				EnquireLink request = new EnquireLink();
				connection.sendRequest(request);

				log.trace(getLogHead() + "Enquire Link: " + request.toString());

				return true;
			} catch (Exception e) {
				log.info(getLogHead() + "Exception while Enquire Link: " + e.getMessage(), e);
			}

			return false;
		}
	}

	/**
	 * This thread is started in the {@link #doStart()} method and it process the submit_sm
	 * responses that are queued in the submitSmResponses list.
	 *
	 * @author German Escobar
	 */
	class SubmitSmResponseThread extends Thread {

		public void run() {
			while (started) {
				try {
					if (bound) {
						process();

						synchronized (submitSmResponses) {
							submitSmResponses.wait(500);
						}
					}
				} catch (Exception e) {
					log.warn(getLogHead() + "Exception receiving event: " + e.getMessage(), e);
				}
			}
		}

		/**
		 * Helper method that will retrieve all the queued submit responses and will process them if
		 * the lastProcessTime is null or it hasn't been checked in the last 500 ms.
		 */
		private void process() {
			List<SubmitSmResp> submitSmResponseCopy = new ArrayList<SubmitSmResp>(submitSmResponses);

			if (submitSmResponseCopy.size() > 10) {
				log.debug("processing " + submitSmResponseCopy.size() + " submit_sm responses");
			}
			for (SubmitSmResp response : submitSmResponseCopy) {
				// check if we have to precess this response
				if (response.lastProcessedTime == null || (new Date().getTime() - response.lastProcessedTime.getTime()) > 500) {
					process(response);
				}
			}
		}

		/**
		 * Helper method that will process a submit_sm response. It will try to find the message that originated the
		 * response and update the messageId and the commandStatus
		 *
		 * @param response the submit_sm response to be processed
		 */
		private void process(SubmitSmResp response) {
			int sequenceNumber = response.submitSMResp.getSequenceNum();
			String messageId = response.submitSMResp.getMessageId();
			int commandStatus = response.submitSMResp.getCommandStatus();

			log.debug(getLogHead() + "processing submit_sm_response for sequence " + sequenceNumber
					+ " with messageId '" + messageId + "' and command status " + commandStatus);

			// if no messageStore is set, we can't process this response
			if (messageStore == null) {
				submitSmResponses.remove(response);
				log.warn("MessageStore is null: ignoring submit_sm response: " + response.submitSMResp.toString());
				return;
			}

			// try to find a message that matches the criteria
			MessageCriteria criteria = new MessageCriteria()
				.direction(context.getDirection())
				.addProperty("destination", context.getId())
				.addProperty("smsc_sequencenumber", response.submitSMResp.getSequenceNum());

			long startTime = new Date().getTime();
			Collection<Message> messages = messageStore.list(criteria);
			long endTime = new Date().getTime();
			log.trace(getLogHead() + "retrieve message with smsc_sequencenumber " + response.submitSMResp.getSequenceNum()
					+ " took " + (endTime - startTime) + " milis");

			Message message = null;
			if (!messages.isEmpty()) {
				message = messages.iterator().next();
			}

			// if the message is found, update it, otherwise, try later
			if (message != null) {
				submitSmResponses.remove(response);

				message.setProperty("messageId", messageId);
				message.setProperty("commandStatus", commandStatus);
				message.setProperty("reponseTime", new Date());

				if (configuration.getFailedCommandStatuses().contains(commandStatus + "")) {
					message.setStatus(Message.STATUS_FAILED);
				}

				startTime = new Date().getTime();
				messageStore.saveOrUpdate(message);
				endTime = new Date().getTime();
				log.trace(getLogHead() + "update message with id " + message.getId() + " took " + (endTime - startTime)
						+ " milis");
			} else {
				// we couldn't find a matching message, try later or delete if too old
				if (response.lastProcessedTime != null && System.currentTimeMillis() - response.lastProcessedTime.getTime() > 5 * 60 * 1000) {
					log.warn(getLogHead() + "message with smsc_sequencenumber "
							+ response.submitSMResp.getSequenceNum() + " not found after " + response.retries
							+ " retries ... ignoring");
					submitSmResponses.remove(response);
				} else {
					response.lastProcessedTime = new Date();
					response.retries++;
				}
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
					if (bound) {
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
			List<DeliveryReceipt> deliveryReceiptsCopy = new ArrayList<DeliveryReceipt>(deliveryReceipts);

			for (DeliveryReceipt dr : deliveryReceiptsCopy) {
				long idleTime = 900L * dr.retries;

				if (dr.lastProcessedTime == null || (new Date().getTime() - dr.lastProcessedTime.getTime()) > idleTime) {
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
		private void process(DeliveryReceipt dr) {
			if (messageStore == null) {
				log.warn("MessageStore is null: ignoring delivery receipt: " + dr.deliverSm.toString());
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

				// only route delivery receipt if original message contains a receiptDestination
				String receiptDestination = originalMessage.getProperty("receiptDestination", String.class);
				if (receiptDestination != null && !"".equals(receiptDestination)) {
					drMessage.setDestination(receiptDestination); // this skips the routing mechanism
					messageProducer.produce(drMessage);
				}
			} else {
				// we couldn't find a matching message, remove or try later if less than 6 retries
				if (dr.retries > 9) {
					log.warn(getLogHead() + " could not find message with messageId '" + convertMessageId(drMessage.getProperty("messageId", String.class)) + "' after " + dr.retries + " retries ... ignoring.");
					deliveryReceipts.remove(dr);
				} else {
					dr.lastProcessedTime = new Date();
					dr.retries++;

					log.debug(getLogHead() + " could not find message with messageId: " + convertMessageId(drMessage.getProperty("messageId", String.class)) + " ... trying later, retry " + dr.retries);
				}
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
			messageId = convertMessageId(messageId);
			String to = drMessage.getProperty("to", String.class);
			String from = drMessage.getProperty("from", String.class);

			MessageCriteria criteria = new MessageCriteria()
				.direction(context.getDirection())
				.addProperty("destination", context.getId())
				.addProperty("smsc_messageid", messageId);

			long startTime = new Date().getTime();
			Collection<Message> messages = messageStore.list(criteria);
			long endTime = new Date().getTime();

			log.trace(getLogHead() + "retrieve message with smsc_messageid: " + messageId + ", to: " + to + ", from: " + from + " took "
					+ (endTime - startTime) + " milis");

			if (messages.size() == 1) {
				originalMessage = messages.iterator().next();
			} else if (messages.size() > 1) {
				log.debug(messages.size() + " messages matched the id: " + messageId);

				// iterate through the matched messages to find one that matches exactly
				Iterator<Message> iterMessages = messages.iterator();
				while (iterMessages.hasNext() && originalMessage == null) {
					Message message = iterMessages.next();

					String mTo = message.getProperty("to", String.class);
					if (mTo.equals(to) || mTo.equals(from)) {
						originalMessage = message;
					}
				}
			}

			return originalMessage;
		}

		/**
		 * Helper method that checks the configuration to see if we have to convert the delivery receipt id to
		 * hexadecimal or decimal.
		 *
		 * @param messageId the string to be converted.
		 * @return the converted messageId according to the configuration (can happen that the value is not converted at all).
		 */
		private String convertMessageId(String messageId) {
			if (configuration.getDlrIdConversion().equals(DlrIdConversion.HEXA_TO_DEC)) {
				return SmppUtil.toMessageIdAsLong(messageId) + "";
			} else if (configuration.getDlrIdConversion().equals(DlrIdConversion.DEC_TO_HEXA)) {
				try {
					long messageIdAsLong = Long.parseLong(messageId);
					return SmppUtil.toMessageIdAsHexString(messageIdAsLong);
				} catch (NumberFormatException e) {
					log.warn("NumberFormatException trying to convert '" + messageId + " to hexadecimal");
				}
			}

			return messageId;
		}

	}

	private class MessageListener implements ConnectionObserver {

		private BindResp bindResponse;

		@Override
		public void packetReceived(Connection source, SMPPPacket packet) {
			if (packet.isRequest()) {
				if (packet.getCommandId() == SMPPPacket.DELIVER_SM) {
					int esmClass = packet.getEsmClass();

					// check if it is a short message or a delivery receipt and handle appropriately
					if (esmClass == SMPPPacket.SMC_RECEIPT) {
						try {
							handleDeliveryReceipt((DeliverSM) packet);
						} catch (ParseException e) {
							log.warn(getLogHead() + "ParseException processing delivery receipt: " + e.getMessage());
						}
					} else {
						handleDeliverSm((DeliverSM) packet);
					}
				}
			} else {
				if (packet.getCommandId() == SMPPPacket.BIND_RECEIVER_RESP || packet.getCommandId() == SMPPPacket.BIND_TRANSMITTER_RESP
						|| packet.getCommandId() == SMPPPacket.BIND_TRANSCEIVER_RESP) {
					bindResponse = (BindResp) packet;
				} else if (packet.getCommandId() == SMPPPacket.SUBMIT_SM_RESP) {
					handleSubmitSmResponse((SubmitSMResp) packet);
				}
			}
		}

		@Override
		public void update(Connection source, SMPPEvent event) {
			log.info(getLogHead() + "an SMPPEvent was received: " + event.toString());

			if (event.getType() == SMPPEvent.RECEIVER_EXIT && started) {
				ReceiverExitEvent exitEvent = (ReceiverExitEvent) event;

				String msg = getLogHead() + "Received an exit event (ReceiverExitEvent) with reason " + exitEvent.getReason()
						+ ", state is " + exitEvent.getState();
				if (exitEvent.getReason() == ReceiverExitEvent.EXCEPTION) {
					msg += ": " + exitEvent.getException().getMessage();
				}
				log.error(msg, exitEvent.getException());

				// restart the connection if there was an exception
				if (exitEvent.getReason() == ReceiverExitEvent.EXCEPTION) {
					bound = false;
					status = MonitorStatusBuilder.failed("received an exit event");

					try { connection.closeLink(); } catch (Exception e) {}

					log.info(getLogHead() + "creating new ConnectionThread after a ReceiverExitEvent");
					if (!connecting) {
						new Thread(
							new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())
						).start();
					}
				}
			} else if (event.getType() == SMPPEvent.RECEIVER_EXCEPTION) {
				ReceiverExceptionEvent exceptionEvent = (ReceiverExceptionEvent) event;
				log.error(getLogHead() + "Received ReceiverExceptionEvent with state " + exceptionEvent.getState()
						+ ": " + exceptionEvent.getException().getMessage(), exceptionEvent.getException());
			}
		}

		public BindResp getBindResponse(long timeout) {
			long startTime = new Date().getTime();

			while ((new Date().getTime() - startTime) < timeout && bindResponse == null) {
				try { Thread.sleep(500); } catch (Exception e) {}
			}

			return bindResponse;
		}

		/**
		 * Helper method that handles a submit_sm response. It will wrap it into a SubmitSmResponse class
		 * (defined in this same file) and add it to the submitSmResponses list. It will then be retrieved by the
		 * SubmitSmResponseThread (defined in this file) and processed accordingly.
		 *
		 * @param response the SubmitSMResp that was received.
		 */
		private void handleSubmitSmResponse(SubmitSMResp response) {
			log.debug(getLogHead() + "received submit_sm response with sequence '" + response.getSequenceNum() + "' and status " + response.getCommandStatus());

			SubmitSmResp submitSmResponse = new SubmitSmResp();
			submitSmResponse.submitSMResp = response;

			submitSmResponses.add(submitSmResponse);

			// the SubmitSmResponseThread might be waiting on the list, notify that a new response arrived
			synchronized (submitSmResponses) {
				submitSmResponses.notifyAll();
			}
		}

		/**
		 * Helper method that handle deliver_sm PDU's. It will create a Message object to inject it into the
		 * gateway using the MessageProducer.
		 *
		 * @param deliverSm the deliver_sm PDU that was received.
		 */
		private void handleDeliverSm(DeliverSM deliverSm) {
			if (!configuration.isDiscardIncomingMsgs()) {
				log.debug(getLogHead() + "received DeliverSM: " +  deliverSm.toString());

				String from = deliverSm.getSource().getAddress();
				String to = deliverSm.getDestination().getAddress();
				String text = new String(deliverSm.getMessageText());
				log.debug(getLogHead() + "DeliverSM text: " + new String(deliverSm.getMessageText()));
				log.debug(getLogHead() + "DeliverSM text bytes: " + decodeBytes(deliverSm.getMessage()));
				log.debug(getLogHead() + "DeliverSM data coding: " + deliverSm.getDataCoding());
				TLVTable tlvTable = deliverSm.getTLVTable();
				if (tlvTable != null) {
					Collection values = deliverSm.getTLVTable().values();
					for (Object tlv : values) {
						log.debug(getLogHead() + "DeliverSM tlv: " + tlv);
					}
				} else {
					log.debug(getLogHead() + "DeliverSM has no TLVTable!");
				}

				Message message = new Message();
				message.setProperty("to", to);
				message.setProperty("from", from);
				message.setProperty("text", text);

				messageProducer.produce(message);
			} else {
				log.warn(getLogHead() + "received DeliverSM discardIncomingMessages is set ... ignoring message: " + deliverSm.toString());
			}
		}

		private String decodeBytes(byte[] bytes) {
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02X ", b));
			}
			return sb.toString();
		}

		/**
		 * Helper method that handle delivery receipts PDU's. It will create a Message object out of the delivery
		 * receipt, wrap it in the DeliveryReceipt class (defined in this file) and add it to the deliveryReceipts
		 * list. It will be retrieved later by the DeliveryReceiptThread (defined in this file) and processed
		 * accordingly
		 *
		 * @param deliverSm the deliver_sm PDU that was received, it is a delivery receipt.
		 * @throws ParseException if the format of the short message (delivery receipt) is wrong.
		 */
		private void handleDeliveryReceipt(DeliverSM deliverSm) throws ParseException {
			String dest = "null";
			if (deliverSm.getDestination() != null) {
				dest = deliverSm.getDestination().getAddress();
			}

			String source = "null";
			if (deliverSm.getSource() != null) {
				source = deliverSm.getSource().getAddress();
			}

			log.info(getLogHead() + "received Delivery Receipt: source: " + source + ", dest: " + dest + ", text: " + deliverSm.getMessageText());

			String shortMessage = new String(deliverSm.getMessageText());
			String id = getDeliveryReceiptValue("id", shortMessage);

			int submitted = Integer.parseInt(getDeliveryReceiptValue("sub", shortMessage));
			int delivered = Integer.parseInt(getDeliveryReceiptValue("dlvrd", shortMessage));

			SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
			Date submitDate = sdf.parse(getDeliveryReceiptValue("submit date", shortMessage));
			Date doneDate = sdf.parse(getDeliveryReceiptValue("done date", shortMessage));

			String finalStatus = getDeliveryReceiptValue("stat", shortMessage);
			//String error = getDeliveryReceiptValue("err", shortMessage);

			// create the message
			Message message = new Message();

			message.setProperty("isDLR", true);

			String to = deliverSm.getDestination().getAddress();
			String from = deliverSm.getSource().getAddress();

			message.setProperty("to", to);
			message.setProperty("from", from);

			// set the id
			message.setProperty("messageId", id);

			// set the number of submitted and submit date
			message.setProperty("submitted", submitted);
			message.setProperty("submitDate", submitDate);

			// set the number of delivered
			message.setProperty("delivered", delivered);

			// set done date
			message.setProperty("doneDate", doneDate);

			// set final status
			message.setProperty("finalStatus", finalStatus);

			DeliveryReceipt deliveryReceipt = new DeliveryReceipt();
			deliveryReceipt.message = message;
			deliveryReceipt.deliverSm = deliverSm;

			// add to the deliveryReceipts list and notify the DeliveryReceiptThread that might be waiting
			deliveryReceipts.add(deliveryReceipt);
			synchronized (deliveryReceipts) {
				deliveryReceipts.notifyAll();
			}
		}

		/**
		 * Helper method used to get the delivery receipt attributes values.
		 *
		 * @param attrName is the attribute name.
		 * @param source the original source id:IIIIIIIIII sub:SSS dlvrd:DDD submit
		 *        date:YYMMDDhhmm done date:YYMMDDhhmm stat:DDDDDDD err:E
		 *        Text:....................
		 * @return the value of specified attribute.
		 * @throws IndexOutOfBoundsException
		 */
		private String getDeliveryReceiptValue(String attrName, String source) throws IndexOutOfBoundsException {
			String tmpAttr = attrName + ":";
			int startIndex = source.indexOf(tmpAttr);
			if (startIndex < 0) {
				return null;
			}
			startIndex = startIndex + tmpAttr.length();
			int endIndex = source.indexOf(" ", startIndex);
			if (endIndex > 0) {
				return source.substring(startIndex, endIndex);
			}

			return source.substring(startIndex);
		}

	}

	/**
	 * Helper class that wraps the submit_sm response with the last time it was tried to be processed and number
	 * of retries.
	 *
	 * @author German Escobar
	 */
	class SubmitSmResp {
		public SubmitSMResp submitSMResp;
		public Date lastProcessedTime;
		public int retries = 0;
	}

	/**
	 * Helper class that wraps the delivery receipt PDU and the delivery receipt messages, besides the last time
	 * it was tried to be processed and the number of retries.
	 *
	 * @author German Escobar
	 */
	class DeliveryReceipt {
		public Message message;
		public DeliverSM deliverSm;
		public Date lastProcessedTime;
		public int retries = 0;
	}

}
