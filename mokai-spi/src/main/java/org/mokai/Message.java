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
	
	public static final String ANONYMOUS_ACCOUNT_ID = "anonymous";
	
	public static final String SMS_TYPE = "sms";
	public static final String DELIVERY_RECEIPT_TYPE = "delivery-receipt";
	
	/**
	 * The source type of the message. With the {@link Message#source} attribute, 
	 * it answers the question: Who produced the message?
	 * 
	 * @author German Escobar
	 */
	public enum SourceType {
		
		RECEIVER(1), PROCESSOR(2), UNKNOWN(-1);
		
		private byte id;
		
		private SourceType(int id) {
			this.id = (byte) id;
		}
		
		public byte value() {
			return id;
		}
		
		public static SourceType getSourceType(byte b) {
			if (b == 1) {
				return RECEIVER;
			} else if (b == 2) {
				return PROCESSOR;
			} else if (b == -1) {
				return UNKNOWN;
			}
			
			throw new IllegalArgumentException("Source type with id " + b + " not supported");
		}
	}
	
	/**
	 * The destination type of the message. With the {@link Message#destination} attribute, 
	 * it answers the question: Who processed the message?
	 * 
	 * @author German Escobar
	 */
	public enum DestinationType {
		
		PROCESSOR(1), UNKNOWN(-1);
		
		private byte id;
		
		private DestinationType(int id) {
			this.id = (byte) id;
		}
		
		public byte value() {
			return id;
		}
		
		public static DestinationType getDestinationType(byte b) {
			for (DestinationType t : values()) {
				if (t.value() == b) {
					return t;
				}
			}
			
			throw new IllegalArgumentException("Destination type with id " + b + " not supported");
		}
	}
	
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
	 * Describes the status of the message.
	 * 
	 * @author German Escobar
	 */
	public enum Status {
		
		/**
		 * The message was created. This is the default status.
		 */
		CREATED(1),
		
		/**
		 * The message was processed by a processor. 
		 */
		PROCESSED(2), 
		
		/**
		 * The message couldn't be processed by the selected processor.
		 */
		FAILED(3), 
		
		/**
		 * The message couldn't be routed (no processor accepted it)
		 */
		UNROUTABLE(4),
		
		/**
		 * The message failed, but now it is being retried.
		 */
		RETRYING(5), 
		
		/**
		 * The message is being re-routed
		 */
		REROUTED(6);
		
		private byte id;
		
		private Status(int id) {
			this.id = (byte) id;
		}
		
		public byte value() {
			return id;
		}
		
		public static Status getSatus(byte b) {
			
			for (Status status : values()) {
				if (b == status.value()) {
					return status;
				}
			}
			
			throw new IllegalArgumentException("Status with id " + b + " not supported");
		}
	}
	
	/**
	 * Set and used by persistence mechanisms.
	 */
	private long id = NOT_PERSISTED;
	
	/**
	 * The account to which the message belongs.
	 */
	private String accountId = ANONYMOUS_ACCOUNT_ID;
	
	/**
	 * Transient. Should not be persisted. Should be set to null once validated.
	 * Mandatory if accountId is not Message.ANONYMOUS_ACCOUNT_ID and type is
	 * Message.Type.OUTBOUND.
	 */
	private transient String password;
	
	/**
	 * A reference string that applications can use to query the messages.
	 */
	private String reference = UUID.randomUUID().toString();
	
	/**
	 * An String used to identify the message (e.g. sms, email, etc.)
	 */
	private String type;
	
	/**
	 * @see Direction
	 */
	private Direction direction = Direction.UNKNOWN;
	
	/**
	 * The id of the source of the message.
	 */
	private String source;
	
	/**
	 * @see SourceType
	 */
	private SourceType sourceType = SourceType.UNKNOWN;
	
	/**
	 * The id of the destination of the message
	 */
	private String destination;
	
	/**
	 * @see DestinationType
	 */
	private DestinationType destinationType = DestinationType.UNKNOWN;
	
	/**
	 * @see Status
	 */
	private Status status = Status.CREATED;
	
	/**
	 * The properties of the message
	 */
	private Map<String,Object> properties = new HashMap<String,Object>();
	
	/**
	 * The body of the message.
	 */
	private Object body;
	
	/**
	 * The creation time of the message.
	 */
	private Date creationTime = new Date();
	
	/**
	 * The modification time of the message.
	 */
	private Date modificationTime = new Date();
	
	public Message() {
		
	}
	
	public Message(String type) {
		this.type = type;
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
		
		if (this.type != null && this.type.equals(t)) {
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

	public final String getReference() {
		return reference;
	}

	public final void setReference(String reference) {
		this.reference = reference;
	}

	public final String getAccountId() {
		return accountId;
	}

	public final void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public final String getPassword() {
		return password;
	}

	public final void setPassword(String password) {
		this.password = password;
	}

	public final String getSource() {
		return source;
	}

	public final void setSource(String source) {
		this.source = source;
	}

	public final SourceType getSourceType() {
		return sourceType;
	}

	public final void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public final String getDestination() {
		return destination;
	}

	public final void setDestination(String destination) {
		this.destination = destination;
	}

	public final DestinationType getDestinationType() {
		return destinationType;
	}

	public final void setDestinationType(DestinationType destinationType) {
		this.destinationType = destinationType;
	}

	public final String getType() {
		return type;
	}

	public final void setType(String type) {
		this.type = type;
	}

	public final Direction getDirection() {
		return direction;
	}

	public final void setDirection(Direction direction) {
		this.direction = direction;
	}

	public final Status getStatus() {
		return status;
	}

	public final void setStatus(Status status) {
		this.status = status;
	}

	public final Object getBody() {
		return body;
	}
	
	public final Map<String, Object> getProperties() {
		return properties;
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

	@SuppressWarnings("unchecked")
	public final <T> T getBody(Class<T> clazz) {
		return (T) body;
	}

	public final void setBody(Object body) {
		this.body = body;
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