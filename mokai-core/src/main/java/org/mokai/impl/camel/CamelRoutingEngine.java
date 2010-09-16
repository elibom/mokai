package org.mokai.impl.camel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mokai.ExecutionException;
import org.mokai.Message;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.Processor;
import org.mokai.ProcessorService;
import org.mokai.Receiver;
import org.mokai.ReceiverService;
import org.mokai.RoutingEngine;
import org.mokai.Service;
import org.mokai.Message.Direction;
import org.mokai.Message.Status;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.MessageCriteria.OrderType;
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
	
	private Map<String,ReceiverService> receivers = new HashMap<String,ReceiverService>();
	
	private Map<String,ProcessorService> processors = new HashMap<String,ProcessorService>();
	
	private CamelContext camelContext;
	
	private JmsComponent jmsComponent;
	
	private ResourceRegistry resourceRegistry;
	
	private State state = State.STOPPED;
	
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
		
		final OutboundRouter outboundRouter = new OutboundRouter();
		outboundRouter.setRoutingContext(this);
		
		try {
			camelContext.addRoutes(new RouteBuilder() {
	
				@Override
				public void configure() throws Exception {
					// internal router					
					from("activemq:outboundRouter").bean(outboundRouter);
					
					// sent messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor sentProcessor = new PersistenceProcessor(resourceRegistry);
					from("direct:processedmessages").process(sentProcessor);
					
					// failed messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor failedProcessor = new PersistenceProcessor(resourceRegistry);
					from("activemq:failedmessages").process(failedProcessor);
					
					// unroutable messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor unRoutableProcessor = new PersistenceProcessor(resourceRegistry);
					from("activemq:unroutablemessages").process(unRoutableProcessor);
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
	public final void start() throws ExecutionException {
		
		if (!state.isStartable()) {
			return;
		}
		
		try {
			camelContext.start();
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
		
		// start processors
		for (ProcessorService processor : processors.values()) {
			try {
				processor.start();
			} catch (Exception e) {
				log.error("processor '" + processor.getId() + "' couldnt be started: "  + e.getMessage(), e);
			}
		}
			
		// start recievers
		for (ReceiverService receiver : receivers.values()) {
			try {
				receiver.start();
			} catch (Exception e) {
				log.error("receiver '" + receiver.getId() + "' couldnt be started: " + e.getMessage(), e);
			}
		}
			
		state = State.STARTED;
	}

	@Override
	public final void stop() throws ExecutionException {
		
		if (!state.isStoppable()) {
			return;
		}
		
		// stop processors
		for (ProcessorService processor : processors.values()) {
			try {
				processor.stop();
			} catch (Exception e) {
				log.error("processor '" + processor.getId() + "' couldnt be stopped: "  + e.getMessage(), e);
			}
		}
			
		// stop receivers
		for (ReceiverService receiver : receivers.values()) {
			try {
				receiver.stop();
			} catch (Exception e) {
				log.error("receiver '" + receiver.getId() + "' couldnt be stopped: " + e.getMessage(), e);
			}
		}
			
		try {	
			camelContext.stop();
			
			state = State.STOPPED;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	/*@SuppressWarnings("unused")
	private JmsComponent createActiveMQComponent() {
		PooledConnectionFactory pooledActiveMQCF = new PooledConnectionFactory("vm://localhost?broker.persistent=true");
		pooledActiveMQCF.setMaxConnections(8);
		pooledActiveMQCF.setMaximumActive(500);
		
		JmsTransactionManager txManager = new JmsTransactionManager(pooledActiveMQCF);
		
		JmsConfiguration configuration = new JmsConfiguration();
		configuration.setDeliveryPersistent(true);
		configuration.setTransacted(true);
		configuration.setTransactionManager(txManager);
		configuration.setConnectionFactory(pooledActiveMQCF);
		
		JmsComponent activeComponent = ActiveMQComponent.jmsComponent(configuration);
		
		return activeComponent;
	}*/

	@Override
	public final ProcessorService createProcessor(String id, int priority,
			Processor processor) throws IllegalArgumentException,
			ObjectAlreadyExistsException {
		
		// fix id
		String fixedId = StringUtils.lowerCase(id);
		fixedId = StringUtils.deleteWhitespace(fixedId);
		
		// check if already exists
		if (processors.containsKey(fixedId)) {
			throw new ObjectAlreadyExistsException("Processor with id " + fixedId + " already exists");
		}
		
		// create and start the ProcessorService instance
		CamelProcessorService processorService = 
			new CamelProcessorService(fixedId, priority, processor, resourceRegistry);
		if (state.equals(State.STARTED)) {
			processorService.start();
		}
		
		// add to the map
		processors.put(fixedId, processorService);
		
		return processorService;
	}

	@Override
	public final RoutingEngine removeProcessor(String id)
			throws IllegalArgumentException, ObjectNotFoundException {
		Validate.notEmpty(id);
		
		if (!processors.containsKey(id)) {
			throw new ObjectNotFoundException("Processor with id " + id + " doesnt exists");
		}
		
		ProcessorService processorService = processors.get(id);
		processorService.destroy();
		
		processors.remove(id);
		
		return this;
	}

	@Override
	public final ProcessorService getProcessor(String id) {
		Validate.notEmpty(id);
		
		return processors.get(id);
	}

	@Override
	public final List<ProcessorService> getProcessors() {
		List<ProcessorService> processorsList = 
			new ArrayList<ProcessorService>(processors.values());
		
		Collections.sort(processorsList, new Comparator<ProcessorService>() {

			@Override
			public int compare(ProcessorService o1, ProcessorService o2) {
				if (o1.getPriority() > o2.getPriority()) {
					return 1;
				} else if (o1.getPriority() < o2.getPriority()) {
					return -1;
				}
				
				return 0;
			}
			
		});
		
		return Collections.unmodifiableList(processorsList);
	}
	
	@Override
	public final ReceiverService createReceiver(String id, Receiver receiver)
			throws IllegalArgumentException, ObjectAlreadyExistsException {
		
		// fix id
		String fixedId = StringUtils.lowerCase(id);
		fixedId = StringUtils.deleteWhitespace(fixedId);
		
		// check if already exists
		if (receivers.containsKey(fixedId)) {
			throw new ObjectAlreadyExistsException("Receiver with id " + fixedId + " already exists");
		}
		
		// create and start the ReceiverService instance
		CamelReceiverService receiverService = new CamelReceiverService(fixedId, receiver, resourceRegistry);
		if (state.equals(State.STARTED)) {
			receiverService.start();
		}
		
		// add to the map
		receivers.put(fixedId, receiverService);
		
		return receiverService;
	}

	@Override
	public final ReceiverService getReceiver(String id) {
		Validate.notNull(id);
		
		return receivers.get(id);
	}

	@Override
	public final Collection<ReceiverService> getReceivers() {
		List<ReceiverService> receiversList = 
			new ArrayList<ReceiverService>(receivers.values());
		
		return Collections.unmodifiableList(receiversList);
	}

	@Override
	public final RoutingEngine removeReceiver(String id)
			throws IllegalArgumentException, ObjectNotFoundException {
		Validate.notEmpty(id);
		
		if (!receivers.containsKey(id)) {
			throw new ObjectNotFoundException("Receiver with id " + id + " doesnt exists");
		}
		
		ReceiverService receiverService = receivers.get(id);
		receiverService.destroy();
		
		receivers.remove(id);
		
		return this;
	}

	public final void retryFailedMessages() {
		log.debug("running ... ");
		
		MessageStore messageStore = resourceRegistry.getResource(MessageStore.class); 
		
		// update all the failed messages to retrying
		MessageCriteria criteria = new MessageCriteria()
			.addStatus(Status.FAILED)
			.direction(Direction.OUTBOUND);
		messageStore.updateStatus(criteria, Status.RETRYING);
		
		ProducerTemplate producer = camelContext.createProducerTemplate();
		
		criteria = new MessageCriteria()
			.addStatus(Message.Status.RETRYING)
			.orderBy("creation_time")
			.orderType(OrderType.UPWARDS);

		Collection<Message> messages = messageStore.list(criteria);
		logCollectionSize(messages.size());
		for (Message message : messages) {
			producer.sendBody("activemq:outboundRouter", ExchangePattern.InOnly, message);
		}
		
		log.debug("finished");
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
