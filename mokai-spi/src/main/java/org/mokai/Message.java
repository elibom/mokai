package org.mokai;

import java.io.Serializable;
import java.util.Collections;
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
	
	public static final String DIRECTION_PROPERTY = "MK_DIRECTION";
	public static final String SOURCE_PROPERTY = "MK_SOURCE";
	public static final String DESTINATION_PROPERTY = "MK_DESTINATION";
	public static final String REFERENCE_PROPERTY = "MK_REFERENCE";
	
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
		properties.put(DIRECTION_PROPERTY, Direction.UNKNOWN);
		properties.put(REFERENCE_PROPERTY, UUID.randomUUID().toString());
	}

	public final long getId() {
		return id;
	}

	public final void setId(long id) {
		this.id = id;
	}

	public final String getSource() {
		return getProperty(SOURCE_PROPERTY, String.class);
	}

	public final void setSource(String source) {
		setProperty(SOURCE_PROPERTY, source);
	}

	public final String getDestination() {
		return getProperty(DESTINATION_PROPERTY, String.class);
	}

	public final void setDestination(String destination) {
		setProperty(DESTINATION_PROPERTY, destination);
	}
	
	public final Message withDestination(String destination) {
		setDestination(destination);
		
		return this;
	}

	public final Direction getDirection() {
		return getProperty(DIRECTION_PROPERTY, Direction.class);
	}

	public final void setDirection(Direction direction) {
		setProperty(DIRECTION_PROPERTY, direction);
	}
	
	public final String getReference() {
		return getProperty(REFERENCE_PROPERTY, String.class);
	}
	
	public final void setReference(String reference) {
		setProperty(REFERENCE_PROPERTY, reference);
	}

	public final byte getStatus() {
		return status;
	}

	public final void setStatus(byte status) {
		this.status = status;
	}

	/**
	 * Returns the user properties (i.e. it excludes the Mokai properties, those that start with MK_).
	 * 
	 * @return a map of string-object tuples.
	 */
	public final Map<String, Object> getProperties() {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			if (!entry.getKey().startsWith("MK_")) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		
		return Collections.unmodifiableMap(ret);
	}
	
	/**
	 * Returns the value of a property as an object.
	 * 
	 * @param key the key of the property wose value we are searching.
	 * @return the value of the property if it exists or null otherwise.
	 */
	public final Object getProperty(String key) {
		return properties.get(key);
	}
	
	/**
	 * Returns the value of a property when the type is known.
	 * 
	 * @param <T> the type of the property.
	 * 
	 * @param key the key of the property whose value we are searching.
	 * @param clazz the class of the property
	 * @return the value of the property if it exists or null otherwise.
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getProperty(String key, Class<T> clazz) {
		return (T) properties.get(key);
	}
	
	/**
	 * Sets a property in the map. If the property exists, it will be modified.
	 * 
	 * @param key the key of the property to be added or modified.
	 * @param value the value of the property to be added or modified.
	 * @return the Message instance for chaining.
	 */
	public final Message setProperty(String key, Object value) {
		properties.put(key, value);
		
		return this;
	}
	
	/**
	 * Removes a property from the map if it exists. Notice that is possible to remove Mokai properties, although not recommended.
	 * 
	 * @param key the key of the property to be removed.
	 */
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