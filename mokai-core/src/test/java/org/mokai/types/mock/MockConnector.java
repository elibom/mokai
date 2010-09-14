package org.mokai.types.mock;

import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.Receiver;
import org.mokai.annotation.Resource;
import org.mokai.persist.MessageStore;

public class MockConnector implements Receiver, Processor {
	
	/**
	 * This field is here to test inject resources
	 */
	@Resource
	private MessageStore messageStore;

	@Override
	public void process(Message message) {
		
	}

	@Override
	public boolean supports(Message message) {
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
