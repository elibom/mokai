package org.mokai.impl.camel;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mokai.Action;
import org.mokai.Message;

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
