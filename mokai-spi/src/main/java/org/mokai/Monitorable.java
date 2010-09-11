package org.mokai;

/**
 * Implemented by extensions that can be monitored for broken links.
 * 
 * @author German Escobar
 */
public interface Monitorable {

	/**
	 * Possible values of the status of an extension.
	 * 
	 * @author German Escobar
	 */
	public enum Status {
		
		/**
		 * There is no information about the status of the extension.
		 */
		UNKNOWN,
		
		/**
		 * The extension is working well.
		 */
		OK,
		
		/**
		 * The extension has failed.
		 */
		FAILED;
		
		/**
		 * The message associated with the status.
		 */
		private String message;
		
		/**
		 * The last exception thrown by the extension when it fails.
		 */
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
	
	/**
	 * @return the status of the extension
	 */
	Status getStatus();
}
