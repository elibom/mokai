package org.mokai;

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
