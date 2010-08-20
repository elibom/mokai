package org.mokai.acceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mokai.spi.Acceptor;
import org.mokai.spi.ExposableConfiguration;
import org.mokai.spi.Message;
import org.mokai.spi.message.SmsMessage;
import org.mokai.spi.type.annotation.Label;
import org.mokai.spi.type.annotation.List;

public class RegExpAcceptor implements Acceptor, ExposableConfiguration<RegExpAcceptor> {
	
	@Label("Field")
	@List({"to", "from", "message"})
	private String field;
	
	@Label("Regular Expression")
	private String regexp;
	
	public RegExpAcceptor() {
		
	}
	
	public RegExpAcceptor(String regexp) {
		this.regexp = regexp;
	}

	@Override
	public boolean accepts(Message message) {
		if (SmsMessage.class.isInstance(message)) {
			SmsMessage smsMessage = (SmsMessage) message;
			
			String to = smsMessage.getTo();
			
			if (to != null) {
				Pattern pattern = Pattern.compile(regexp);
				Matcher matcher = pattern.matcher(to);
				
				if (matcher.matches()) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public RegExpAcceptor getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	@Override
	public String toString() {
		return "RegExpAcceptor [field=" + field + ",regexp=" + regexp + "]";
	}

	
}
