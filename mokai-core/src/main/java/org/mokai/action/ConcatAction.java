package org.mokai.action;

import java.util.ArrayList;
import java.util.List;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

/**
 * An action that concats multiple fields in a new field.
 *
 * @author German Escobar
 */
public class ConcatAction implements Action, ExposableConfiguration<ConcatAction> {

	private List<String> fields = new ArrayList<String>();

	private String separator = "-";

	private String destField;

	@Override
	public void execute(Message message) throws Exception {
		// check that field is not null
		if (destField == null) {
			throw new IllegalArgumentException("destination field not provided");
		}

		StringBuffer buffer = new StringBuffer();
		for (String field : fields) {
			String value = message.getProperty(field) + "";
			if (!"".equals(value)) {
				buffer.append(value).append(separator);
			}
		}

		String concat = buffer.toString();
		if (!"".equals(concat)) {
			concat = concat.substring(0, concat.length()-1);
		}

		message.setProperty(destField, concat);
	}

	@Override
	public ConcatAction getConfiguration() {
		return this;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getDestField() {
		return destField;
	}

	public void setDestField(String destField) {
		this.destField = destField;
	}

}
