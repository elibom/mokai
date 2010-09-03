package org.mokai.impl.camel;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.RecipientList;
import org.mokai.Acceptor;
import org.mokai.Message;
import org.mokai.ProcessorService;
import org.mokai.RoutingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class decides which {@link ProcessorService} will handle a message using
 * the {@link org.mokai.Processor#supports(Message)} method and the {@link Acceptor}s 
 * of each {@link ProcessorService}. It is configured in the {@link CamelRoutingEngine}
 * class.
 * 
 * @author German Escobar
 */
public class OutboundRouter {
	
	private Logger log = LoggerFactory.getLogger(OutboundRouter.class);
	
	private RoutingEngine routingContext;

	@RecipientList
	public final String route(Exchange exchange) {
		Message message = exchange.getIn().getBody(Message.class);
		
		return route(message);
	}
	
	private String route(Message message) {
		// try to route the message
		List<ProcessorService> processorServices = routingContext.getProcessors();
		for (ProcessorService processorService : processorServices) {
			
			// check if the processor supports the message
			boolean supported = false;
			if (processorService.getProcessor().supports(message)) {
				supported = true; 
			}
			
			// check the acceptors only if the message is supported 
			if (supported) {
				List<Acceptor> acceptors = processorService.getAcceptors();
				for (Acceptor acceptor : acceptors) {
					try {
						if (acceptor.accepts(message)) {
							return "activemq:processor-" + processorService.getId();
						}
					} catch (Exception e) {
						log.error("Exception while calling Acceptor " + acceptor + ": " + e.getMessage(), e);						
					}
				}
			}
		}
		
		// unroutable
		message.setStatus(Message.Status.UNROUTABLE);
		return "activemq:unroutablemessages";
	}

	public final void setRoutingContext(RoutingEngine routingContext) {
		this.routingContext = routingContext;
	}
}
