package org.mokai;

/**
 * Implemented by receivers and processors that can be started and stopped.
 *
 * @author German Escobar
 */
public interface Serviceable {

	/**
	 * Lifecycle callback called when the service starts.
	 *
	 * @throws Exception if something goes wrong.
	 */
	void doStart() throws Exception;

	/**
	 * Lifecycle callback called when the service stops.
	 *
	 * @throws Exception if something goes wrong.
	 */
	void doStop() throws Exception;

}
