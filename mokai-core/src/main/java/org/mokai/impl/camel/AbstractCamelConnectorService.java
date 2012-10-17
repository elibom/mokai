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
import org.mokai.Connector;
import org.mokai.ConnectorContext;
import org.mokai.ConnectorService;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.MessageProducer;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.Monitorable.Status;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.Processor;
import org.mokai.Serviceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConnectorService} implementation based on Apache Camel. Base class of {@link CamelApplicationService}
 * and {@link CamelConnectionService}.
 * 
 * @author German Escobar
 */
public abstract class AbstractCamelConnectorService implements ConnectorService {
	
	private Logger log = LoggerFactory.getLogger(AbstractCamelConnectorService.class);
	
	/**
	 * The default number of consumers for this connector service. This is applied to the
	 * maxConcurrentMsgs attribute of this class.
	 */
	private static final int DEFAULT_MAX_CONCURRENT_MSGS = 1;
	
	/**
	 * The default priority of the connector service. Applied to the priority attribute of 
	 * this class.
	 */
	private static final int DEFAULT_PRIORITY = 1000;
	
	private String id;
	
	private int priority;
	
	private int maxConcurrentMsgs;
	
	private List<Acceptor> acceptors;
	
	private Connector connector;
	
	private List<Action> preProcessingActions;
	
	private List<Action> postProcessingActions;
	
	private List<Action> postReceivingActions;
	
	private State state;
	
	private ResourceRegistry resourceRegistry;
	
	private CamelContext camelContext;
	
	private ConnectorServiceChangeListener changeListener;
	
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
	 * The status of the connector service. Notice that this can differ from the
	 * {@link Connector} status (if it implements {@link Monitorable}).
	 * 
	 * @see ConnectorService#getStatus()
	 */
	private Status status = Status.UNKNOWN;
	
	/**
	 * We keep a record of the messages that have failed consecutively.
	 */
	private int failedMessages;
	
	/**
	 * <p>Constructor. Initializes the object with the received arguments. Modifies the id of the connector by removing 
	 * spaces and lower casing it. Injects some resources to the connector: {@link ResourceRegistry} implementation, 
	 * {@link MessageProducer} implementation and the {@link ConnectorContext}.</p>
	 * 
	 * <p>Notice that this constructor doesn't initialize the routes. See the {@link #start()} method to see how the 
	 * routes are initialized.</p>
	 * 
	 * @param id the id of the connector service. Shouldn't be null or empty.
	 * @param connector the {@link Connector} implementation
	 * @param resourceRegistry a populated {@link ResourceRegistry} implementation with at least the CamelContext and 
	 * the {@link RedeliveryPolicy}, among other resources.
	 * 
	 * @throws IllegalArgumentException if the id, connector, ResourceRegistry or CamelContext are not provided..
	 * @throws ExecutionException if an exception is thrown configuring the connector.
	 * 
	 * @see #start()
	 */
	public AbstractCamelConnectorService(String id, Connector connector, ResourceRegistry resourceRegistry) 
			throws IllegalArgumentException, ExecutionException {
		
		Validate.notEmpty(id, "An id must be provided");
		Validate.notNull(connector, "A connector must be provided");
		Validate.notNull(resourceRegistry, "A ResourceRegistry must be provided");
		Validate.notNull(resourceRegistry.getResource(CamelContext.class), "A CamelContext must be provided");
		
		String fixedId = StringUtils.lowerCase(id);
		this.id = StringUtils.deleteWhitespace(fixedId);
		
		this.priority = DEFAULT_PRIORITY;
		this.maxConcurrentMsgs = DEFAULT_MAX_CONCURRENT_MSGS;
		this.connector = connector;
		
		this.state = State.STOPPED;
		
		this.acceptors = new ArrayList<Acceptor>();
	
		this.preProcessingActions = Collections.synchronizedList(new ArrayList<Action>());
		this.postProcessingActions = Collections.synchronizedList(new ArrayList<Action>());
		this.postReceivingActions = Collections.synchronizedList(new ArrayList<Action>());
		
		this.resourceRegistry = resourceRegistry;
		
		this.camelContext = resourceRegistry.getResource(CamelContext.class);
		this.camelProducer = camelContext.createProducerTemplate();
		
		// inject resources to the connector
		ResourceInjector.inject(connector, resourceRegistry);
		injectMessageProducer(connector);
		injectConnectorContext(connector);
		
	}
	
	/**
	 * Helper method. Called from the {@link #start()} method to initialize the Camel routes that we use 
	 * internally to move messages (eg. outbound router -> acceptor -> pre actions -> connector -> post actions). 
	 * Specifically, it creates outbound routes (if the connector is a processor) and inbound routes.
	 * 
	 * @throws ExecutionException wraps any exception thrown while creating the Camel routes.
	 * 
	 * @see #start()
	 */
	private void initRoutes() throws ExecutionException {
		
		try {

			routes = new ArrayList<RouteDefinition>();
			
			// only add the outbound routes if the connector implements Processor
			if (Processor.class.isInstance(connector)) {
			
				RouteBuilder outboundRouteBuilder = createOutboundRouteBuilder();
				camelContext.addRoutes(outboundRouteBuilder);
				routes.addAll(outboundRouteBuilder.getRouteCollection().getRoutes());
			}
			
			// always add the inbound routes
			RouteBuilder inboundRouteBuilder = createInboundRouteBuilder();
			camelContext.addRoutes(inboundRouteBuilder);
			routes.addAll(inboundRouteBuilder.getRouteCollection().getRoutes());
			
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
		
	}
	
	/**
	 * Helper method. Called from the {@link #initRoutes()} method to initialize Camel routes for outbound routing 
	 * (ie. when the connector implements {@link Processor}).
	 * 
	 * @return an initialized RouteBuilder (internal Camel object).
	 */
	private RouteBuilder createOutboundRouteBuilder() {
		
		// these are the outbound routes
		RouteBuilder outboundRouteBuilder = new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				onException(Exception.class).process(new org.apache.camel.Processor() {

					@Override
					public void process(Exchange exchange) throws Exception {
						Message message = exchange.getIn().getBody(Message.class);
						message.setStatus(Message.STATUS_FAILED);
					}
					
				}).to(getFailedMessagesUri());
				
				// from the connector queue to the pre-processing actions which puts the message(s) in an internal queue
				from(getOutboundUri())
					.process(new OutboundMessageProcessor()) // sets the destination
					.process(new ActionsProcessor(preProcessingActions, getOutboundInternalUri())); // pre-processing actions
				
				// fromt the internal queue to the post-processing actions which puts the message in the processed URI
				from(getOutboundInternalUri())
					.process(new ConnectorProcessor()) // execute the connector
					.process(new ActionsProcessor(postProcessingActions, getProcessedMessagesUri())); 
					
			}
			
		};
		
		return outboundRouteBuilder;
	}
	
	/**
	 * Helper method. Called from the {@link #initRoutes()} method to initialize Camel routes for inbound routing 
	 * (ie. for receiving messages).
	 * 
	 * @return an initialized RouteBuilder (internal Camel object).
	 */
	private RouteBuilder createInboundRouteBuilder() {
		
		// these are the inbound routes
		RouteBuilder inboundRouteBuilder = new RouteBuilder() {

			@Override
			public void configure() throws Exception {;
				
				// from the inbound queue (that receives the messages from the MessageProducer) to the post-receiving
				// actions that puts the message(s) in the router URI
				from(getInboundUri())
					.process(new InboundMessageProcessor())
					.process(new ActionsProcessor(postReceivingActions, getMessagesRouterUri()));
			}
			
		};
		
		return inboundRouteBuilder;
	}
	
	/**
	 * Helper method that injects a {@link MessageProducer} into a connector.
	 * 
	 * @param connector the Connector to which we are injecting the MessageProducer.
	 */
	private void injectMessageProducer(Connector connector) {
		
		// create the message producer that will send the message to an
		// internal queue so we can process it.
		MessageProducer messageProducer = new MessageProducer() {
			
			private ProducerTemplate producer = camelContext.createProducerTemplate();

			@Override
			public void produce(Message message) {
				Validate.notNull(message);
				
				try {
					producer.asyncSendBody(getInboundUri(), message);
				} catch (CamelExecutionException e) {
					Throwable ex = e;
					if (e.getCause() != null) {
						ex = e.getCause();
					}
					throw new ExecutionException(ex);
				}
			}
			
		};
		
		ResourceInjector.inject(connector, messageProducer);
	}
	
	/**
	 * Helper method that injects a {@link ConnectorContext} into a processor.
	 * 
	 * @param processor
	 */
	private void injectConnectorContext(Connector processor) {
		
		// create an implementation of the ProcessorContext interface
		ConnectorContext context = new ConnectorContext() {

			@Override
			public String getId() {
				return id;
			}

			@Override
			public Direction getDirection() {
				return AbstractCamelConnectorService.this.getDirection();
			}
			
		};
		
		ResourceInjector.inject(processor, context);
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
	public ConnectorService withPriority(int priority) {
		setPriority(priority);
		return this;
	}

	@Override
	public int getMaxConcurrentMsgs() {
		return maxConcurrentMsgs;
	}

	@Override
	public void setMaxConcurrentMsgs(int maxConcurrentMsgs) {
		this.maxConcurrentMsgs = maxConcurrentMsgs;
	}

	@Override
	public final Connector getConnector() {
		return this.connector;
	}
	
	@Override
	public final int getNumQueuedMessages() {
		
		BrowsableEndpoint queueEndpoint = camelContext.getEndpoint(getOutboundUriPrefix() + id, BrowsableEndpoint.class);
		int num = queueEndpoint.getExchanges().size();
		
		num += camelContext.getEndpoint(getOutboundInternalUri(), BrowsableEndpoint.class).getExchanges().size();
		
		return num;
		
	}

	@Override
	public final ConnectorService addAcceptor(Acceptor acceptor) throws IllegalArgumentException, 
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
	public final ConnectorService removeAcceptor(Acceptor acceptor) throws IllegalArgumentException, 
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
	public final ConnectorService addPreProcessingAction(Action action) throws IllegalArgumentException, 
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
	public final ConnectorService removePreProcessingAction(Action action) throws IllegalArgumentException, 
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
	public final ConnectorService addPostProcessingAction(Action action) throws IllegalArgumentException, 
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
	public final ConnectorService removePostProcessingAction(Action action) throws IllegalArgumentException,
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
	public final ConnectorService addPostReceivingAction(Action action) throws IllegalArgumentException, 
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
	public final ConnectorService removePostReceivingAction(Action action) throws IllegalArgumentException, 
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
		if (Monitorable.class.isInstance(connector)) {
			Monitorable monitorable = (Monitorable) connector;
			retStatus = monitorable.getStatus();
		}
		
		// resolve conflicts
		if (retStatus.equals(Status.OK) && status.equals(Status.FAILED)) {
			String message = status.getMessage();
			message = "Connector is OK but " + message;
			
			status.setMessage(message);
			
			retStatus = status;
		} 
		
		return retStatus;
	}
	
	/**
	 * This is the queue where messages are stored after the connector service has accepted them. Remember that the 
	 * {@link ConnectionsRouter#route(Exchange)} method is the responsible of choosing the processor service that will
	 * handle the message.
	 * 
	 * @return an Apache Camel endpoint uri where the messages are queued before processing them.
	 * 
	 * @see ConnectionsRouter
	 */
	private String getOutboundUri() {
		return getOutboundUriPrefix() + id;
	}
	
	/**
	 * This is the queue where messages are stored after the pre-processing actions have executed. Notice that this
	 * URI is the one that will have multiple concurrent consumers.
	 * 
	 * @return an Apache Camel endpoint uri where the messages are queued after the pre-processing
	 * actions.
	 */
	private String getOutboundInternalUri() {
		return getOutboundUriPrefix() + "int-" + id + "?maxConcurrentConsumers=" + maxConcurrentMsgs;
	}
	
	/**
	 * This is an endpoint where processors put the received messages before they are passed through the post-receiving 
	 * actions.
	 * 
	 * @return an Apache Camel endpoint Uri where the received messages are stored.
	 */
	private String getInboundUri() {
		return getInboundUriPrefix() + id;
	}

	/**
	 * Starts consuming messages from the queue (returned by {@link AbstractCamelConnectorService#getQueueUri()} 
	 * method) and builds the Apache Camel routes to process and receive messages from the {@link Processor}. 
	 * If the {@link Processor} implements {@link Serviceable}, it will call the {@link Serviceable#doStart()} 
	 * method.
	 */
	@Override
	public final void start() throws ExecutionException {
		
		if (!state.isStartable()) {
			log.warn("Connector " + id + " is already started, ignoring call");
			return;
		}
		
		// start the connector if is Serviceable
		LifecycleMethodsHelper.start(connector);
		
		try {
			
			// create the routes
			if (routes == null) {
				initRoutes();				
			} else {
			
				// start the routes
				for (RouteDefinition route : routes) {
					camelContext.startRoute(route);
				}
				
			}
			
			state = State.STARTED;
			
			if (changeListener != null) {
				changeListener.changed(this, getDirection());
			}
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	/**
	 * Starts consuming messages from the queue (returned by 
	 * {@link AbstractCamelConnectorService#getQueueUri()} method) and stops
	 * the Apache Camel routes. If the {@link Processor} implements
	 * {@link Serviceable}, it calls the {@link Serviceable#doStop()}
	 * method.
	 */
	@Override
	public final void stop() {
		
		if (!state.isStoppable()) {
			log.warn("Connector " + id + " is already stopped, ignoring call");
			return;
		}
			
		// stop the processor if it implements Configurable
		LifecycleMethodsHelper.stop(connector);
			
		try {
			// stop the routes
			for (RouteDefinition route : routes) {
				camelContext.stopRoute(route);
			}
			
			state = State.STOPPED;
			
			if (changeListener != null) {
				changeListener.changed(this, getDirection());
			}
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
		LifecycleMethodsHelper.destroy(connector);
		
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
				
				if (!Processor.class.isInstance(connector)) {
					
					// should not happen but just in case
					throw new IllegalStateException("A message cannot be processed by connector " + id +  
							", it doesn't implements org.mokai.Processor");
				}
				
				// try to process the message
				Processor processor = (Processor) connector;
				processor.process(message);
				message.setStatus(Message.STATUS_PROCESSED);
				
				status = Monitorable.class.isInstance(processor) ? MonitorStatusBuilder.ok() : MonitorStatusBuilder.unknown();
				failedMessages = 0;
				
				return true;
			} catch (Exception e) {
				
				// only retry if we haven't exceeded the max redeliveries
				RedeliveryPolicy redeliveryPolicy = getRedeliveryPolicy();
				int maxRetries = redeliveryPolicy.getMaxRedeliveries();
				if (attempt < maxRetries) {
					
					// wait redelivery delay
					long delay = redeliveryPolicy.getMaxRedeliveryDelay();
					try { this.wait(delay); } catch (Exception f) { }
					
					// MOKAI-20 - return keyword was missing
					return process(message, attempt + 1);
				} else {
					
					// print the stack trace every 50 messages that fail consecutively
					if (failedMessages % 50 == 0) {
						log.error("message failed after " + maxRetries + " retries: " + e.getMessage(), e);
					} else {
						log.error("message failed after " + maxRetries + " retries: " + e.getMessage());
					}
					
					// set the new status
					failedMessages++;
					String failMessage = failedMessages +
						(failedMessages == 1 ? " message has " : " messages have") + "failed.";
					status = MonitorStatusBuilder.failed(failMessage, e);
					
					// send to failed messages
					message.setStatus(Message.STATUS_FAILED);
					camelProducer.sendBody(getFailedMessagesUri(), message);
					
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
			
			// set the direction of the message
			if (getDirection().equals(Direction.TO_APPLICATIONS)) {
				
				message.setDirection(Direction.TO_CONNECTIONS);
				
			} else if (getDirection().equals(Direction.TO_CONNECTIONS)) {
				
				message.setDirection(Direction.TO_APPLICATIONS);
				
			} else {
				message.setDirection(Direction.UNKNOWN);
			}
			
		}
		
	}
	
	public ConnectorServiceChangeListener getChangeListener() {
		return changeListener;
	}

	public void setChangeListener(ConnectorServiceChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	/**
	 * The outbound uri is the endpoint where messages are queued before they are processed by a {@link Processor}.
	 *  
	 * @return a string with the outbound uri prefix.
	 */
	protected abstract String getOutboundUriPrefix();

	/**
	 * The inbound uri is the enpoint where messages are received from a {@link MessageProducer}) and handled
	 * by this connector service.
	 * 
	 * @return a string with the inbound uri prefix.
	 */
	protected abstract String getInboundUriPrefix();
	
	/**
	 * The processed messages uri is the enpoint where processed messages are queued before persisted.
	 * 
	 * @return a string with a processed messages uri.
	 */
	protected abstract String getProcessedMessagesUri();
	
	/**
	 * The failed messages uri is the endpoint where failed messages are queued before persisted.
	 * 
	 * @return a string with a failed messages uri.
	 */
	protected abstract String getFailedMessagesUri();
	
	/**
	 * The messages router uri is the endpoint to which the received messages are routed.
	 * 
	 * @return a string with the messages router uri.
	 */
	protected abstract String getMessagesRouterUri();

	/**
	 * @return the direction that should be assigned to a received message.
	 */
	protected abstract Direction getDirection();
}
