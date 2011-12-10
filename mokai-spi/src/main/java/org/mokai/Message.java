package org.mokai;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message that is being routing through the gateway.
 * 
 * @author German Escobar
 */
public class Message implements Serializable {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -2265738328900734732L;
	
	public static final long NOT_PERSISTED = -1;
	
	private static final String TYPE = "MK_TYPE";
	private static final String DIRECTION = "MK_DIRECTION";
	private static final String SOURCE = "MK_SOURCE";
	private static final String DESTINATION = "MK_DESTINATION";
	private static final String REFERENCE = "MK_REFERENCE";
	
	public static final String SMS_TYPE = "sms";
	public static final String DELIVERY_RECEIPT_TYPE = "delivery-receipt";
	
	/**
	 * The message was created. This is the default status.
	 */
	public static final byte STATUS_CREATED = 1;
	
	/**
	 * The message was processed by a processor. 
	 */
	public static final byte STATUS_PROCESSED = 2;
	
	/**
	 * The message couldn't be processed by the selected processor.
	 */
	public static final byte STATUS_FAILED = 3;
	
	/**
	 * The message couldn't be routed (no processor accepted it)
	 */
	public static final byte STATUS_UNROUTABLE = 4;
	
	/**
	 * The message failed, but now it is being retried.
	 */
	public static final byte STATUS_RETRYING = 5;
	
	/**
	 * Tells whether the message is outbound (from applications to connections) or 
	 * inbound (from connections to applications)
	 * 
	 * @author German Escobar
	 */
	public enum Direction {
		
		TO_APPLICATIONS(1), TO_CONNECTIONS(2), UNKNOWN(-1);
		
		private byte id;
		
		private Direction(int id) {
			this.id = (byte) id;
		}
		
		public byte value() {
			return id;
		}
		
		public static Direction getFlow(byte b) {
			for (Direction t : values()) {
				if (t.value() == b) {
					return t;
				}
			}
			
			throw new IllegalArgumentException("Type with id " + b + " not supported");
			
		}
	}
	
	/**
	 * Set and used by persistence mechanisms.
	 */
	private long id = NOT_PERSISTED;
	
	/**
	 * The status of the message. It can be any byte.
	 */
	private byte status = STATUS_CREATED;
	
	/**
	 * The properties of the message
	 */
	private Map<String,Object> properties = new HashMap<String,Object>();
	
	/**
	 * The creation time of the message.
	 */
	private Date creationTime = new Date();
	
	/**
	 * The modification time of the message.
	 */
	private Date modificationTime = new Date();
	
	public Message() {
		this(SMS_TYPE);
	}
	
	public Message(String type) {
		properties.put(TYPE, type);
		properties.put(DIRECTION, Direction.UNKNOWN);
		properties.put(REFERENCE, UUID.randomUUID().toString());
	}
	
	/**
	 * Helper method to determine if a message is from a specified type.
	 * 
	 * @param t the type to be checked.
	 * @return true if the message is of the expected type, false
	 * otherwise.
	 */
	public final boolean isType(String t) {
		if (t == null) {
			throw new IllegalArgumentException("Type not provided");
		}
		
		if (getType() != null && getType().equals(t)) {
			return true;
		}
		
		return false;
	}

	public final long getId() {
		return id;
	}

	public final void setId(long id) {
		this.id = id;
	}

	public final String getSource() {
		return getProperty(SOURCE, String.class);
	}

	public final void setSource(String source) {
		setProperty(SOURCE, source);
	}

	public final String getDestination() {
		return getProperty(DESTINATION, String.class);
	}

	public final void setDestination(String destination) {
		setProperty(DESTINATION, destination);
	}

	public final String getType() {
		return getProperty(TYPE, String.class);
	}

	public final void setType(String type) {
		setProperty(TYPE, type);
	}

	public final Direction getDirection() {
		return getProperty(DIRECTION, Direction.class);
	}

	public final void setDirection(Direction direction) {
		setProperty(DIRECTION, direction);
	}
	
	public final String getReference() {
		return getProperty(REFERENCE, String.class);
	}
	
	public final void setReference(String reference) {
		setProperty(REFERENCE, reference);
	}

	public final byte getStatus() {
		return status;
	}

	public final void setStatus(byte status) {
		this.status = status;
	}

	public final Map<String, Object> getProperties() {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			if (!entry.getKey().startsWith("MK")) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		
		return ret;
	}
	
	public final Object getProperty(String key) {
		return properties.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public final <T> T getProperty(String key, Class<T> clazz) {
		return (T) properties.get(key);
	}
	
	public final void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	public final void removeProperty(String key) {
		properties.remove(key);
	}

	public final Date getCreationTime() {
		return creationTime;
	}

	public final void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public final Date getModificationTime() {
		return modificationTime;
	}

	public final void setModificationTime(Date modificationTime) {
		this.modificationTime = modificationTime;
	}
	
}