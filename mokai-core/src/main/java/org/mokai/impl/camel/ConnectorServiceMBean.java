package org.mokai.impl.camel;

import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.annotation.ManagedAttribute;
import net.gescobar.jmx.annotation.ManagedOperation;

/**
 * This class will be exposed as an JMX Bean wrapping an {@link AbstractCamelConnectorService} and exposing some
 * of its attributes and methods.
 *
 * @author German Escobar
 */
public class ConnectorServiceMBean {

	private AbstractCamelConnectorService connectorService;

	public ConnectorServiceMBean(AbstractCamelConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@ManagedAttribute(description="The unique id of the connector")
	public String getId() {
		return connectorService.getId();
	}

	@ManagedAttribute(description="The priority of the connector")
	public int getPriority() {
		return connectorService.getPriority();
	}

	@ManagedAttribute(description="The max number of messages that can be processed concurrently")
	public int getMaxConcurrentMsgs() {
		return connectorService.getMaxConcurrentMsgs();
	}

	@ManagedAttribute(description="The number of queued messages")
	public int getNumQueuedMessages() {
		return connectorService.getNumQueuedMessages();
	}

	@ManagedAttribute(description="The state of the connector: STARTED or STOPPED")
	public String getState() {
		return connectorService.getState().name();
	}

	@ManagedAttribute(description="The status of the connector: OK, FAILED or UNKNOWN")
	public String getStatus() {
		return connectorService.getStatus().name();
	}

	@ManagedOperation(impact=Impact.ACTION, description="Starts the connector")
	public void start() {
		connectorService.start();
	}

	@ManagedOperation(impact=Impact.ACTION, description="Stops the connector")
	public void stop() {
		connectorService.stop();
	}

}
