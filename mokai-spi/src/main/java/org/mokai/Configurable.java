package org.mokai;

/**
 * Implemented by extensions (receivers, processors, acceptors and actions) 
 * that need to be configured when added to the routing engine and destroyed
 * when removed. 
 * 
 * @author German Escobar
 */
public interface Configurable {

	/**
	 * Lifecycle method called when the extension is added to the 
	 * routing engine.
	 * 
	 * @throws Exception if something goes wrong.
	 */
	void configure() throws Exception;
	
	/**
	 * Lifecycle method called when the extension is removed from the
	 * routing engine.
	 * 
	 * @throws Exception if something goes wrong.
	 */
	void destroy() throws Exception;
	
}
