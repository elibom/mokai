package org.mokai.impl.camel;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mokai.Action;
import org.mokai.Message;

/**
 * Camel Processor implementation used by {@link CamelProcessorService} 
 * and {@link CamelReceiverService} to handle framework actions (pre-processing, 
 * post-processing and post-receiving). 
 * 
 * @author German Escobar
 */
public class ActionsProcessor implements Processor {
	
	private List<Action> actions;
	
	public ActionsProcessor(List<Action> actions) {
		this.actions = actions;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message message = exchange.getIn().getBody(Message.class);

		for (Action action : actions) {
			action.execute(message);
		}
	}

}
