package org.mokai.spi;

/**
 * <p>This interface is implemented by the connectors that want to process 
 * messages. Processors usually send messages out of the framework to 
 * operators or applications.</p>
 * 
 * When a processor is added to the the {@link RoutingEngine} or to an
 * {@link Account}, it is wrapped into a {@link ProcessorService} which
 * contains a queue, acceptors, pre-processing actions, post-processing
 * actions and post-receiving actions.
 * 
 * @author German Escobar
 */
public interface Processor {

	/**
	 * Called when a message needs to be processed.
	 * @param message the {@link Message} that needs to be processed.
	 */
	void process(Message message);
	
	/**
	 * Tells whether a message is supported by this processor or not.
	 * @param message the {@link Message} to be tested
	 * @return true if the message is supported, false otherwise.
	 */
	boolean supports(Message message);
	
}
