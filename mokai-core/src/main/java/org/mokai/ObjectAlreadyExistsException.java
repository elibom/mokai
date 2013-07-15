package org.mokai;

/**
 * Thrown when the object that is being created already exists.
 * 
 * @author German Escobar
 */
public class ObjectAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ObjectAlreadyExistsException() {
		super();
	}

	public ObjectAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectAlreadyExistsException(String message) {
		super(message);
	}

	public ObjectAlreadyExistsException(Throwable cause) {
		super(cause);
	}

}
