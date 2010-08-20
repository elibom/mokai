package org.mokai.impl.camel;


public class ConsumerServiceImpl  {
	
	/*private Logger log = LoggerFactory.getLogger(ConsumerServiceImpl.class);
	
	private QueueImpl queue;
	
	private ProcessorPattern<?> consumer;
	
	private RedeliveryPolicy redeliveryPolicy;
	
	private CamelContext camelContext;
	
	private Status state;
	
	private List<RouteDefinition> routes;
	
	public ConsumerServiceImpl(QueueImpl queue, ProcessorPattern<?> consumer) {
		this.queue = queue;
		this.consumer = consumer;
		
		this.state = Status.STOPPED;
	}

	@Override
	public Status getStatus() {
		return state;
	}

	@Override
	public void start() {
		try {
			if (routes != null) {
				for (RouteDefinition route : routes) {
					camelContext.startRoute(route);
				}
				
				state = Status.STARTED;
				
				return;
			}
		
			// create the actual consumer route
			RouteBuilder subRouteBuilder = new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					// this route will be called from the ConsumerProcessor
					RouteDefinition subRoute = from("direct:" + queue.getId());
					consumer.completeRoute(subRoute);
				}
				
			};
			
			// create the ConsumerService route wrapper
			RouteBuilder consumerRouteBuilder = new RouteBuilder() {

				@Override
				public void configure() throws Exception {
					RouteDefinition route = from("activemq:" + queue.getId());
					route.process(new ConsumerProcessor());
					route.bean(PersistenceRouter.class);
				}
				
			};
		
			camelContext.addRoutes(consumerRouteBuilder);
			camelContext.addRoutes(subRouteBuilder);
			
			// retrieve the consumer route, this is what we are going to actually stop
			routes = consumerRouteBuilder.getRouteCollection().getRoutes();
			routes.addAll(subRouteBuilder.getRouteCollection().getRoutes());
			
			state = Status.STARTED;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		try {
			for (RouteDefinition route : routes) {
				camelContext.stopRoute(route);
			}
			
			state = Status.STOPPED;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ProcessorPattern<?> getConsumer() {
		return consumer;
	}

	@Override
	public String toString() {
		return consumer.toString();
	}
	
	public void setRedeliveryPolicy(RedeliveryPolicy redeliveryPolicy) {
		this.redeliveryPolicy = redeliveryPolicy;
	}

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}



	private class ConsumerProcessor implements Processor {
		
		ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

		@Override
		public void process(Exchange exchange) throws Exception {
						
			// copy the exchange and change the exchange pattern
			Exchange copy = exchange.copy();
			copy.setPattern(ExchangePattern.InOut);
			
			// try to process the message
			int maxRetries = redeliveryPolicy.getMaxRedeliveries();
			long retryInterval = redeliveryPolicy.getMaxRedeliveryDelay();
			process(copy, maxRetries, retryInterval);
			
		}
		
		private void process(Exchange exchange, int maxRetries, long retryInterval) {
			
			// retrieve the message
			Message message = exchange.getIn().getBody(Message.class);
			
			boolean processed = false;
			for (int i=1; i <= maxRetries && !processed; i++) {
				try {
					producerTemplate.send("direct:" + queue.getId(), exchange);
					
					// the message was processed, update the status
					message.setStatus(Message.Status.PROCESSED);
					processed = true;
					
				} catch (Exception e) {
					
					// retry or fail
					if (i == maxRetries) {
						log.error("message failed after " + maxRetries + " retries: " + e.getMessage(), e);
						
						// update with the failed status
						message.setStatus(Message.Status.FAILED);
					} else {
						log.warn("message failed, retrying " + i + " of " + maxRetries);
						
						// wait the interval
						try { this.wait(retryInterval); } catch (Exception f) { }
					}
					
				}
			}

		}
		
	}*/

}
