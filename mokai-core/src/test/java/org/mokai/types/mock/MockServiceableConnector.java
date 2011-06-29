package org.mokai.types.mock;

import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.Connector;
import org.mokai.Serviceable;

public class MockServiceableConnector implements Processor, Serviceable, Connector {
	
	private long startWaitTime;
	
	private long stopWaitTime;
	
	private boolean throwExceptionOnStart = false;
	
	private boolean throwExceptionOnStop = false;

	@Override
	public void doStart() throws Exception {
		Thread.sleep(startWaitTime);
		
		if (throwExceptionOnStart) {
			throw new NullPointerException();
		}
	}

	@Override
	public void doStop() throws Exception {
		Thread.sleep(stopWaitTime);
		
		if (throwExceptionOnStop) {
			throw new NullPointerException();
		}
	}

	@Override
	public void process(Message message) throws Exception {
		
	}

	@Override
	public boolean supports(Message message) {
		return false;
	}
	
	public MockServiceableConnector withStartWaitTime(long startWaitTime) {
		this.startWaitTime = startWaitTime;
		return this;
	}
	
	public MockServiceableConnector withStopWaitTime(long stopWaitTime) {
		this.stopWaitTime = stopWaitTime;
		return this;
	}
	
	public MockServiceableConnector throwExceptionOnStart() {
		this.throwExceptionOnStart = true;
		return this;
	}
	
	public MockServiceableConnector throwExceptionOnStop() {
		this.throwExceptionOnStop = true;
		return this;
	}

}
