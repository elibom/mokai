package org.mokai.config;

/**
 * Implemented by configuration classes that can load and save information from 
 * a persistence provider.
 * 
 * @author German Escobar
 */
public interface Configuration {

	/**
	 * Loads the configuration. The source of the information is implementation
	 * specific.
	 *  
	 * @throws ConfigurationException wraps any unexpected exception thrown.
	 */
	void load() throws ConfigurationException;
	
	/**
	 * Saves the configuration. The persistence provider of the information is
	 * implementation specific.
	 * 
	 * @throws ConfigurationException wraps any unexpected exception thrown.
	 */
	void save() throws ConfigurationException;
	
}
