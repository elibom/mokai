package org.mokai.acceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mokai.Acceptor;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.List;

/**
 * Acceptor that matches a {@link Message} property to a regular expression.
 * 
 * @author German Escobar
 */
public class RegExpAcceptor implements Acceptor, ExposableConfiguration<RegExpAcceptor> {
	
	@Label("Field")
	@List({"to", "from", "text"})
	private String field;
	
	@Label("Regular Expression")
	private String regexp;
	
	public RegExpAcceptor() {
		
	}
	
	public RegExpAcceptor(String regexp) {
		this.regexp = regexp;
	}

	@Override
	public final boolean accepts(Message message) {
			
		String value = message.getProperty(field, String.class);
			
		if (value != null) {
			Pattern pattern = Pattern.compile(regexp);
			Matcher matcher = pattern.matcher(value);
				
			if (matcher.matches()) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public final RegExpAcceptor getConfiguration() {
		return this;
	}

	public final String getField() {
		return field;
	}

	public final void setField(String field) {
		this.field = field;
	}

	public final String getRegexp() {
		return regexp;
	}

	public final void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	@Override
	public final String toString() {
		return "RegExpAcceptor [field=" + field + ",regexp=" + regexp + "]";
	}

	
}
