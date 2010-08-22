package org.mokai.types.mock;

import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.Receiver;

public class MockConnector implements Receiver, Processor {

	@Override
	public void process(Message message) {
		
	}

	@Override
	public boolean supports(Message message) {
		return false;
	}

}
