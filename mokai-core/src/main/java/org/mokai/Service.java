package org.mokai;

import org.mokai.spi.ExecutionException;

/**
 * 
 * @author German Escobar
 */
public interface Service {

	public enum Status {
		STARTED, STOPPED;
		
		public boolean isStartable() {
			return this == STOPPED;
		}
		
		public boolean isStoppable() {
			return this == STARTED;
		}
	};
	
	void start() throws ExecutionException;
	
	void stop() throws ExecutionException;
	
	Status getStatus();
}
