package org.mokai.spi;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author German Escobar
 */
public class Message implements Serializable {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -2265738328900734732L;
	
	public static final long NOT_PERSISTED = -1;
	
	public static final String ANONYMOUS_ACCOUNT_ID = "anonymous";
	
	public enum SourceType {
		
		RECEIVER(1), PROCESSOR(2), UNKNOWN(-1);
		
		private byte id;
		
		private SourceType(int id) {
			this.id = (byte) id;
		}
		
		public byte getId() {
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
		
		public byte getId() {
			return id;
		}
		
		public static DestinationType getDestinationType(byte b) {
			if (b == 1) {
				return PROCESSOR;
			} else if (b == -1) {
				return UNKNOWN;
			}
			
			throw new IllegalArgumentException("Destination type with id " + b + " not supported");
		}
	}
	
	public enum Type {
		
		INBOUND(1), OUTBOUND(2), UNKNOWN(-1);
		
		private byte id;
		
		private Type(int id) {
			this.id = (byte) id;
		}
		
		public byte getId() {
			return id;
		}
		
		public static Type getType(byte b) {
			if (b == 1) {
				return INBOUND;
			} else if (b == 2) {
				return OUTBOUND;
			} else if (b == -1) {
				return UNKNOWN;
			}
			
			throw new IllegalArgumentException("Type with id " + b + " not supported");
			
		}
	}
	
	public enum Status {
		
		CREATED(1), PROCESSED(2), FAILED(3), UNROUTABLE(4);
		
		private byte id;
		
		private Status(int id) {
			this.id = (byte) id;
		}
		
		public byte getId() {
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
			}
			
			throw new IllegalArgumentException("Status with id " + b + " not supported");
		}
	}
	
	protected long id = NOT_PERSISTED;
	
	protected String accountId = ANONYMOUS_ACCOUNT_ID;
	
	/**
	 * Transient. Should not be persisted. Should be set to null once validated.
	 * Mandatory if accountId is not Message.ANONYMOUS_ACCOUNT_ID and type is
	 * Message.Type.OUTBOUND.
	 */
	protected transient String password;
	
	protected String reference = UUID.randomUUID().toString();
	
	protected Type type = Type.UNKNOWN;
	
	protected String source;
	
	protected SourceType sourceType = SourceType.UNKNOWN;
	
	protected String destination;
	
	protected DestinationType destinationType = DestinationType.UNKNOWN;
	
	protected Status status = Status.CREATED;
	
	protected Date creationTime = new Date();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public DestinationType getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(DestinationType destinationType) {
		this.destinationType = destinationType;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	
}