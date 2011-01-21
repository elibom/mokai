package org.mokai.impl.camel;

import org.mokai.Configurable;
import org.mokai.ExecutionException;
import org.mokai.Serviceable;

/**
 * Helper class with static methods to support lifecycle operations on objects.
 * 
 * @author German Escobar
 */
public class LifecycleMethodsHelper {

	private LifecycleMethodsHelper() {
		
	}
	
	/**
	 * Helper method that calls the {@link Serviceable#doStart()} method
	 * on the object if it implements {@link Serviceable}.
	 * 
	 * @param object the object on which we are going to call the doStart
	 * method.
	 * @throws ExecutionException if the doStart method throws an exception.
	 */
	public static void start(Object object) throws ExecutionException {

		try {
			// start the object if it implements Serviceable
			if (Serviceable.class.isInstance(object)) {
				Serviceable connectorService = (Serviceable) object;
				connectorService.doStart();
			}
		} catch (Exception e) {
			throw new ExecutionException("Exception while starting object: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Helper method that calls the {@link Serviceable#doStop()} method
	 * on the object if it implements {@link Serviceable}.
	 * 
	 * @param object the object on which we are going to call the doStop
	 * method.
	 * @throws ExecutionException if the doStop method throws an exception.
	 */
	public static void stop(Object object) throws ExecutionException {
		
		try {
			// stop the object if it implements Serviceable
			if (Serviceable.class.isInstance(object)) {
				Serviceable connectorService = (Serviceable) object;
				connectorService.doStop();
			} 
		} catch (Exception e) {
			throw new ExecutionException("Exception while stopping object: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Helper method that calls the {@link Configurable#configure()} method
	 * on the object if it implements {@link Configurable}.
	 * 
	 * @param object the object on which we are going to call the configure
	 * method. 
	 * @throws ExecutionException if the configure method throws an exception.
	 */
	public static void configure(Object object) throws ExecutionException {
		
		try {
			if (Configurable.class.isInstance(object)) {
				Configurable configurable = (Configurable) object;
				configurable.configure();
			}
		} catch (Exception e) {
			throw new ExecutionException("Exception while configuring object: " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Helper method that calls the {@link Configurable#destroy()} method
	 * on the object if it implements {@link Configurable}.
	 * 
	 * @param object the object on which we are going to call the destroy
	 * method.
	 * @throws ExecutionException the the destroy method throws an exception.
	 */
	public static void destroy(Object object) throws ExecutionException {
		
		try {
			if (Configurable.class.isInstance(object)) {
				Configurable configurable = (Configurable) object;
				configurable.destroy();
			}
		} catch (Exception e) {
			throw new ExecutionException("Exception while destroying object: " + e.getMessage(), e);
		}
		
	}
	
}