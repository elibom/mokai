package org.mokai;

/**
 * <p>This interface is implemented by the connectors that want to process
 * messages. Processors usually send messages out of the framework to
 * operators, integrators or other applications.</p>
 *
 * @author German Escobar
 */
public interface Processor extends Connector {

	/**
	 * Called when a message needs to be processed.
	 * @param message the {@link Message} that needs to be processed.
	 * @throws Exception
	 */
	void process(Message message) throws Exception;

	/**
	 * Tells whether a message is supported by this processor or not.
	 * @param message the {@link Message} to be tested
	 * @return true if the message is supported, false otherwise.
	 */
	boolean supports(Message message);

}
