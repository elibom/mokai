package org.mokai.types.mock;

import org.mokai.spi.Message;
import org.mokai.spi.Processor;

public class MockConnector implements Processor {

	@Override
	public void process(Message message) {
		
	}

	@Override
	public boolean supports(Message message) {
		return false;
	}

}
