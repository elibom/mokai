package org.mokai.impl.camel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mokai.Message;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;
import org.mokai.Processor;
import org.mokai.ProcessorService;
import org.mokai.Receiver;
import org.mokai.ReceiverService;
import org.mokai.RoutingEngine;
import org.mokai.Service;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.StoreException;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.persist.impl.DefaultMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.JmsTransactionManager;

/**
 * An Apache Camel based implementation of the {@link RoutingEngine}
 * 
 * @author German Escobar
 */
public class CamelRoutingEngine implements RoutingEngine, Service {
	
	private Logger log = LoggerFactory.getLogger(CamelRoutingEngine.class);
	
	private Map<String,ReceiverService> receivers = new HashMap<String,ReceiverService>();
	
	private Map<String,ProcessorService> processors = new HashMap<String,ProcessorService>();
	
	private CamelContext camelContext;
	
	private JmsComponent jmsComponent;
	
	private RedeliveryPolicy redeliveryPolicy;
	
	private MessageStoreDelegate messageStoreDelegate;
	
	private Status state;
	
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
	protected JmsComponent defaultJmsComponent() {
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
	
	@Override
	public Status getStatus() {
		return state;
	}

	@Override
	public void start() {
		try {
			camelContext.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		try {
			camelContext.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void init() {
		// create a default redelivery policy
		redeliveryPolicy = new RedeliveryPolicy();		
		
		// create a default message store and wrap it as a delegate
		MessageStore delegate = new DefaultMessageStore();
		messageStoreDelegate = new MessageStoreDelegate(delegate);
		
		camelContext = new DefaultCamelContext();

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
					PersistenceProcessor sentProcessor = new PersistenceProcessor(messageStoreDelegate);
					from("direct:processedmessages").process(sentProcessor);
					
					// failed messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor failedProcessor = new PersistenceProcessor(messageStoreDelegate);
					from("activemq:failedmessages").process(failedProcessor);
					
					// unroutable messages - we pass a delegate in case the MessageStore changes
					PersistenceProcessor unRoutableProcessor = new PersistenceProcessor(messageStoreDelegate);
					from("activemq:unroutablemessages").process(unRoutableProcessor);
				}
				
			});
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@SuppressWarnings("unused")
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
	}

	@Override
	public ProcessorService createProcessor(String id, int priority,
			Processor processor) throws IllegalArgumentException,
			ObjectAlreadyExistsException {
		
		// fix id
		id = StringUtils.lowerCase(id);
		id = StringUtils.deleteWhitespace(id);
		
		// check if already exists
		if (processors.containsKey(id)) {
			throw new ObjectAlreadyExistsException("Processor with id " + id + " already exists");
		}
		
		// create and start the ProcessorService instance
		CamelProcessorService processorService = 
			new CamelProcessorService(id, priority, processor, camelContext);
		processorService.start();
		
		// add to the map
		processors.put(id, processorService);
		
		return processorService;
	}

	@Override
	public RoutingEngine removeProcessor(String id)
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
	public ProcessorService getProcessor(String id) {
		Validate.notEmpty(id);
		
		return processors.get(id);
	}

	@Override
	public List<ProcessorService> getProcessors() {
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
	public ReceiverService createReceiver(String id, Receiver receiver)
			throws IllegalArgumentException, ObjectAlreadyExistsException {
		
		// fix id
		id = StringUtils.lowerCase(id);
		id = StringUtils.deleteWhitespace(id);
		
		// check if already exists
		if (receivers.containsKey(id)) {
			throw new ObjectAlreadyExistsException("Receiver with id " + id + " already exists");
		}
		
		// create and start the ReceiverService instance
		CamelReceiverService receiverService = new CamelReceiverService(id, receiver, camelContext);
		receiverService.start();
		
		// add to the map
		receivers.put(id, receiverService);
		
		return receiverService;
	}

	@Override
	public ReceiverService getReceiver(String id) {
		Validate.notNull(id);
		
		return receivers.get(id);
	}

	@Override
	public Collection<ReceiverService> getReceivers() {
		List<ReceiverService> receiversList = 
			new ArrayList<ReceiverService>(receivers.values());
		
		return Collections.unmodifiableList(receiversList);
	}

	@Override
	public RoutingEngine removeReceiver(String id)
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

	public void retryFailedMessages() {
		log.debug("running ... ");
		
		ProducerTemplate producer = camelContext.createProducerTemplate();
		
		MessageCriteria criteria = new MessageCriteria()
			.addStatus(Message.Status.FAILED)
			.orderBy("creation_time")
			.orderType(OrderType.UPWARDS)
			.firstRecord(0)
			.numRecords(500);
			
		Collection<Message> messages = messageStoreDelegate.list(criteria);
		logCollectionSize(messages.size());
		for (Message message : messages) {
			producer.sendBody("activemq:router", ExchangePattern.InOnly, message);
		}
		
		log.debug("finished");
	}
	
	private void logCollectionSize(int size) {
		if (size > 0) {
			log.debug("processing " + size + " failed messages ...");
		}
	}

	public void retryUnRoutableMessages() {
		
	}

	public RedeliveryPolicy getRedeliveryPolicy() {
		return redeliveryPolicy;
	}

	public void setRedeliveryPolicy(RedeliveryPolicy redeliveryPolicy) {
		this.redeliveryPolicy = redeliveryPolicy;
	}

	public MessageStore getMessageStore() {
		return messageStoreDelegate.getDelegate();
	}

	public void setMessageStore(MessageStore messageStore) {
		this.messageStoreDelegate.setDelegate(messageStore);
	}

	public CamelContext getCamelContext() {
		return camelContext;
	}
	
	/**
	 * This class is a {@link MessageStore} that delegates to another
	 * {@link MessageStore}. The reason it exists, is that we can allow
	 * to change the {@link MessageStore} dynamically through the 
	 * {@link #setMessageStore(MessageStore} meth
	 * 
	 * @author German Escobar
	 */
	private class MessageStoreDelegate implements MessageStore {
		
		private MessageStore delegate;
		
		public MessageStoreDelegate(MessageStore delegate) {
			this.delegate = delegate;
		}

		@Override
		public Collection<Message> list(MessageCriteria criteria)
				throws StoreException {
			return delegate.list(criteria);
		}

		@Override
		public void saveOrUpdate(Message message) throws StoreException {
			delegate.saveOrUpdate(message);
		}

		public MessageStore getDelegate() {
			return delegate;
		}

		public void setDelegate(MessageStore delegate) {
			this.delegate = delegate;
		}
		
	}

}
