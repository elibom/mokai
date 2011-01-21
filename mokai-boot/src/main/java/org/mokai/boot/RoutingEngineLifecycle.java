package org.mokai.boot;

import org.mokai.RoutingEngine;
import org.mokai.Service;
import org.mokai.config.xml.ProcessorConfiguration;
import org.mokai.config.xml.ReceiverConfiguration;

/**
 * This class is configured in the core-context.xml file with dependencies to all the
 * classes that need to be configured before starting the routing engine. See MOKAI-21
 * for more information.
 * 
 * @author German Escobar
 */
public class RoutingEngineLifecycle {

	private RoutingEngine routingEngine;
	
	private ReceiverConfiguration receiverConfiguration;
	private ProcessorConfiguration processorConfiguration;
	
	public void start() {
		processorConfiguration.load();
		receiverConfiguration.load();
		
		if (Service.class.isInstance(routingEngine)) {
			Service service = (Service) routingEngine;
			service.start();
		}
	}
	
	public void stop() {
		if (Service.class.isInstance(routingEngine)) {
			Service service = (Service) routingEngine;
			service.stop();
		}
	}

	public void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

	public void setReceiverConfiguration(ReceiverConfiguration receiverConfiguration) {
		this.receiverConfiguration = receiverConfiguration;
	}

	public void setProcessorConfiguration(
			ProcessorConfiguration processorConfiguration) {
		this.processorConfiguration = processorConfiguration;
	}
	
}
