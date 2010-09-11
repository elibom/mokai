package org.mokai;

/**
 * Performs an operation with a message (validation, transformation, etc.). Actions 
 * are invoked after messages are received, before they are processed or after they 
 * have been processed.
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
