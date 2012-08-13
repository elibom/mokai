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
	
	@ManagedOperation(description="Starts the Routing Engine", impact=Impact.ACTION)
	public void start() {
		routingEngine.start();
	}
	
	@ManagedOperation(description="Stops the Routing Engine", impact=Impact.ACTION)
	public void stop() {
		routingEngine.stop();
	}
	
	@ManagedAttribute(description="The state of the Routing Engine")
	public String getState() {
		return routingEngine.getState().name();
	}
	
	@ManagedAttribute(description="Number of messages that failed or are being retried.")
	public int getNumFailedMessages() {
		
		MessageCriteria criteria = new MessageCriteria()
			.addStatus(Message.STATUS_FAILED)
			.addStatus(Message.STATUS_RETRYING);
	
		return routingEngine.getMessageStore().list(criteria).size();

	}
	
	@ManagedAttribute(description="Number of messages queued in the connections router.")
	public int getNumQueuedInConnectionsRouter() {
		return routingEngine.getNumQueuedInConnectionsRouter();
	}
	
	@ManagedAttribute(description="Number of messages queued in the connections router.")
	public int getNumQueuedInApplicationsRouter() {
		return routingEngine.getNumQueuedInApplicationsRouter();
	}
	
}
