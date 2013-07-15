package org.mokai;

/**
 * Unchecked exception thrown when there is a problem in the framework execution.
 * It's really a utility Exception that is thrown when no other fits.
 *
 * @author German Escobar
 */
public class ExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExecutionException() {
	}

	public ExecutionException(String message) {
		super(message);
	}

	public ExecutionException(Throwable cause) {
		super(cause);
	}

	public ExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
