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
	
	public enum Flow {
		
		INBOUND(1), OUTBOUND(2), UNKNOWN(-1);
		
		private byte id;
		
		private Flow(int id) {
			this.id = (byte) id;
		}
		
		public byte value() {
			return id;
		}
		
		public static Flow getFlow(byte b) {
			for (Flow t : values()) {
				if (t.value() == b) {
					return t;
				}
			}
			
			throw new IllegalArgumentException("Type with id " + b + " not supported");
			
		}
	}
	
	public enum Status {
		
		CREATED(1), PROCESSED(2), FAILED(3), UNROUTABLE(4), RETRYING(5);
		
		private byte id;
		
		private Status(int id) {
			this.id = (byte) id;
		}
		
		public byte value() {
			return id;
		}
		
		public static Status getSatus(byte b) {
			if (b == 1) {
				return CREATED;
			} else if (b == 2) {
				return PROCESSED;
			} else if (b == 3) {
				return FAILED;
			} else if (b == 4) {
				return UNROUTABLE;
			} else if (b == 5) {
				return RETRYING;
			}
			
			throw new IllegalArgumentException("Status with id " + b + " not supported");
		}
	}
	
	private long id = NOT_PERSISTED;
	
	private String accountId = ANONYMOUS_ACCOUNT_ID;
	
	/**
	 * Transient. Should not be persisted. Should be set to null once validated.
	 * Mandatory if accountId is not Message.ANONYMOUS_ACCOUNT_ID and type is
	 * Message.Type.OUTBOUND.
	 */
	private transient String password;
	
	private String reference = UUID.randomUUID().toString();
	
	private String type;
	
	private Flow flow = Flow.UNKNOWN;
	
	private String source;
	
	private SourceType sourceType = SourceType.UNKNOWN;
	
	private String destination;
	
	private DestinationType destinationType = DestinationType.UNKNOWN;
	
	private Status status = Status.CREATED;
	
	private Map<String,Object> properties = new HashMap<String,Object>();
	
	private Object body;
	
	private Date creationTime = new Date();
	
	public Message() {
		
	}
	
	public Message(String type) {
		this.type = type;
	}
	
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

	public final Flow getFlow() {
		return flow;
	}

	public final void setFlow(Flow flow) {
		this.flow = flow;
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
	
}