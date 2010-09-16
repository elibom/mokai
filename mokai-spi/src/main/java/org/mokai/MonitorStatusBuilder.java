package org.mokai;

import org.mokai.Monitorable.Status;

/**
 * Utility class to help create the Monitorable.Status objects.
 * 
 * @author German Escobar
 */
public class MonitorStatusBuilder {
	
	/**
	 * This class is not supposed to be instantiated.
	 */
	private MonitorStatusBuilder() {}

	public static Status unknown() {
		return Status.UNKNOWN;
	}
	
	public static Status unknown(String message) {
		Status status = Status.UNKNOWN;
		status.setMessage(message);
		
		return status;
	}
	
	public static Status ok() {
		return Status.OK;
	}
	
	public static Status ok(String message) {
		Status status = Status.OK;
		status.setMessage(message);
		
		return status;
	}
	
	public static Status failed(String message) {
		Status status = Status.FAILED;
		status.setMessage(message);
		
		return status;
	}
	
	public static Status failed(String message, Exception exception) {
		Status status = Status.FAILED;
		status.setMessage(message);
		status.setException(exception);
		
		return status;
	}
}
