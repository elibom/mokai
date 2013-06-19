package org.mokai.action;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 * @author German Escobar
 */
public class JsonParserAction implements Action, ExposableConfiguration<JsonParserAction> {

	private Logger log = LoggerFactory.getLogger(JsonParserAction.class);

	private String field;

	@Override
	public void execute(Message message) throws Exception {
		String body = (String) message.getProperty(field);

		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String,Object> map = mapper.readValue(body, new TypeReference<Map<String,Object>>() {});

			for (Map.Entry<String,Object> entry : map.entrySet()) {
				message.setProperty(entry.getKey().toString(), entry.getValue());
			}
		} catch (Exception e) {
			log.warn("Exception parsing JSON '" + body + "': " + e.getMessage(), e);
		}
	}

	@Override
	public JsonParserAction getConfiguration() {
		return this;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}
