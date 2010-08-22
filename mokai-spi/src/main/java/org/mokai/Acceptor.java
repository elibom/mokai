package org.mokai;

/**
 * @author German Escobar
 */
public interface Acceptor {

	/**
	 * 
	 * @param message
	 * @return
	 */
	boolean accepts(Message message);

}