package org.mokai;

/**
 * Implemented by types (receivers, processors, actions and acceptors) that can be 
 * monitored for broken links.
 * 
 * @author German Escobar
 */
public interface Monitorable {

	public enum Status {
		UNKNOWN,
		OK,
		FAILED;
		
		private String message;
		
		private Exception exception;
		
		public void setMessage(String message) {
			this.message = message;
		}
		
		public String getMessage() {
			return message;
		}
		
		public void setException(Exception exception) {
			this.exception = exception;
		}
		
		public Exception getException() {
			return exception;
		}
	}
	
	Status getStatus();
}
