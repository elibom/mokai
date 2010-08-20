package org.mokai.types.mock;

import org.mokai.spi.Acceptor;
import org.mokai.spi.Message;

public class MockAcceptor implements Acceptor {

	@Override
	public boolean accepts(Message message) {
		return false;
	}

}
