package org.mokai.action;

import java.util.HashMap;
import java.util.Map;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that replaces strings on a field. It uses the method String.replaceAll so you can use regular
 * expressions.
 * 
 * @author German Escobar
 */
public class ReplaceAction implements Action, ExposableConfiguration<ReplaceAction> {
	
	private Logger log = LoggerFactory.getLogger(ReplaceAction.class);
	
	private String field;
	
	private Map<String,String> replace = new HashMap<String,String>();

	@Override
	public void execute(Message message) throws Exception {
		
		// check that field is not null
		if (field == null) {
			throw new IllegalArgumentException("field not provided");
		}
		
		String fieldValue = message.getProperty(field, String.class);
		if (fieldValue != null && !"".equals(fieldValue)) {
			
			for (Map.Entry<String, String> entry : replace.entrySet()) {
				String findString = entry.getKey();
				String replaceString = entry.getValue();
				
				log.trace("replacing '" + findString + "' with '" + replaceString + "' in field '" + field + "': '" + fieldValue + "'");
				
				fieldValue = fieldValue.replaceAll(findString, replaceString);
			}
			
			log.trace("new field value: " + fieldValue);
			
			message.setProperty(field, fieldValue);
		}
	}

	@Override
	public ReplaceAction getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Map<String, String> getReplace() {
		return replace;
	}

	public void setReplace(Map<String, String> replace) {
		this.replace = replace;
	}

}
