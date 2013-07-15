package org.mokai.action;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

/**
 * An action that adds a prefix to a string if the string doesn't starts with the prefix.
 *
 * @author German Escobar
 */
public class AddPrefixAction implements Action, ExposableConfiguration<AddPrefixAction> {

	@Required
	@Label("Field")
	private String field;

	@Required
	@Label("Prefix")
	private String prefix;

	@Override
	public void execute(Message message) throws Exception {
		validateNotNull(field, "field");
		validateNotNull(prefix, "prefix");

		String fieldValue = message.getProperty(field, String.class);
		if (fieldValue == null) {
			fieldValue = "";
		}

		if (!fieldValue.startsWith(prefix)) {
			fieldValue = prefix + fieldValue;
			message.setProperty(field, fieldValue);
		}
	}

	private void validateNotNull(Object object, String name) throws IllegalArgumentException {
		if (object == null) {
			throw new IllegalArgumentException(name + " not provided");
		}
	}

	@Override
	public AddPrefixAction getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
