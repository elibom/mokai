package org.mokai.web.admin.jogger.controllers;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.mokai.Message;

public class MessageUI {

	private Object id;

	private String date;

	private String source;

	private String destination;

	private String status;

	private String to;

	private String from;

	private String sequence;

	private String messageId;

	private String cmdStatus;

	private String receipt;

	private String receiptDate;

	private String text;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public MessageUI(Message message) {
		this.id = message.getId().toString();
		this.date = sdf.format( message.getCreationTime() );
		this.source = message.getSource();
		this.destination = message.getDestination();
		this.status = formatStatus(message.getStatus());
		this.to = message.getProperty("to", String.class);
		this.from = message.getProperty("from", String.class);
		this.sequence = message.getProperty("sequence", String.class);
		this.messageId = message.getProperty("messageId", String.class);
		Integer intCmdStatus = message.getProperty("commandStatus", Integer.class);
		if (intCmdStatus != null) {
			this.cmdStatus = intCmdStatus + "";
		}
		this.receipt = message.getProperty("receiptStatus", String.class);
		Timestamp dReceiptDate = message.getProperty("receiptTime", Timestamp.class);
		if (dReceiptDate != null) {
			this.receiptDate = sdf.format( dReceiptDate );
		}
		this.text = message.getProperty("text", String.class);
	}

	private String formatStatus(byte status) {

		if (status == Message.STATUS_CREATED) {
			return "CREATED";
		} else if (status == Message.STATUS_FAILED) {
			return "FAILED";
		} else if (status == Message.STATUS_PROCESSED) {
			return "PROCESSED";
		} else if (status == Message.STATUS_RETRYING) {
			return "RETRYING";
		} else if (status == Message.STATUS_UNROUTABLE) {
			return "UNROUTABLE";
		}

		return "UNKNWON";
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination == null ? "" : destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTo() {
		return to == null ? "" : to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from == null ? "" : from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSequence() {
		return sequence == null ? "" : sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getMessageId() {
		return messageId == null ? "" : messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getCmdStatus() {
		return cmdStatus == null ? "" : cmdStatus ;
	}

	public void setCmdStatus(String cmdStatus) {
		this.cmdStatus = cmdStatus;
	}

	public String getReceipt() {
		return receipt == null ? "" : receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	public String getReceiptDate() {
		return receiptDate == null ? "" : receiptDate;
	}

	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}

	public String getText() {
		return text == null ? "" : text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
