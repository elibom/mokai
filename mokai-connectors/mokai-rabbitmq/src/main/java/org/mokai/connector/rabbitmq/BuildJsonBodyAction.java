package org.mokai.connector.rabbitmq;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import org.mokai.Action;
import org.mokai.Message;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
public class BuildJsonBodyAction implements Action {

	@Override
	public void execute(Message message) throws Exception {
		Map<String, Object> properties = message.getProperties();
		JsonObject jsonObject = new JsonObject();
		for (Map.Entry<String, Object> property : properties.entrySet()) {
			String key = property.getKey();
			String value = property.getValue().toString();
			if (!"body".equalsIgnoreCase(key)) {
				jsonObject.add(key, new JsonPrimitive(value));
			}
		}
		message.setProperty("body", jsonObject.getAsString());
	}
}
