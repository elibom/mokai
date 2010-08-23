package org.mokai;

/**
 * Marks a connector (receiver and/or processor) as configurable. 
 * 
 * @author German Escobar
 */
public interface Configurable {

	/**
	 * 
	 * @throws Exception
	 */
	void configure() throws Exception;
	
	void destroy() throws Exception;
	
}
