package org.mokai.spi.message;

import org.mokai.spi.Message;

public class SmsMessage extends Message {
	
	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 9086855818103798691L;

	private String to;
	
	private String from;
	
	private String text;
	
	private String messageId;
	
	private int commandStatus;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public int getCommandStatus() {
		return commandStatus;
	}

	public void setCommandStatus(int commandStatus) {
		this.commandStatus = commandStatus;
	}
	
}
