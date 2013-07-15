package org.mokai.acceptor;

import org.mokai.Acceptor;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.List;

/**
 * Acceptor that matches a {@link Message} property to a exact expression.
 *
 * @author German Escobar
 */
public class ExactMatchAcceptor implements Acceptor, ExposableConfiguration<ExactMatchAcceptor> {

	@Label("Field")
	@List({"to", "from", "message"})
	private String field;

	@Label("Expression")
	private String expression;

	public ExactMatchAcceptor() {

	}

	public ExactMatchAcceptor(String field, String expression) {
		this.field = field;
		this.expression = expression;
	}

	@Override
	public final boolean accepts(Message message) {
		String value = message.getProperty(field, String.class);

		if (value != null && value.equals(expression)) {
			return true;
		}

		return false;
	}

	@Override
	public final ExactMatchAcceptor getConfiguration() {
		return this;
	}

	public final String getField() {
		return field;
	}

	public final void setField(String field) {
		this.field = field;
	}

	public final String getExpression() {
		return expression;
	}

	public final void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public final String toString() {
		if (customToString() == null) {
			return "ExactMatchAcceptor [field=" + field + ",expression=" + expression + "]";
		}

		return customToString();
	}

	protected String customToString() {
		return null;
	}
}
