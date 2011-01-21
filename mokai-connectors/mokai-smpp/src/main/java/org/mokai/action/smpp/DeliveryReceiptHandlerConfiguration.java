package org.mokai.action.smpp;

/**
 * Holds the configuration of the {@link DeliveryReceiptHanlderAction}. 
 * 
 * @author German Escobar
 */
public class DeliveryReceiptHandlerConfiguration {
	
	private static final long DEFAULT_TIMEOUT = 4000;

	/**
	 * The max time in milliseconds to wait for the message record
	 * to be created
	 */
	private long messageRecordTimeout = DEFAULT_TIMEOUT;

	public long getMessageRecordTimeout() {
		return messageRecordTimeout;
	}

	public void setMessageRecordTimeout(long messageRecordTimeout) {
		this.messageRecordTimeout = messageRecordTimeout;
	}
	
}
