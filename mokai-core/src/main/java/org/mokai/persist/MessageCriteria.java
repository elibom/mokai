package org.mokai.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mokai.Message.Status;

/**
 * Class used to filter messages from the {@link MessageStore}.
 * @see MessageStore#list(MessageCriteria)
 * 
 * @author German Escobar
 */
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
	private int firstRecord;
	
	/**
	 * The number of records to be retrieved.
	 */
	private int numRecords;
	
	/**
	 * The column by which the query should be ordered.
	 */
	private String orderBy;
	
	/**
	 * If it should be ordered upwards or downwards.
	 */
	private OrderType orderType = OrderType.UPWARDS;
	
	private List<Status> status;
	
	public MessageCriteria() {
		this.status = new ArrayList<Status>();
	}

	public final int getFirstRecord() {
		return firstRecord;
	}

	public final void setFirstRecord(int firstRecord) {
		this.firstRecord = firstRecord;
	}
	
	public final MessageCriteria firstRecord(int firstRecord) {
		setFirstRecord(firstRecord);
		
		return this;
	}

	public final int getNumRecords() {
		return numRecords;
	}

	public final void setNumRecords(int numRecords) {
		this.numRecords = numRecords;
	}
	
	public final MessageCriteria numRecords(int numRecords) {
		setNumRecords(numRecords);
		
		return this;
	}

	public final String getOrderBy() {
		return orderBy;
	}

	public final void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	public final MessageCriteria orderBy(String orderBy) {
		setOrderBy(orderBy);
		
		return this;
	}

	public final OrderType getOrderType() {
		return orderType;
	}

	public final void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	
	public final MessageCriteria orderType(OrderType orderType) {
		setOrderType(orderType);
		
		return this;
	}

	public final List<Status> getStatus() {
		return status;
	}

	public final void setStatus(List<Status> status) {
		this.status = status;
	}
	
	public final MessageCriteria addStatus(Status status) {
		this.status.add(status);
		
		return this;
	}
	
}
