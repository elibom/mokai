package org.mokai.impl.camel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.RoutingEngine;
import org.mokai.Service;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.persist.MessageStore;
import org.mokai.persist.impl.DefaultMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An <a href="http://camel.apache.org">Apache Camel</a> based implementation of the {@link RoutingEngine}
 * 
 * @author German Escobar
 */
public class CamelRoutingEngine implements RoutingEngine, Service {
	
	private Logger log = LoggerFactory.getLogger(CamelRoutingEngine.class);
	
	private Map<String,ConnectorService> applications = new ConcurrentHashMap<String,ConnectorService>();
	
	private Map<String,ConnectorService> connections = new ConcurrentHashMap<String,ConnectorService>();
	
	private CamelContext camelContext;
	
	private JmsComponent jmsComponent;
	
	private ResourceRegistry resourceRegistry;
	
	private State state = State.STOPPED;
	
	private ExecutorService executor = 
			new ThreadPoolExecutor(2, 4, Long.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	
	public CamelRoutingEngine() {
		this.jmsComponent = defaultJmsComponent();
		init();
	}
	
	/**
	 * Used by the constructor to create a "default" JmsComponent that will
	 * be used in the routing engine.
	 * 
	 * @return a Camel JmsComponent object.
	 */
	private JmsComponent defaultJmsComponent() {
		// a simple activemq connection factory
		ActiveMQConnectionFactory connectionFactory = 
			new ActiveMQConnectionFactory("vm://broker1?broker.persistent=false");
		
		// create the default JmsComponent 
		JmsComponent jmsComponent = new JmsComponent();
		jmsComponent.setConnectionFactory(connectionFactory);
		
		return jmsComponent; 
	}
	
	public CamelRoutingEngine(JmsComponent jmsComponent) {
		this.jmsComponent = jmsComponent;
		init();
	}
	
	private void init() throws ExecutionException {
		
		resourceRegistry = new ResourceRegistry();
		
		// create a default redelivery policy and add it to the resource registry
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		resourceRegistry.putResource(RedeliveryPolicy.class, redeliveryPolicy);		
		
		// create a default message store and add it to the registry
		MessageStore messageStore = new DefaultMessageStore();
		resourceRegistry.putResource(MessageStore.class, messageStore);
		
		camelContext = new DefaultCamelContext();
		resourceRegistry.putResource(CamelContext.class, camelContext);

		camelContext.addComponent("activemq", jmsComponent);
		
		final ConnectionsRouter connectionsRouter = new ConnectionsRouter();
		connectionsRouter.setRoutingEngine(this);
		
		final ApplicationsRouter applicationsRouter = new ApplicationsRouter();
		applicationsRouter.setRoutingEngine(this);
		
		try {
			camelContext.addRoutes(new RouteBuilder() {
	
				@Override
				public void configure() throws Exception {
					// internal router					
					from(UriConstants.CONNECTIONS_ROUTER).bean(connectionsRouter);
					
					// sent messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor sentProcessor = new PersistenceProcessor(resourceRegistry);
					from(UriConstants.CONNECTIONS_PROCESSED_MESSAGES).process(sentProcessor);
					
					// failed messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor failedProcessor = new PersistenceProcessor(resourceRegistry);
					from(UriConstants.CONNECTIONS_FAILED_MESSAGES).process(failedProcessor);
					
					// unroutable messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor unRoutableProcessor = new PersistenceProcessor(resourceRegistry);
					from(UriConstants.CONNECTIONS_UNROUTABLE_MESSAGES).process(unRoutableProcessor);
				}
				
			});
			
			camelContext.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					// internal router					
					from(UriConstants.APPLICATIONS_ROUTER).bean(applicationsRouter);
					
					// sent messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor sentProcessor = new PersistenceProcessor(resourceRegistry);
					from(UriConstants.APPLICATIONS_PROCESSED_MESSAGES).process(sentProcessor);
					
					// failed messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor failedProcessor = new PersistenceProcessor(resourceRegistry);
					from(UriConstants.APPLICATIONS_FAILED_MESSAGES).process(failedProcessor);
					
					// unroutable messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor unRoutableProcessor = new PersistenceProcessor(resourceRegistry);
					from(UriConstants.APPLICATIONS_UNROUTABLE_MESSAGES).process(unRoutableProcessor);
				}
				
			});
			
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
		
	}
	
	@Override
	public final State getState() {
		return state;
	}

	@Override
	public final synchronized void start() throws ExecutionException {
		
		log.debug("Mokai starting ... ");
		
		if (!state.isStartable()) {
			log.warn("Mokai already started!");
			return;
		}
		
		try {
			camelContext.start();
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
		
		// start applications in separate threads
		for (final ConnectorService cs : applications.values()) {
			
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						cs.start();
					} catch (Exception e) {
						log.error("application '" + cs.getId() + "' couldn't be started: "  + e.getMessage(), e);
					}
				}
				
			});	
		}
			
		// start connections in separate threads
		for (final ConnectorService cs : connections.values()) {
			
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						cs.start();
					} catch (Exception e) {
						log.error("connection '" + cs.getId() + "' couldnt be started: " + e.getMessage(), e);
					}		
				}
				
			});
			
		}
			
		state = State.STARTED;
		
		log.info("<<< Mokai started >>>");
	}

	@Override
	public final synchronized void stop() throws ExecutionException {
		
		log.debug("Mokai stopping ... ");
		
		if (!state.isStoppable()) {
			log.warn("Mokai already stopped!");
			return;
		}
		
		// stop applications
		for (final ConnectorService cs : applications.values()) {
			try {
				cs.stop();
			} catch (Exception e) {
				log.error("application '" + cs.getId() + "' couldnt be destroyed: " + e.getMessage(), e);
			}
			
		}
		
		// stop connections
		for (final ConnectorService cs : connections.values()) {
			try {
				cs.stop();
			} catch (Exception e) {
				log.error("connection '" + cs.getId() + "' couldnt be destroyed: "  + e.getMessage(), e);
			}
		}
			
		try {	
			camelContext.stop();
			
			state = State.STOPPED;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
		
		log.info("<<< Mokai stopped >>>");
	}
	
	@Override
	public final ConnectorService addApplication(String id, Connector connector) 
			throws IllegalArgumentException, ObjectAlreadyExistsException {
		
		// fix id
		String fixedId = StringUtils.lowerCase(id);
		fixedId = StringUtils.deleteWhitespace(fixedId);		
		
		// check if already exists
		if (applications.containsKey(fixedId)) {
			throw new ObjectAlreadyExistsException("Application with id '" + fixedId + "' already exists");
		}
		
		log.debug("adding application with id '" + fixedId + "'");
		
		// create the ConnectorService instance
		CamelApplicationService applicationService = new CamelApplicationService(fixedId, connector, resourceRegistry);
		
		// configure the connector
		LifecycleMethodsHelper.configure(connector);
		
		applications.put(fixedId, applicationService);
		
		log.info("application with id " + fixedId + " added");
		
		return applicationService;
		
	}

	@Override
	public final RoutingEngine removeApplication(String id) throws IllegalArgumentException, ObjectNotFoundException {
		removeAndDestroyConnector(id, applications, "applications");
		return this;
	}

	@Override
	public final ConnectorService getApplication(String id) {
		return getConnector(id, applications);
	}

	@Override
	public final List<ConnectorService> getApplications() {
		return getConnectors(applications);
	}
	
	@Override
	public final ConnectorService addConnection(String id, Connector connector) 
			throws IllegalArgumentException, ObjectAlreadyExistsException {
		
		// fix id
		String fixedId = StringUtils.lowerCase(id);
		fixedId = StringUtils.deleteWhitespace(fixedId);		
		
		// check if already exists
		if (connections.containsKey(fixedId)) {
			throw new ObjectAlreadyExistsException("Connection with id '" + fixedId + "' already exists");
		}
		
		log.debug("adding connection with id '" + fixedId + "'");
		
		// create the ConnectorService instance
		CamelConnectionService connectionService = new CamelConnectionService(fixedId, connector, resourceRegistry);
		
		// configure the connector
		LifecycleMethodsHelper.configure(connector);
		
		connections.put(fixedId, connectionService);
		
		log.info("connection with id " + fixedId + " added");
		
		return connectionService;
		
	}

	@Override
	public final RoutingEngine removeConnection(String id) throws IllegalArgumentException, ObjectNotFoundException {
		removeAndDestroyConnector(id, connections, "connections");
		return this;
	}

	@Override
	public final ConnectorService getConnection(String id) {
		return getConnector(id, connections);
	}

	@Override
	public final List<ConnectorService> getConnections() {
		return getConnectors(connections);
	}
	
	private void removeAndDestroyConnector(String id, Map<String,ConnectorService> map, String mapName) {
		Validate.notEmpty(id);
		
		ConnectorService cs = map.remove(id);
		if (cs == null) {
			throw new ObjectNotFoundException("Connector with id " + id + " doesnt exists in map of " + mapName);
		}
		
		// call the destroy method on the processor or receiver service
		cs.destroy();
		
	}
	
	private ConnectorService getConnector(String id, Map<String,ConnectorService> map) {
		Validate.notEmpty(id);

		ConnectorService cs = map.get(id);
		if (cs != null && ConnectorService.class.isInstance(cs)) {
			return cs;
		}
		
		return null;
	}
	
	private List<ConnectorService> getConnectors(Map<String,ConnectorService> map) {
		
		List<ConnectorService> connectorsList = new ArrayList<ConnectorService>();
		
		// add only the processor services
		for (ConnectorService cs : map.values()) {
			connectorsList.add(cs);
		}
			
		// sort the processors by priority
		Collections.sort(connectorsList, new Comparator<ConnectorService>() {

			@Override
			public int compare(ConnectorService o1, ConnectorService o2) {
				if (o1.getPriority() > o2.getPriority()) {
					return 1;
				} else if (o1.getPriority() < o2.getPriority()) {
					return -1;
				}
				
				return 0;
			}
			
		});
		
		return connectorsList;
	}

	public final void retryFailedMessages() {

		log.trace("retrying failed messages ... ");
		
		long startTime = new Date().getTime();
		
		MessageStore messageStore = resourceRegistry.getResource(MessageStore.class); 
		
		ProducerTemplate producer = camelContext.createProducerTemplate();
		
		MessageCriteria criteria = new MessageCriteria()
			.addStatus(Message.STATUS_FAILED)
			.orderBy("creation_time")
			.orderType(OrderType.UPWARDS);

		Collection<Message> messages = messageStore.list(criteria);
		logCollectionSize(messages.size()); // log the size of the collection
		for (Message message : messages) {
			
			// update the message and send it
			message.setStatus(Message.STATUS_RETRYING);
			message.setModificationTime(new Date());
			messageStore.saveOrUpdate(message);
			
			if (message.getDirection().equals(Direction.TO_CONNECTIONS)) {
				producer.sendBody(UriConstants.CONNECTIONS_ROUTER, ExchangePattern.InOnly, message);
			} else if (message.getDirection().equals(Direction.TO_APPLICATIONS)) {
				producer.sendBody(UriConstants.APPLICATIONS_ROUTER, ExchangePattern.InOnly, message);
			} else {
				log.warn("message with direction " + message.getDirection() + " cannot be retried ... ignoring");
			}
		}
		
		long endTime = new Date().getTime();
		
		log.debug("retry failed messages took " + (endTime - startTime) + " milis.");
	}
	
	private void logCollectionSize(int size) {
		if (size > 0) {
			log.debug("processing " + size + " failed messages ...");
		}
	}

	public final void retryUnRoutableMessages() {
		
	}

	public final RedeliveryPolicy getRedeliveryPolicy() {
		return resourceRegistry.getResource(RedeliveryPolicy.class);
	}

	public final void setRedeliveryPolicy(RedeliveryPolicy redeliveryPolicy) {
		resourceRegistry.getResource(RedeliveryPolicy.class);
	}

	@Override
	public final MessageStore getMessageStore() {
		return resourceRegistry.getResource(MessageStore.class);
	}

	public final void setMessageStore(MessageStore messageStore) {
		resourceRegistry.putResource(MessageStore.class, messageStore);
	}

	public final CamelContext getCamelContext() {
		return camelContext;
	}

}
