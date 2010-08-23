package org.mokai.connector.smpp;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.mokai.ExecutionException;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Processor;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;
import org.mokai.message.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connector that sends and receives messages using the SMPP protocol.
 * 
 * @author German Escobar
 */
@Name("SMPP")
@Description("Sends and receives messages using SMPP protocol")
public class SmppConnector implements Processor, Serviceable, 
		ExposableConfiguration<SmppConfiguration> {
	
	private Logger log = LoggerFactory.getLogger(SmppConnector.class);
	
	@Resource
	private MessageProducer messageProducer;
	
	private SMPPSession session = new SMPPSession();
	
	private SmppConfiguration configuration;
	
	public SmppConnector() {
		this(new SmppConfiguration());
	}
	
	public SmppConnector(SmppConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void doStart() throws Exception {
		
		log.debug("starting SmppConnector ... ");
		
		session = new SMPPSession();
		
		TypeOfNumber ton = TypeOfNumber.UNKNOWN;
		if (configuration.getSourceTON() != null && !"".equals(configuration.getSourceTON())) {
			ton = TypeOfNumber.valueOf(Byte.decode(configuration.getSourceTON()));
		}
		
		NumberingPlanIndicator npi = NumberingPlanIndicator.UNKNOWN;
		if (configuration.getSourceNPI() != null && !"".equals(configuration.getSourceNPI())) {
			npi = NumberingPlanIndicator.valueOf(Byte.valueOf(configuration.getSourceNPI()));
		}
		
		session.setEnquireLinkTimer(configuration.getEnquireLinkTimer());
		session.connectAndBind(
                configuration.getHost(),
                configuration.getPort(),
                new BindParameter(
                        BindType.BIND_TRX,
                        configuration.getSystemId(),
                        configuration.getPassword(), 
                        configuration.getSystemType(),
                        ton,
                        npi,
                        ""));
		
		session.setMessageReceiverListener(new MessageReceiverListener() {

			@Override
			public void onAcceptAlertNotification(AlertNotification notification) {
				log.debug("received deliverSm: " + notification);
			}

			@Override
			public void onAcceptDeliverSm(DeliverSm pdu) throws ProcessRequestException {
				log.debug("received deliverSm: " + pdu);
				
				if (pdu.isSmscDeliveryReceipt()) {
					log.warn("DeliveryReceipt not supported yet!");
				} else {
					String to = pdu.getDestAddress();
					String from = pdu.getSourceAddr();
					String text = new String(pdu.getShortMessage());
					
					SmsMessage message = new SmsMessage();
					message.setTo(to);
					message.setFrom(from);
					message.setText(text);
					
					messageProducer.produce(message);
				}
					
			}

			@Override
			public DataSmResult onAcceptDataSm(DataSm pdu, Session session) 
					throws ProcessRequestException {
				log.debug("received dataSm: " + pdu);
				return null;
			}
			
		});
	}

	@Override
	public void doStop() throws Exception {
		session.unbindAndClose();
	}

	@Override
	public void process(Message message) {
		SmsMessage smsMessage = (SmsMessage) message;

		try {
		
			SubmitSm submitSm = new SubmitSm();
			submitSm.setShortMessage(smsMessage.getText().getBytes());
			
			// destination address
			submitSm.setDestAddress(smsMessage.getTo());
			if (notEmpty(configuration.getDestNPI())) {
				submitSm.setDestAddrNpi(Byte.valueOf(configuration.getDestNPI()));
			}
			if (notEmpty(configuration.getDestTON())) {
				submitSm.setDestAddrTon(Byte.valueOf(configuration.getDestTON()));
			}
			
			// source address
			submitSm.setSourceAddr(smsMessage.getFrom());
			if (notEmpty(configuration.getSourceNPI())) {
				submitSm.setSourceAddrNpi(Byte.valueOf(configuration.getDestNPI()));
			}
			if (notEmpty(configuration.getSourceTON())) {
				submitSm.setSourceAddrTon(Byte.valueOf(configuration.getDestTON()));
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
			
			message.setReference(messageId);
			
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	@Override
	public boolean supports(Message message) {
		if (SmsMessage.class.isInstance(message)) {
			return true;
		}
		
		return false;
	}

	@Override
	public SmppConfiguration getConfiguration() {
		return configuration;
	}
	
	private boolean notEmpty(String s) {
		if (s != null && !"".equals(s)) {
			return true;
		}
		
		return false;
	}

}
