package org.mokai.impl.camel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.ReceiverService;
import org.mokai.spi.Action;
import org.mokai.spi.Configurable;
import org.mokai.spi.ExecutionException;
import org.mokai.spi.Message;
import org.mokai.spi.MessageProducer;
import org.mokai.spi.Serviceable;
import org.mokai.spi.Message.SourceType;
import org.mokai.spi.Message.Type;
import org.mokai.spi.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelReceiverService implements ReceiverService {
	
	private Logger log = LoggerFactory.getLogger(CamelReceiverService.class);
	
	private String id;
	
	private Object connector;
	
	private List<Action> postReceivingActions;
	
	private Status status;
	
	private CamelContext camelContext;
	
	private List<RouteDefinition> routes;
	
	public CamelReceiverService(String id, Object connector, CamelContext camelContext) 
			throws IllegalArgumentException, ExecutionException {
		
		Validate.notEmpty(id, "An Id must be provided");
		Validate.notNull(connector, "A connector must be provided");
		Validate.notNull(camelContext, "A CamelContext must be provided");
		
		id = StringUtils.lowerCase(id);
		this.id = StringUtils.deleteWhitespace(id);
		this.connector = connector;
	
		this.status = Status.STOPPED;
		this.postReceivingActions = new ArrayList<Action>();
		
		this.camelContext = camelContext;

		init();
	}
	
	private void init() {
		// add the message producer to connector
		addMessageProducerToConnector(connector);		
		
		try {
			
			// configure connector
			if (Configurable.class.isInstance(connector)) {
				Configurable configurableConnector = (Configurable) connector;
				configurableConnector.configure();
			}
			
			RouteBuilder routeBuilder = new RouteBuilder() {
		
				@Override
				public void configure() throws Exception {
					// from the component that receives the messages from the MessageProducer	
					RouteDefinition route = from(getEndpointUri());
					
					// add the source and set type to Message.Type.OUTBOUND
					route.process(new CompleteMessageProcessor());
					
					// execute all the post receiving actions
					route.process(new ActionsProcessor(postReceivingActions));
					
					// route to the outbound router
					route.to("activemq:outboundRouter");
				}
					
			};
				
			camelContext.addRoutes(routeBuilder);
			routes = routeBuilder.getRouteCollection().getRoutes();
				
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
	
	private void addMessageProducerToConnector(Object connector) {
		MessageProducer messageProducer = new MessageProducer() {
			
			ProducerTemplate producer = camelContext.createProducerTemplate();

			@Override
			public void produce(Message message) {
				Validate.notNull(message);
				
				try {
					producer.sendBody(getEndpointUri(), message);
				} catch (CamelExecutionException e) {
					Throwable ex = e;
					if (e.getCause() != null) {
						ex = e.getCause();
					}
					throw new ExecutionException(ex);
				}
			}
			
		};
		
		Field[] fields = connector.getClass().getDeclaredFields();
		for (Field field : fields) {
			
			if (field.isAnnotationPresent(Resource.class) 
					&& field.getType().isInstance(messageProducer)) {
				field.setAccessible(true);
				try {
					field.set(connector, messageProducer);
				} catch (Exception e) {
					throw new ExecutionException(e);
				} 
			}
		}
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public Object getReceiver() {
		return this.connector;
	}

	@Override
	public boolean isServiceable() {
		if (Serviceable.class.isInstance(connector)) {
			return true;
		}
		
		return false;
	}

	@Override
	public ReceiverService addPostReceivingAction(Action action) throws IllegalArgumentException, 
			ObjectAlreadyExistsException {
		
		Validate.notNull(action);
		
		if (postReceivingActions.contains(action)) {
			throw new ObjectAlreadyExistsException("Action " + action + " already exists");
		}
		
		postReceivingActions.add(action);
		
		return this;
	}
	
	@Override
	public ReceiverService removePostReceivingAction(Action action) throws IllegalArgumentException, 
			ObjectNotFoundException {
		
		Validate.notNull(action);
		
		boolean removed = postReceivingActions.remove(action);
		if (!removed) {
			throw new ObjectNotFoundException("Action " + action + " not found");
		}
		
		return this;
	}

	@Override
	public List<Action> getPostReceivingActions() {
		return Collections.unmodifiableList(postReceivingActions);
	}


	@Override
	public Status getStatus() {
		return this.status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws ExecutionException {
		
		try {
			
			if (!status.isStartable()) {
				log.warn("Receiver " + id + " is already started, ignoring call");
				return;
			}
			
			// start the connector if is Serviceable
			if (isServiceable()) {
				Serviceable connectorService = (Serviceable) connector;
				connectorService.doStart();
			} else {
				log.warn("Receiver " + id + " is not Serviceable, ignoring call");
			}
			
			status = Status.STARTED;
			
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
		
	}

	@Override
	public void stop() throws ExecutionException {
		try {
			
			if (!status.isStoppable()) {
				log.warn("Receiver is already stopped, ignoring call");
				return;
			}
			
			if (isServiceable()) {
				Serviceable connectorService = (Serviceable) connector;
				connectorService.doStop();
			} else {
				log.warn("Receiver " + id + " is not Serviceable, ignoring call");
			}
			
			status = Status.STOPPED;
			
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
	
	@Override
	public void destroy() {
		try {
			
			stop();
			
			// invoke the remove method on the connector
			if (Configurable.class.isInstance(connector)) {
				Configurable configurableConnector = (Configurable) connector;
				configurableConnector.destroy();
			}
			
			// stop the routes
			for (RouteDefinition route : routes) {
				camelContext.stopRoute(route);
			}
		} catch (Exception e) {
			log.warn("Exception destroying receiver " + id + ": " + e.getMessage(), e);
		}
	}

	private String getEndpointUri() {
		return "direct:receiver-" + id;
	}
	
	private class CompleteMessageProcessor implements Processor {

		@Override
		public void process(Exchange exchange) throws Exception {
			Message message = exchange.getIn().getBody(Message.class);
			
			message.setSourceType(SourceType.RECEIVER);
			message.setSource(id);
			message.setType(Type.OUTBOUND);
		}
		
	}

}
