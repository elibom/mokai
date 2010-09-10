package org.mokai.persist;

/**
 * The {@link MessageStore} rejected the message of the operation, so, it
 * cannot be persisted.
 * 
 * @author German Escobar
 */
public class RejectedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RejectedException() {
		
	}

	public RejectedException(String message) {
		super(message);
	}

	public RejectedException(Throwable cause) {
		super(cause);
	}

	public RejectedException(String message, Throwable cause) {
		super(message, cause);
	}

}
