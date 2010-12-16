package org.mokai.impl.camel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.BrowsableEndpoint;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Configurable;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.Processor;
import org.mokai.ProcessorService;
import org.mokai.Serviceable;
import org.mokai.Message.DestinationType;
import org.mokai.Monitorable.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ProcessorService} implementation based on Apache Camel.
 * 
 * @author German Escobar
 */
public class CamelProcessorService implements ProcessorService {
	
	private Logger log = LoggerFactory.getLogger(CamelProcessorService.class);
	
	private String id;
	
	private int priority;
	
	private List<Acceptor> acceptors;
	
	private Processor processor;
	
	private List<Action> preProcessingActions;
	
	private List<Action> postProcessingActions;
	
	private List<Action> postReceivingActions;
	
	private State state;
	
	private ResourceRegistry resourceRegistry;
	
	private CamelContext camelContext;
	
	/**
	 * Used to send messages to Apache Camel endpoints.
	 */
	private ProducerTemplate camelProducer;
	
	/**
	 * Maintains a list of Apache Camel routes that we can stop.
	 * @see #start()
	 * @see #stop()
	 */
	private List<RouteDefinition> routes;
	
	/**
	 * The status of the processor service. Notice that this can differ from the
	 * {@link Processor} status (if it implements {@link Monitorable}).
	 * 
	 * @see ProcessorService#getStatus()
	 */
	private Status status = Status.UNKNOWN;
	
	/**
	 * We keep a record of the messages that have failed consecutively.
	 */
	private int failedMessages;
	
	/**
	 * Constructor. The id is modified by removing spaces and lowercasing it.
	 * 
	 * @param id the id of the processor service. Shouldn't be null or empty.
	 * @param priority the priority of the processor service (can be positive
	 * or negative)
	 * @param processor the {@link Processor} implementation
	 * @param resourceRegistry a populated {@link ResourceRegistry} implementation with
	 * at least the CamelContext and the {@link RedeliveryPolicy}, among other resources.
	 * @throws IllegalArgumentException if the id arg is null or empty, or if the 
	 * processor arg is null.
	 * @throws ExecutionException if an exception is thrown configuring the processor
	 */
	public CamelProcessorService(String id, int priority, Processor processor, ResourceRegistry resourceRegistry) 
			throws IllegalArgumentException, ExecutionException {
		
		Validate.notEmpty(id, "An id must be provided");
		Validate.notNull(processor, "A processor must be provided");
		Validate.notNull(resourceRegistry, "A ResourceRegistry must be provided");
		Validate.notNull(resourceRegistry.getResource(CamelContext.class), "A CamelContext must be provided");
		
		String fixedId = StringUtils.lowerCase(id);
		this.id = StringUtils.deleteWhitespace(fixedId);
		this.priority = priority;
		this.processor = processor;
		
		this.state = State.STOPPED;
		
		this.acceptors = new ArrayList<Acceptor>();
	
		this.preProcessingActions = new ArrayList<Action>();
		this.postProcessingActions = new ArrayList<Action>();
		this.postReceivingActions = new ArrayList<Action>();
		
		this.resourceRegistry = resourceRegistry;
		
		this.camelContext = resourceRegistry.getResource(CamelContext.class);
		this.camelProducer = camelContext.createProducerTemplate();
		
		// add the message producer to the processor
		ResourceInjector.inject(processor, resourceRegistry);
		injectMessageProducer(processor);
		
		// configure the processor if it implements Configurable
		LifecycleMethodsHelper.configure(processor);
	}
	
	/**
	 * Helper method to inject a {@link MessageProducer} into a processor.
	 * 
	 * @param processor
	 */
	private void injectMessageProducer(Processor processor) {
		
		// create the message producer that will send the message to an
		// internal queue so we can process it.
		MessageProducer messageProducer = new MessageProducer() {
			
			private ProducerTemplate producer = camelContext.createProducerTemplate();

			@Override
			public void produce(Message message) {
				Validate.notNull(message);
				
				try {
					producer.sendBody(getInternalUri(), message);
				} catch (CamelExecutionException e) {
					Throwable ex = e;
					if (e.getCause() != null) {
						ex = e.getCause();
					}
					throw new ExecutionException(ex);
				}
			}
			
		};
		
		ResourceInjector.inject(processor, messageProducer);
	}
	
	@Override
	public final String getId() {
		return this.id;
	}
	
	@Override
	public final int getPriority() {
		return this.priority;
	}

	@Override
	public final void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public final Processor getProcessor() {
		return this.processor;
	}
	
	@Override
	public final int getNumQueuedMessages() {
		BrowsableEndpoint queueEndpoint = camelContext.getEndpoint("activemq:processor-" + id, BrowsableEndpoint.class);
		return queueEndpoint.getExchanges().size();
	}

	@Override
	public final ProcessorService addAcceptor(Acceptor acceptor) throws IllegalArgumentException, 
			ObjectAlreadyExistsException, ExecutionException {
		
		Validate.notNull(acceptor);
		
		// check if the acceptor already exists
		if (acceptors.contains(acceptor)) {
			throw new ObjectAlreadyExistsException("Acceptor " + acceptor + " already exists");
		}
		
		// inject the resources
		ResourceInjector.inject(acceptor, resourceRegistry);
		
		// configure if it implements Configurable
		LifecycleMethodsHelper.configure(acceptor);
		
		// add the acceptor to the collection of acceptors
		this.acceptors.add(acceptor);
		
		return this;
	}
	
	@Override
	public final ProcessorService removeAcceptor(Acceptor acceptor) throws IllegalArgumentException, 
			ObjectNotFoundException {
		
		Validate.notNull(acceptor);
		
		boolean removed = acceptors.remove(acceptor);
		if (!removed) {
			throw new ObjectNotFoundException("Acceptor " + acceptor + " not found");
		}
		
		// destroy if it implements Configurable
		LifecycleMethodsHelper.destroy(acceptor);
		
		return this;
	}
	
	@Override
	public final List<Acceptor> getAcceptors() {
		return Collections.unmodifiableList(acceptors);
	}
	
	@Override
	public final ProcessorService addPreProcessingAction(Action action) throws IllegalArgumentException, 
			ObjectAlreadyExistsException {
		
		Validate.notNull(action);
		
		// validate if the action already exists
		if (preProcessingActions.contains(action)) {
			throw new ObjectAlreadyExistsException("Action " + action + " already exists");
		}
		
		// inject the resources
		ResourceInjector.inject(action, resourceRegistry);
		
		// configure if it implements Configurable
		LifecycleMethodsHelper.configure(action);
		
		// add the action to the collection of pre-processing actions
		this.preProcessingActions.add(action);
		
		return this;
	}
	
	@Override
	public final ProcessorService removePreProcessingAction(Action action) throws IllegalArgumentException, 
			ObjectNotFoundException {
		
		Validate.notNull(action);
		
		boolean removed = preProcessingActions.remove(action);
		if (!removed) {
			throw new ObjectNotFoundException("Action " + action + " not found");
		}
		
		// destroy if it implements Configurable
		LifecycleMethodsHelper.destroy(action);
		
		return this;
	}
	
	@Override
	public final List<Action> getPreProcessingActions() {
		return Collections.unmodifiableList(preProcessingActions);
	}

	@Override
	public final ProcessorService addPostProcessingAction(Action action) throws IllegalArgumentException, 
			ObjectAlreadyExistsException {
		
		Validate.notNull(action);
		
		// validate if the action already exists
		if (postProcessingActions.contains(action)) {
			throw new ObjectAlreadyExistsException("Action " + action + " already exists");
		}
		
		// inject the resources
		ResourceInjector.inject(action, resourceRegistry);
		
		// configure if it implements Configurable
		LifecycleMethodsHelper.configure(action);
		
		// add the action to the collection of post-processing actions
		this.postProcessingActions.add(action);
		
		return this;
	}
	
	@Override
	public final ProcessorService removePostProcessingAction(Action action) throws IllegalArgumentException,
			ObjectNotFoundException {
		
		Validate.notNull(action);
		
		boolean removed = postProcessingActions.remove(action);
		if (!removed) {
			throw new ObjectNotFoundException("Action " + action + " not found");
		}
		
		// destroy if it implements Configurable
		LifecycleMethodsHelper.destroy(action);
		
		return this;
	}
	
	@Override
	public final List<Action> getPostProcessingActions() {
		return Collections.unmodifiableList(postProcessingActions);
	}

	@Override
	public final ProcessorService addPostReceivingAction(Action action) throws IllegalArgumentException, 
			ObjectAlreadyExistsException {
		
		Validate.notNull(action);
		
		// validate if the action already exists
		if (postReceivingActions.contains(action)) {
			throw new ObjectAlreadyExistsException("Action " + action + " already exists");
		}
		
		// inject the resources
		ResourceInjector.inject(action, resourceRegistry);
		
		// configure if it implements Configurable
		LifecycleMethodsHelper.configure(action);
		
		// add the action to the post-receiving actions
		this.postReceivingActions.add(action);
		
		return this;
	}
	
	@Override
	public final ProcessorService removePostReceivingAction(Action action) throws IllegalArgumentException, 
			ObjectNotFoundException {
		
		Validate.notNull(action);
		
		boolean remove = postReceivingActions.remove(action);
		if (!remove) {
			throw new ObjectNotFoundException("Action " + action + " not found");
		}
		
		// destroy if it implements Configurable
		LifecycleMethodsHelper.destroy(action);
		
		return this;
	}

	@Override
	public final List<Action> getPostReceivingActions() {
		return Collections.unmodifiableList(postReceivingActions);
	}

	@Override
	public final State getState() {
		return state;
	}

	@Override
	public final Status getStatus() {
		
		Status retStatus = status; // the status we are returning
		
		// check if the processor is monitorable
		if (Monitorable.class.isInstance(processor)) {
			Monitorable monitorable = (Monitorable) processor;
			retStatus = monitorable.getStatus();
		}
		
		// resolve conflicts
		if (retStatus.equals(Status.OK) && status.equals(Status.FAILED)) {
			String message = status.getMessage();
			message = "Processor is OK but " + message;
			
			status.setMessage(message);
			
			retStatus = status;
		} 
		
		return retStatus;
	}
	
	/**
	 * This is the queue where messages are stored after the processor service
	 * has accepted them. Remember that he {@link OutboundRouter#route(Exchange)} 
	 * method is the responsible of choosing the processor service that will
	 * handle the message.
	 * 
	 * @return an Apache Camel endpoint uri where the messages are queued
	 * before processing them.
	 * 
	 * @see OutboundRouter
	 */
	private String getQueueUri() {
		return "activemq:processor-" + id;
	}
	
	/**
	 * This is an endpoint where processors put the received messages before they
	 * are passed through the post-receiving actions.
	 * 
	 * @return an Apache Camel endpoint Uri where the received messages are stored.
	 */
	private String getInternalUri() {
		return "direct:processor-" + id;
	}

	/**
	 * Starts consuming messages from the queue (returned by 
	 * {@link CamelProcessorService#getQueueUri()} method) and builds the Apache 
	 * Camel routes to process and receive messages from the {@link Processor}. 
	 * If the {@link Processor} implements {@link Serviceable}, it will call the 
	 * {@link Serviceable#doStart()} method.
	 */
	@Override
	public final void start() throws ExecutionException {
		
		if (!state.isStartable()) {
			log.warn("Processor " + id + " is already started, ignoring call");
			return;
		}
		
		// start the connector if is Serviceable
		LifecycleMethodsHelper.start(processor);
		
		try {
			
			// check if the routes already exists and start them
			if (routes != null) {
				
				for (RouteDefinition route : routes) {
					camelContext.startRoute(route);
				}
				
			} else { // if no routes yet then create them!
				
				// these are the outbound routes
				RouteBuilder outboundRouteBuilder = new RouteBuilder() {

					@Override
					public void configure() throws Exception {
						onException(Exception.class).process(new org.apache.camel.Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								Message message = exchange.getIn().getBody(Message.class);
								message.setStatus(Message.Status.FAILED);
							}
							
						}).to("activemq:failedmessages");

						ActionsProcessor preProcessingActionsProcessor = new ActionsProcessor(preProcessingActions);
						ActionsProcessor postProcessingActionsProcessor = new ActionsProcessor(postProcessingActions);
						
						// from the queue
						RouteDefinition route = from(getQueueUri());
						
						// set the source and type of the Message
						route.process(new OutboundMessageProcessor());
						
						// execute the pre-processing actions
						route.process(preProcessingActionsProcessor);
						
						// call the processor (this process the message)
						route.process(new ConnectorProcessor());
						
						// execute the post-processing actions
						route.process(postProcessingActionsProcessor);
						
						// route to the processed messages
						route.to("direct:processedmessages");
							
					}
					
				};
			
				// these are the inbound routes
				RouteBuilder inboundRouteBuilder = new RouteBuilder() {
		
					@Override
					public void configure() throws Exception {
						ActionsProcessor postReceivingActionsProcessor = new ActionsProcessor(postReceivingActions);
						
						// from the component that receives the messages from the MessageProducer	
						RouteDefinition route = from(getInternalUri());
						
						// add the source and type of the Message
						route.process(new InboundMessageProcessor());
						
						// execute the post-receiving actions
						route.process(postReceivingActionsProcessor);
						
						// route to the inbound router
						route.to("activemq:inboundRouter");
					}
					
				};
				
				camelContext.addRoutes(outboundRouteBuilder);
				camelContext.addRoutes(inboundRouteBuilder);
				
				routes = outboundRouteBuilder.getRouteCollection().getRoutes();
				routes.addAll(inboundRouteBuilder.getRouteCollection().getRoutes());
				
			}
			
			state = State.STARTED;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	/**
	 * Starts consuming messages from the queue (returned by 
	 * {@link CamelProcessorService#getQueueUri()} method) and stops
	 * the Apache Camel routes. If the {@link Processor} implements
	 * {@link Serviceable}, it calls the {@link Serviceable#doStop()}
	 * method.
	 */
	@Override
	public final void stop() {
		
		if (!state.isStoppable()) {
			log.warn("Processor " + id + " is already stopped, ignoring call");
			return;
		}
			
		// stop the processor if it implements Configurable
		LifecycleMethodsHelper.stop(processor);
			
		try {
			// stop the routes
			for (RouteDefinition route : routes) {
				camelContext.stopRoute(route);
			}
			
			state = State.STOPPED;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
	
	/**
	 * Calls the {@link #stop()} method. If the {@link Processor} implements
	 * {@link Configurable}, it calls the {@link Configurable#destroy()}
	 * method. 
	 */
	@Override
	public final void destroy() {
		
		// stop and destroy
		stop();
		LifecycleMethodsHelper.destroy(processor);
		
	}
	
	@Override
	public final String toString() {
		return id;
	}

	/**
	 * An Apache Camel Processor that calls the {@link Processor#process(Message)}
	 * method. It uses the {@link RedeliveryPolicy} to retry the message if it
	 * fails.
	 * 
	 * @author German Escobar
	 */
	private class ConnectorProcessor implements org.apache.camel.Processor {
		
		@Override
		public void process(Exchange exchange) throws Exception {
			Message message = exchange.getIn().getBody(Message.class);

			// we know we support the message
			boolean success = process(message, 1);
			if (!success) {
				exchange.setProperty(Exchange.ROUTE_STOP, true);
			}
		}
		
		private boolean process(Message message, int attempt) {
			
			try {
				// try to process the message
				processor.process(message);
				message.setStatus(Message.Status.PROCESSED);
				
				status = MonitorStatusBuilder.ok();
				failedMessages = 0;
				
				return true;
			} catch (Exception e) {
				
				// only retry if we haven't exceeded the max redeliveries
				RedeliveryPolicy redeliveryPolicy = getRedeliveryPolicy();
				int maxRetries = redeliveryPolicy.getMaxRedeliveries();
				if (attempt < maxRetries) {
					log.warn("message failed, retrying " + attempt + " of " + maxRetries);
					
					// wait redelivery delay
					long delay = redeliveryPolicy.getMaxRedeliveryDelay();
					try { this.wait(delay); } catch (Exception f) { }
					
					// retry
					attempt++;
					
					// MOKAI-20 - return keyword was missing
					return process(message, attempt);
				} else {
					log.error("message failed after " + maxRetries + " retries: " + e.getMessage(), e);
					
					// set the new status
					failedMessages++;
					String failMessage = failedMessages +
						(failedMessages == 1 ? " message has " : " messages have") + "failed.";
					status = MonitorStatusBuilder.failed(failMessage, e);
					
					// send to failed messages
					message.setStatus(Message.Status.FAILED);
					camelProducer.sendBody("activemq:failedmessages", message);
					
				}
				
				return false;
				
			}
		}
		
		private RedeliveryPolicy getRedeliveryPolicy() {
			
			RedeliveryPolicy redeliveryPolicy = resourceRegistry.getResource(RedeliveryPolicy.class);
			if (redeliveryPolicy == null) {
				redeliveryPolicy = new RedeliveryPolicy();
			}
			
			return redeliveryPolicy;
		}
		
	}
	
	/**
	 * An Apache Camel Processor that sets the destination and the destination
	 * type of the {@link Message}s
	 * 
	 * @author German Escobar
	 */
	private class OutboundMessageProcessor implements org.apache.camel.Processor {
		
		@Override
		public void process(Exchange exchange) throws Exception {
			Message message = exchange.getIn().getBody(Message.class);
			
			message.setDestination(id);
			message.setDestinationType(DestinationType.PROCESSOR);
		}
	}
	
	/**
	 * An Apache Camel Processor that sets the source, source type and the
	 * flow of the {@link Message}s.
	 * 
	 * @author German Escobar
	 */
	private class InboundMessageProcessor implements org.apache.camel.Processor {
		
		@Override
		public void process(Exchange exchange) throws Exception {
			Message message = exchange.getIn().getBody(Message.class);
			
			message.setSource(id);
			message.setSourceType(Message.SourceType.PROCESSOR);
			message.setDirection(Message.Direction.INBOUND);
		}
		
	}

}
