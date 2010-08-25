package org.mokai;

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
