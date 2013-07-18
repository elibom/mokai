package org.mokai.impl.camel;

import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.annotation.ManagedAttribute;
import net.gescobar.jmx.annotation.ManagedOperation;

import org.mokai.Message;
import org.mokai.persist.MessageCriteria;

/**
 * This class will be exposed as an JMX Bean wrapping a {@link CamelRoutingEngine} and exposing some of its methods
 * and operations.
 *
 * @author German Escobar
 */
public class RoutingEngineMBean {

	private CamelRoutingEngine routingEngine;

	public RoutingEngineMBean(CamelRoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

	@ManagedOperation(description="Starts all the applications and connections that are stopped.", impact=Impact.ACTION)
	public void start() {
		routingEngine.start();
	}

	@ManagedOperation(description="Stops all the applications and connections that are started.", impact=Impact.ACTION)
	public void stop() {
		routingEngine.stop();
	}

	@ManagedAttribute(description="Number of messages that failed or are being retried.")
	public int getNumFailedMessages() {
		MessageCriteria criteria = new MessageCriteria()
			.addStatus(Message.STATUS_FAILED)
			.addStatus(Message.STATUS_RETRYING);

		return routingEngine.getMessageStore().list(criteria).size();
	}

}
