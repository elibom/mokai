package org.mokai;

/**
 * Performs an operation with a message (validation, transformation, etc.). Actions
 * can be invoked after messages are received, before and/or after they have been processed
 * by a connector.
 *
 * @author German Escobar
 */
public interface Action {

	/**
	 * This method is called by the routing engine to execute the action.
	 *
	 * @param message the message that is being routed.
	 * @throws Exception if something goes wrong.
	 */
	void execute(Message message) throws Exception;

}