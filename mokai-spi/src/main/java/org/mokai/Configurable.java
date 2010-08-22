package org.mokai;

/**
 * 
 * @author German Escobar
 */
public interface Configurable {

	void configure() throws Exception;
	
	void destroy() throws Exception;
	
}
