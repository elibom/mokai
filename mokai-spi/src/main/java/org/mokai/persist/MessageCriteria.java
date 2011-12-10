package org.mokai.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mokai.Message.Direction;

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
	
	private String type;
	
	private Direction direction;
	
	private List<Byte> status;
	
	private String destination;
	
	private Map<String,Object> properties = new HashMap<String,Object>();
	
	/**
	 * The column by which the query should be ordered.
	 */
	private String orderBy;
	
	/**
	 * If it should be ordered upwards or downwards.
	 */
	private OrderType orderType = OrderType.UPWARDS;
	
	/**
	 * The lower limit of records to be retrieved.
	 */
	protected int lowerLimit;
	
	/**
	 * The number of records to be retrieved.
	 */
	protected int numRecords;
	
	public MessageCriteria() {
		this.status = new ArrayList<Byte>();
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

	public final String getType() {
		return type;
	}

	public final void setType(String type) {
		this.type = type;
	}
	
	public final MessageCriteria type(String type) {
		setType(type);
		
		return this;
	}

	public final Direction getDirection() {
		return direction;
	}

	public final void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public final MessageCriteria direction(Direction direction) {
		setDirection(direction);
		
		return this;
	}

	public final List<Byte> getStatus() {
		return status;
	}

	public final void setStatus(List<Byte> status) {
		this.status = status;
	}
	
	public final MessageCriteria addStatus(byte status) {
		this.status.add(status);
		
		return this;
	}

	public final String getDestination() {
		return destination;
	}

	public final void setDestination(String destination) {
		this.destination = destination;
	}
	
	public final MessageCriteria destination(String destination) {
		setDestination(destination);
		
		return this;
	}

	public int getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(int lowerLimit) {
		this.lowerLimit = lowerLimit;
	}
	
	public MessageCriteria lowerLimit(int lowerLimit) {
		setLowerLimit(lowerLimit);	
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

	public final Map<String, Object> getProperties() {
		return properties;
	}

	public final void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public final MessageCriteria addProperty(String key, Object value) {
		properties.put(key, value);
		
		return this;
	}
	
}
