package org.mokai.types.mock;

import org.mokai.Acceptor;
import org.mokai.Message;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;
import org.mokai.persist.MessageStore;

/**
 * 
 * @author German Escobar
 */
@Name("MockAcceptor") // do not change, this is validated in the tests
@Description("Mock Acceptor Description") // do not change
public class MockAcceptor implements Acceptor {
	
	/**
	 * This field is here to test inject resources
	 */
	@Resource
	private MessageStore messageStore;

	@Override
	public boolean accepts(Message message) {
		return false;
	}

	/**
	 * This method is here to test inject resources
	 * 
	 * @return
	 */
	public MessageStore getMessageStore() {
		return messageStore;
	}

}
