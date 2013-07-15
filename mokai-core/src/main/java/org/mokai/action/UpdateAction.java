package org.mokai.action;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

/**
 * An action that adds or updates a field in a message. If the field doesn't exists, it gets added, otherwise, it is replaced
 * with the specified value.
 *
 * @author German Escobar
 */
public class UpdateAction implements Action, ExposableConfiguration<UpdateAction> {

	private String field;

	private String value;

	@Override
	public void execute(Message message) throws Exception {
		message.setProperty(field, value);
	}

	@Override
	public UpdateAction getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
