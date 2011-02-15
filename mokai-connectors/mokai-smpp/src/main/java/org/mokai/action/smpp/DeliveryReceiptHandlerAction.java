package org.mokai.action.smpp;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.mokai.Action;
import org.mokai.Configurable;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.annotation.Resource;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.RejectedException;
import org.mokai.persist.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the database with the information of the delivery receipts. This action is configured
 * as a post receiving action. There are a number of challenges associated with this action:
 * 
 * <ul>
 * 	<li>The delivery receipt can arrive before the message is inserted in the database.</li>
 * 	<li>We need to establish a max timeout to wait for the message to be inserted.</li>
 * 	<li>We don't want to block other DELIVER_SM messages, processing of delivery receipts
 * should be asynchronous.</li> 
 * </ul>
 * 
 * @author German Escobar
 */
public class DeliveryReceiptHandlerAction implements Action, Configurable, 
		ExposableConfiguration<DeliveryReceiptHandlerConfiguration> {
	
	private Logger log = LoggerFactory.getLogger(DeliveryReceiptHandlerAction.class);
	
	/**
	 * The configuration of this action
	 */
	private DeliveryReceiptHandlerConfiguration configuration = new DeliveryReceiptHandlerConfiguration();
	
	/**
	 * This will execute the {@link DeliveryReceiptHandler} class asynchronously
	 */
	private ScheduledExecutorService executor;

	/**
	 * Used to retrieve processed messages when a delivery receipt arrives
	 */
	@Resource
	private MessageStore messageStore;
	
	@Override
	public void configure() throws Exception {
		this.executor = Executors.newScheduledThreadPool(10);
	}
	
	@Override
	public void destroy() throws Exception {
		// we are accepting that some delivery receipts may be lost if this action is destroyed
		this.executor.shutdown();
	}
		
	@Override
	public void execute(final Message message) throws Exception {
		
		if (!message.getType().equals(Message.DELIVERY_RECEIPT_TYPE)) {
			return;
		}
		
		if (messageStore == null) {
			log.warn("no MessageStore configured ... ignoring delivery receipt");
			return;
		}
		
		// handle the delivery receipt
		handleDeliveryReceipt(message, new Date().getTime());
		
	}
	
	public void handleDeliveryReceipt(Message message, long startTime) {
		
		// try to find the original submitted message
		String messageId = message.getProperty("messageId", String.class);
		String to = message.getProperty("to", String.class);
		Message originalMessage = findOriginalMessage(messageStore, messageId, to);
				
		if (originalMessage != null) {
					
			// update original message
			String receiptStatus = message.getProperty("finalStatus", String.class);
			Date doneDate = message.getProperty("doneDate", Date.class);
			updateOriginalMessage(messageStore, originalMessage, receiptStatus, doneDate);
				
			// update the reference of the delivery receipt
			message.setProperty("originalReference", originalMessage.getReference());
					
		} else { // the message was not found in the database
				
			long actualTime = new Date().getTime();
				
			// reschedule if it hasn't timeout
			if ((actualTime - startTime) < configuration.getMessageRecordTimeout()) {
					
				log.debug("rescheduling delivery receipt handling to run in 900 millis for message with id: " + messageId);
					
				try { Thread.sleep(900); } catch (Exception e) {}
					
				handleDeliveryReceipt(message, startTime);
			} else {
				log.warn("no submitted message was found with messageId: " + messageId);
			}
		}	
	}
		
	/**
	 * Helper method to find the submitted message of a delivery receipt.
	 * 
	 * @param messageStore the {@link MessageStore} instance used to look for the submitted
	 * message.
	 * @param messageId the id that was returned by the SMSC when the message was submitted
	 * @param to the smsc destination of the message
	 */
	private Message findOriginalMessage(MessageStore messageStore, String messageId, String to) {
			
		log.debug("looking for message with SMSC message id: " + messageId + " and to: " + to);
			
		// create the criteria
		MessageCriteria criteria = new MessageCriteria();
		criteria.addProperty("smsc_messageid", messageId);
		criteria.addProperty("smsc_to", to);

		Collection<Message> messages = messageStore.list(criteria);
		if (messages != null && !messages.isEmpty()) {
					
			Message originalMessage = messages.iterator().next();
			log.debug("message with SMSC message id: " + messageId + " found");
					
			return originalMessage;
		}

		return null;
	}	
		
	/**
	 * Helper method to update the status and done date of a submitted message. 
	 * 
	 * @param messageStore
	 * @param originalMessage
	 * @param receiptStatus
	 * @param doneDate
	 * @throws StoreException
	 * @throws RejectedException
	 */
	private void updateOriginalMessage(MessageStore messageStore, Message originalMessage, 
			String receiptStatus, Date doneDate) throws StoreException, RejectedException {
			
		originalMessage.setProperty("receiptStatus", receiptStatus);
		originalMessage.setProperty("receiptTime", doneDate);
		messageStore.saveOrUpdate(originalMessage);
		
		log.debug("message with id " + originalMessage.getId() + " updated with receiptStatus: " 
				+ receiptStatus);
	}

	@Override
	public DeliveryReceiptHandlerConfiguration getConfiguration() {
		return configuration;
	}

}
