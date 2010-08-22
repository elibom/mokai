package org.mokai.types.mock;

import org.mokai.Acceptor;
import org.mokai.Message;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;

/**
 * 
 * @author German Escobar
 */
@Name("MockAcceptor") // do not change, this is validated in the tests
@Description("Mock Acceptor Description") // do not change
public class MockAcceptor implements Acceptor {

	@Override
	public boolean accepts(Message message) {
		return false;
	}

}
