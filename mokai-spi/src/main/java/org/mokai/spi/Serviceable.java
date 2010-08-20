package org.mokai.spi;

/**
 * Implemented by all the objects that can be started and stopped.
 * 
 * @author German Escobar
 */
public interface Serviceable {

	void doStart() throws Exception;
	
	void doStop() throws Exception;
	
}
