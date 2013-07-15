package org.mokai.persist;

/**
 *
 *
 * @author German Escobar
 */
public class StoreException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StoreException() {
	}

	public StoreException(String message) {
		super(message);
	}

	public StoreException(Throwable cause) {
		super(cause);
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
