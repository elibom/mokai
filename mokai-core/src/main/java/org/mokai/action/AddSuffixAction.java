package org.mokai.action;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

/**
 * An action that adds a suffix to a string if the string doesn't ends with the suffix.
 *
 * @author German Escobar
 */
public class AddSuffixAction implements Action, ExposableConfiguration<AddSuffixAction> {

	@Required
	@Label("Field")
	private String field;

	@Required
	@Label("Suffix")
	private String suffix;

	@Override
	public void execute(Message message) throws Exception {
		validateNotNull(field, "field");
		validateNotNull(suffix, "suffix");

		String fieldValue = message.getProperty(field, String.class);
		if (fieldValue == null) {
			fieldValue = "";
		}

		if (!fieldValue.endsWith(suffix)) {
			fieldValue = fieldValue + suffix;
			message.setProperty(field, fieldValue);
		}
	}

	private void validateNotNull(Object object, String name) throws IllegalArgumentException {
		if (object == null) {
			throw new IllegalArgumentException(name + " not provided");
		}
	}

	@Override
	public AddSuffixAction getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

}
