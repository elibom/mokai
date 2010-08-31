package org.mokai;

/**
 * Acceptors are used inside {@link org.mokai.ProcessorService} to match messages that
 * are going to be processed.
 * 
 * @author German Escobar
 */
public interface Acceptor {

	/**
	 * 
	 * @param message
	 * @return true if the message is accepted, false otherwise.
	 */
	boolean accepts(Message message);

}