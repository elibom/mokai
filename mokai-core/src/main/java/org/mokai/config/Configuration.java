package org.mokai.config;

/**
 * Implemented by configuration classes that can load and save information.
 * 
 * @author German Escobar
 */
public interface Configuration {

	void load() throws ConfigurationException;
	
	void save() throws ConfigurationException;
	
}
