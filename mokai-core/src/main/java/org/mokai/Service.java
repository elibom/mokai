package org.mokai;

import org.mokai.ExecutionException;

/**
 * Implemented by classes that can be started, stopped and return state.
 * 
 * @author German Escobar
 */
public interface Service {

	public enum State {
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
	
	State getState();
}
