package org.mokai.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mokai.Message.Status;

public class MessageCriteria implements Serializable {
	
	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = 9038107535715271586L;

	public enum OrderType {
		UPWARDS, DOWNWARDS;
	}

	/**
	 * The lower limit of records to be retrieved.
	 */
	protected int firstRecord;
	
	/**
	 * The number of records to be retrieved.
	 */
	protected int numRecords;
	
	/**
	 * The column by which the query should be ordered.
	 */
	protected String orderBy;
	
	/**
	 * If it should be ordered upwards or downwards.
	 */
	protected OrderType orderType = OrderType.UPWARDS;
	
	protected List<Status> status;
	
	public MessageCriteria() {
		this.status = new ArrayList<Status>();
	}

	public int getFirstRecord() {
		return firstRecord;
	}

	public void setFirstRecord(int firstRecord) {
		this.firstRecord = firstRecord;
	}
	
	public MessageCriteria firstRecord(int firstRecord) {
		setFirstRecord(firstRecord);
		
		return this;
	}

	public int getNumRecords() {
		return numRecords;
	}

	public void setNumRecords(int numRecords) {
		this.numRecords = numRecords;
	}
	
	public MessageCriteria numRecords(int numRecords) {
		setNumRecords(numRecords);
		
		return this;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	public MessageCriteria orderBy(String orderBy) {
		setOrderBy(orderBy);
		
		return this;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	
	public MessageCriteria orderType(OrderType orderType) {
		setOrderType(orderType);
		
		return this;
	}

	public List<Status> getStatus() {
		return status;
	}

	public void setStatus(List<Status> status) {
		this.status = status;
	}
	
	public MessageCriteria addStatus(Status status) {
		this.status.add(status);
		
		return this;
	}
	
}
