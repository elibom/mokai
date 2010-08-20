package org.mokai.impl.camel;


public class QueueImpl {
	
	/*private String id;
	private int priority;
	private List<Acceptor<?>> acceptors;
	private ConsumerServiceImpl consumerService;
	
	private RedeliveryPolicy redeliveryPolicy;

	private CamelContext camelContext;
	
	public QueueImpl(String id) {
		this(id, 0);
	}
	
	public QueueImpl(String id, int priority) {
		this(null, id, priority);
	}
	
	public QueueImpl(CamelContext camelContext, String id, int priority) {
		this.camelContext = camelContext;
		this.id = id;
		this.priority = priority;
		
		acceptors = new ArrayList<Acceptor<?>>();
	}

	@Override
	public Queue addAcceptor(Acceptor<?> acceptor) {
		this.acceptors.add(acceptor);
		
		return this;
	}

	@Override
	public List<Acceptor<?>> getAcceptors() {
		return acceptors;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public Queue removeAcceptor(Acceptor<?> acceptor) {
		acceptors.remove(acceptor);
		
		return this;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public Queue withPriority(int priority) {
		this.setPriority(priority);
		return this;
	}

	@Override
	public ConsumerService getConsumerService() {
		return consumerService;
	}

	@Override
	public Queue removeConsumer() {
		if (consumerService == null) {
			throw new ObjectNotFoundException();
		}
		
		if (consumerService.getStatus().isStoppable()) {
			consumerService.stop();
		}
		
		consumerService = null;
		
		return this;
	}

	@Override
	public void setConsumer(ProcessorPattern<?> consumer) {
		if (consumerService != null) {
			throw new ObjectAlreadyExistsException();
		}
		
		consumerService = new ConsumerServiceImpl(this, consumer);
		consumerService.setCamelContext(camelContext);
		consumerService.setRedeliveryPolicy(redeliveryPolicy);
		
		consumerService.start();
	}

	@Override
	public ConsumerService withConsumer(ProcessorPattern<?> consumer) {
		this.setConsumer(consumer);
		return consumerService;
	}

	public void setRedeliveryPolicy(RedeliveryPolicy redeliveryPolicy) {
		this.redeliveryPolicy = redeliveryPolicy;
	}

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}*/

}
