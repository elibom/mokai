package org.mokai.action;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

/**
 * An action that fills a string with spaces until the string is of the specified length.
 * 
 * @author German Escobar
 */
public class AddSpacesAction implements Action, ExposableConfiguration<AddSpacesAction> {
	
	private String field;
	
	private int length;

	@Override
	public void execute(Message message) throws Exception {
		
		validateNotNull(field, "field");
		
		// validate length is positive
		if (length < 1) {
			throw new IllegalArgumentException("lenght must be positive");
		}
		
		String fieldValue = message.getProperty(field, String.class);
		if (fieldValue == null) {
			fieldValue = "";
		}
		
		if (fieldValue.length() < length) {
			int missing = length - fieldValue.length();
			
			for (int i=0; i < missing; i++) {
				fieldValue += " ";
			}
			
			message.setProperty(field, fieldValue);
		}
		
	}
	
	private void validateNotNull(Object object, String name) throws IllegalArgumentException {
		if (object == null) {
			throw new IllegalArgumentException(name + " not provided");
		}
	}

	@Override
	public AddSpacesAction getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
