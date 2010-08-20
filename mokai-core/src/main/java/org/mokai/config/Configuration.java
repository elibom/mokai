package org.mokai.config;

/**
 * 
 * @author German Escobar
 */
public interface Configuration {

	void load() throws ConfigurationException;
	
	void save() throws ConfigurationException;
	
}
