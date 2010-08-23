package org.mokai;

/**
 * Performs an operation with a message (validation, transformation, etc.). Actions 
 * are invoked after messages are received, before they are processed or after they 
 * have been processed.
 * 
 * @author German Escobar
 */
public interface Action {

	void execute(Message message) throws Exception;
	
}
