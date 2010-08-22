package org.mokai.acceptor;

import org.mokai.Acceptor;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.message.SmsMessage;
import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.List;

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
	public boolean accepts(Message message) {
		
		if (SmsMessage.class.isInstance(message)) {
			SmsMessage smsMessage = (SmsMessage) message;
			
			String to = smsMessage.getTo();
			
			if (to != null && to.equals(expression)) {
				return true;
			}
			
		}
		
		return false;
	}

	@Override
	public ExactMatchAcceptor getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return "ExactMatchAcceptor [field=" + field + ",expression=" + expression + "]";
	}
}
