package org.mokai;

/**
 * Acceptors are used inside {@link org.mokai.ProcessorService}s to match messages that
 * are going to be processed.
 * 
 * @author German Escobar
 */
public interface Acceptor {

	/**
	 * Decides if a message is accepted or not.
	 * 
	 * @param message
	 * @return true if the message is accepted, false otherwise.
	 */
	boolean accepts(Message message);

}