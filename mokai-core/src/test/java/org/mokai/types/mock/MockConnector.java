package org.mokai.types.mock;

import org.mokai.Connector;
import org.mokai.ConnectorContext;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Processor;
import org.mokai.annotation.Resource;
import org.mokai.persist.MessageStore;

public class MockConnector implements Connector, Processor {

	/**
	 * This field is here to test inject resources
	 */
	@Resource
	private MessageStore messageStore;

	@Resource
	private ConnectorContext context;

	@Resource
	private MessageProducer messageProducer;

	@Override
	public void process(Message message) {

	}

	@Override
	public boolean supports(Message message) {
		return false;
	}

	public void produceMessage(Message message) {
		messageProducer.produce(message);
	}

	/**
	 * This method is here to test inject resources
	 *
	 * @return
	 */
	public MessageStore getMessageStore() {
		return messageStore;
	}

	public ConnectorContext getContext() {
		return context;
	}

}
