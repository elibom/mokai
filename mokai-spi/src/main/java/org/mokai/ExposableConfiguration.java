package org.mokai;

/**
 * Implemented by extensions (receivers, processors, acceptors and actions)
 * that expose a configuration object.
 * 
 * @author German Escobar
 *
 * @param <T> the type of the configuration object.
 */
public interface ExposableConfiguration<T> {

	/**
	 * The configuration object.
	 * 
	 * @return returns the configuration object.
	 */
	T getConfiguration();
	
}
