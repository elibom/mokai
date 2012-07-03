package org.mokai.impl.camel;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.mokai.Action;
import org.mokai.Execution;
import org.mokai.Message;

/**
 * Camel Processor implementation used by {@link AbstractCamelConnectorService} to execute actions (pre-processing, 
 * post-processing and post-receiving). 
 * 
 * @author German Escobar
 */
public class ActionsProcessor implements Processor {
	
	/**
	 * The list of actions to execute.
	 */
	private List<Action> actions;
	
	/**
	 * The endpoint to which we need to send the message after the actions have executed.
	 */
	private String endpoint;
	
	public ActionsProcessor(List<Action> actions, String endpoint) {
		this.actions = actions;
		this.endpoint = endpoint;
	}

	@Override
	public final void process(Exchange exchange) throws Exception {
		
		Message message = exchange.getIn().getBody(Message.class);
		ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();
		
		// execute the actions recursively starting from the first one (index 0)
		ActionsExecutor executor = new ActionsExecutor(producerTemplate, 0);
		executor.route(message);
		
		// stop the message flow
		exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
		
	}
	
	/**
	 * This class actually executes the actions. It implements {@link Execution} because I also use it to pass it to 
	 * the actions (recursively). For mor information see issue #34.
	 * 
	 * @author German Escobar
	 */
	class ActionsExecutor implements Execution {
		
		/**
		 * Used to route the messages to the specified endpoint.
		 */
		private ProducerTemplate producerTemplate;
		
		private int initialIndex;
		
		/**
		 * The index of the action that is being executed.
		 */
		private int index;
		
		/**
		 * Tells if the action stopped the execution.
		 */
		private boolean stopped;
		
		public ActionsExecutor(ProducerTemplate producerTemplate, int index) {
			this.producerTemplate = producerTemplate;
			this.initialIndex = index;
		}
		
		@Override
		public void stop() {
			stopped = true;
		}
		
		
		@Override
		public void route(Message message) throws Exception {
			index = initialIndex; // reset index just in case it is called a second time
			doRoute(message);
		}
		
		/**
		 * Helper method. Used to execute the actions recursively.
		 * 
		 * @param message
		 * 
		 * @throws Exception
		 */
		private void doRoute(final Message message) throws Exception {
			
			// if last action (or no actions), route the message to the endpoint
			if (index == actions.size()) {
				
				producerTemplate.send(endpoint, new Processor() {
					public void process(Exchange outExchange) {
						outExchange.getIn().setBody(message); 
					}
			    });
				
				return;
				
			}
			
			// retrieve the action
			Action action = actions.get(index);
			
			// inject the Execution interface implementation (ie. an instance of this class)
			ActionsExecutor executor = new ActionsExecutor(producerTemplate, index + 1);
			ResourceInjector.inject(action, executor);
			
			// execute the message, notice that the execution could be stopped or new messages routed
			action.execute(message);
			
			// if the action stopped the execution ...
			if (executor.isStopped()) {
				return;
			}
			
			// recursive call to continue to execution
			index++;
			doRoute(message);
		}
		
		/**
		 * Tells if the execution was stopped by the action.
		 * 
		 * @return true if the execution was stopped, false otherwise.
		 */
		public boolean isStopped() {
			return stopped;
		}
		
	}

}
