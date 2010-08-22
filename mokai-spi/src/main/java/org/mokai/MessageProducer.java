package org.mokai;

/**
 * Used to produce messages from the connectors. 
 * 
 * @author German Escobar
 */
public interface MessageProducer {

	/**
	 * Called when a message needs to be routed inside the framework. The 
	 * destination where the message is routed is implementation specific.
	 * @param message the {@link Message} that is going to be routed
	 * @throws IllegalArgumentException if message is null 
	 * @throws ExecutionException wraps any exception thrown producing the 
	 * message. This includes all the flow through the receiver, until the
	 * message is queued.
	 */
	void produce(Message message) throws IllegalArgumentException, 
			ExecutionException;
	
}
