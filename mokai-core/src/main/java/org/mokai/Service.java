package org.mokai;

import org.mokai.ExecutionException;

/**
 * Implemented by classes that can be started, stopped and return state.
 * 
 * @author German Escobar
 */
public interface Service {

	/**
	 * Possible states of a {@link Service}.
	 * 
	 * @author German Escobar
	 */
	public enum State {
		STARTED, STOPPED;
		
		public boolean isStartable() {
			return this == STOPPED;
		}
		
		public boolean isStoppable() {
			return this == STARTED;
		}
	};
	
	/**
	 * Starts the service and updates it's state to {@link State#STARTED}. 
	 * 
	 * @throws ExecutionException wraps any exception thrown by the
	 * service while starting.
	 */
	void start() throws ExecutionException;
	
	/**
	 * Stops the service and updates it's state to {@link State#STOPPED}.
	 * 
	 * @throws ExecutionException wraps any exception thrown by the 
	 * service while stopping.
	 */
	void stop() throws ExecutionException;
	
	/**
	 * @return the state of the service
	 * @see State
	 */
	State getState();
}
