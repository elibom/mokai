package org.mokai.connector.smpp;

import java.io.IOException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connector that sends and receives messages using the SMPP protocol.
 * 
 * @author German Escobar
 */
@Name("SMPP")
@Description("Sends and receives messages using SMPP protocol")
public class SmppConnector implements Processor, Serviceable, Monitorable, 
		ExposableConfiguration<SmppConfiguration> {
	
	private Logger log = LoggerFactory.getLogger(SmppConnector.class);
	
	@Resource
	private MessageProducer messageProducer;
	
	private SMPPSession session = new SMPPSession();
	
	private SmppConfiguration configuration;
	
	private boolean started = false;
	
	private Status status = MonitorStatusBuilder.unknown();
	
	public SmppConnector() {
		this(new SmppConfiguration());
	}
	
	public SmppConnector(SmppConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public final void doStart() throws Exception {
		
		log.debug("starting SmppConnector ... ");
		
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
	
	private SMPPSession createSession() throws IOException {
		session = new SMPPSession();
		
		session.setEnquireLinkTimer(configuration.getEnquireLinkTimer());
		
		session.setMessageReceiverListener(new MessageReceiverListener() {

			@Override
			public void onAcceptAlertNotification(AlertNotification notification) {
				log.debug("received deliverSm: " + notification);
			}

			@Override
			public void onAcceptDeliverSm(DeliverSm pdu) throws ProcessRequestException {
				log.info("received deliverSm: " + pdu);
				
				if (pdu.isSmscDeliveryReceipt()) {
					log.warn("DeliveryReceipt not supported yet: " + pdu);
				} else {
					
					log.info("received deliverySm: " + pdu);
					
					String to = pdu.getDestAddress();
					String from = pdu.getSourceAddr();
					String text = new String(pdu.getShortMessage());
					
					Message message = new Message(Message.SMS_TYPE);
					message.setProperty("to", to);
					message.setProperty("from", from);
					message.setProperty("text", text);
					
					if (messageProducer != null) {
						messageProducer.produce(message);
					} else {
						log.warn("MessageProducer is null ... ignoring message");
					}
				}
					
			}

			@Override
			public DataSmResult onAcceptDataSm(DataSm pdu, Session session) 
					throws ProcessRequestException {
				log.debug("received dataSm: " + pdu);
				return null;
			}
			
		});
		
		session.addSessionStateListener(new SessionStateListener() { 
            public void onStateChange(SessionState newState, SessionState oldState, Object source) {
                if (newState.equals(SessionState.CLOSED)) {
                    log.warn("loosing connection to: " + getConfiguration().getHost() + " - trying to reconnect...");
                    
                    status = MonitorStatusBuilder.failed("connection lost");
                    
                    session.close();
                    reconnect(configuration.getInitialReconnectDelay());
                }
            }
        });
		
		TypeOfNumber ton = TypeOfNumber.UNKNOWN;
		if (configuration.getSourceTON() != null && !"".equals(configuration.getSourceTON())) {
			ton = TypeOfNumber.valueOf(Byte.decode(configuration.getSourceTON()));
		}
		
		NumberingPlanIndicator npi = NumberingPlanIndicator.UNKNOWN;
		if (configuration.getSourceNPI() != null && !"".equals(configuration.getSourceNPI())) {
			npi = NumberingPlanIndicator.valueOf(Byte.valueOf(configuration.getSourceNPI()));
		}
		
		// bind type
		BindType bindType = BindType.BIND_TRX;
		if (configuration.getBindType().equals("r")) {
			log.info("bind type: receiver");
			bindType = BindType.BIND_RX;
		} else if (configuration.getBindType().equals("t")) {
			log.info("bind type: transmitter");
			bindType = BindType.BIND_TX;
		}
		
		
		session.connectAndBind(
                configuration.getHost(),
                configuration.getPort(),
                new BindParameter(
                        bindType,
                        configuration.getSystemId(),
                        configuration.getPassword(), 
                        configuration.getSystemType(),
                        ton,
                        npi,
                        ""));
		
		return session;
	}

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

	@Override
	public final Status getStatus() {
		return status;
	}

	@Override
	public final void process(Message message) {
		if (!status.equals(Status.OK)) {
			throw new IllegalStateException("SMPP client not connected.");
		}

		try {
		
			SubmitSm submitSm = new SubmitSm();
			if (message.getProperty("text", String.class) != null) {
				submitSm.setShortMessage(message.getProperty("text", String.class).getBytes());
			} else {
				submitSm.setShortMessage("".getBytes());
			}
			
			submitSm.setRegisteredDelivery((byte) 1);
			submitSm.setOptionalParametes(
					new OptionalParameter.Null(OptionalParameter.Tag.ALERT_ON_MESSAGE_DELIVERY)
			);
			
			// destination address
			submitSm.setDestAddress(message.getProperty("to", String.class));
			if (notEmpty(configuration.getDestNPI())) {
				submitSm.setDestAddrNpi(Byte.valueOf(configuration.getDestNPI()));
			}
			if (notEmpty(configuration.getDestTON())) {
				submitSm.setDestAddrTon(Byte.valueOf(configuration.getDestTON()));
			}
			
			// source address
			submitSm.setSourceAddr(message.getProperty("from", String.class));
			if (notEmpty(configuration.getSourceNPI())) {
				submitSm.setSourceAddrNpi(Byte.valueOf(configuration.getSourceNPI()));
			}
			if (notEmpty(configuration.getSourceTON())) {
				submitSm.setSourceAddrTon(Byte.valueOf(configuration.getSourceTON()));
			}
			
			submitSm.setRegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE.value());
			
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
			
			message.setProperty("messageId", messageId);
			message.setProperty("commandStatus", 0);
			
		} catch (NegativeResponseException e) {
			message.setProperty("commandStatus", e.getCommandStatus());
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

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

	@Override
	public final SmppConfiguration getConfiguration() {
		return configuration;
	}
	
	private boolean notEmpty(String s) {
		if (s != null && !"".equals(s)) {
			return true;
		}
		
		return false;
	}
	
	private void reconnect(final long initialReconnectDelay) {
        new Thread(new ConnectionThread(Integer.MAX_VALUE, configuration.getInitialReconnectDelay())).start();

	}
	
	/**
	 * Connects to the SMSC. 
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
            } catch (InterruptedException e) {
            }

            int attempt = 0;
            while (attempt < maxRetries && started && (session == null || session.getSessionState().equals(SessionState.CLOSED))) {
                try {
                    log.info("trying to connect to " + getConfiguration().getHost() + " - attempt #" + (++attempt) + "...");
                    session = createSession();
                    
                    status = MonitorStatusBuilder.ok();
                    
                    log.info("connected to '" + configuration.getHost() + ":" + configuration.getPort() + "'");
                    
                } catch (IOException e) {
                    logException(e, attempt == 1);
                    
                    status = MonitorStatusBuilder.failed("could not connect", e);
                    
                    session.close();
                    
                    try {
                        Thread.sleep(configuration.getReconnectDelay());
                    } catch (InterruptedException ee) {
                    }
                }
            }
		}
		
		private void logException(Exception e, boolean firstTime) {
			// print the exception only one time
			String logError = "failed to connect to '" + configuration.getHost() + ":" + configuration.getPort() + "'";
			
			if (firstTime) {
				log.error(logError, e);
			} else {
				log.error(logError + ": " + e.getMessage());
			}
		}
		
	}
}
