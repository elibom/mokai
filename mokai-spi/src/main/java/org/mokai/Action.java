package org.mokai.spi;

/**
 * Performs an operation with a message. It is used after messages are
 * received, before they are processed or after they have been processed.
 * 
 * @author German Escobar
 */
public interface Action {

	void execute(Message message) throws Exception;
	
}
