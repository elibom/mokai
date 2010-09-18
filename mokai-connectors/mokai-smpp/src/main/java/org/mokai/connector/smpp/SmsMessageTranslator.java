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
	 * Hides the public constructor. This is an utility class accessed in a static way.
	 */
	private SmsMessageTranslator() {}

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
	
	/**
	 * Creates a short message based on the DeliverSm argument.
	 * 
	 * @param deliverSm holds the information to create the short message.
	 * @return a {@link Message} with the following properties:
	 * <ul>
	 *   <li>to (String) - the destination of the short message.</li>
	 *   <li>from (String) - the source of the short message.</li>
	 *   <li>text (String) - the text of the short message.</li>
	 * </ul>
	 */
	public static Message createShortMessage(DeliverSm deliverSm) {
		
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
