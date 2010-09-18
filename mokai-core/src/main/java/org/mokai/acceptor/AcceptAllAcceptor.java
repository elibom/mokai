package org.mokai.acceptor;

import org.mokai.Acceptor;
import org.mokai.Message;

/**
 * An acceptor that accepts all messages.
 * 
 * @author German Escobar
 */
public class AcceptAllAcceptor implements Acceptor {

	@Override
	public boolean accepts(Message message) {
		return true;
	}

}
