package org.mokai.action;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

/**
 * An action that removes a field from a message (if it exists).
 * 
 * @author German Escobar
 */
public class RemoveAction implements Action, ExposableConfiguration<RemoveAction> {
	
	private String field;

	@Override
	public void execute(Message message) throws Exception {
		
		message.removeProperty(field);
		
	}

	@Override
	public RemoveAction getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
