package org.mokai.connector.smpp;

import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.mokai.Message;

/**
 * Helper class to translate SMPP messages to {@link Message} and
 * viceversa.
 * 
 * @author German Escobar
 */
public class SmsMessageTranslator {
	
	/**
	 * Hides the public constructor. 
	 */
	private SmsMessageTranslator() {}

	public static Message createDeliveryReceipt(DeliverSm deliverSm) throws InvalidDeliveryReceiptException {
		
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
	
	public static Message createDeliverSm(DeliverSm deliverSm) {
		
		String to = deliverSm.getDestAddress();
		String from = deliverSm.getSourceAddr();
		String text = new String(deliverSm.getShortMessage());
		
		Message message = new Message(Message.SMS_TYPE);
		message.setProperty("to", to);
		message.setProperty("from", from);
		message.setProperty("text", text);
		
		return message;
	}
	
}
