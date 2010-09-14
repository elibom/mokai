package org.mokai;

/**
 * Used by {@link Receiver}s to inject messages into the gateway. 
 * 
 * @author German Escobar
 */
public interface MessageProducer {

	/**
	 * Called by the {@link Receiver} when a message needs to be routed 
	 * inside the gateway.
	 * 
	 * @param message the {@link Message} that is going to be routed
	 * @throws IllegalArgumentException if message is null 
	 * @throws ExecutionException wraps any exception thrown producing the 
	 * message. This includes all the flow through the receiver, until the
	 * message is queued.
	 */
	void produce(Message message) throws IllegalArgumentException, 
			ExecutionException;
	
}
