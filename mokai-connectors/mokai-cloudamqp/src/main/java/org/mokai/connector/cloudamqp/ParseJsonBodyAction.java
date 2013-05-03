package org.mokai.connector.cloudamqp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map.Entry;
import java.util.Set;
import org.mokai.Action;
import org.mokai.Message;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
public class ParseJsonBodyAction implements Action {

    @Override
    public void execute(Message message) throws Exception {
        String body = (String) message.getProperty("body");
        JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
        Set<Entry<String, JsonElement>> jsonEntrySet = jsonObject.entrySet();
        for (Entry<String, JsonElement> jsonEntry : jsonEntrySet) {
            message.setProperty(jsonEntry.getKey(), jsonEntry.getValue().getAsString());
        }
    }
}
