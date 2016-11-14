package org.mokai.connector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import org.mokai.Message;
import org.mokai.Processor;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class FakeFailProcessor implements Processor {

    private final int FAIL_AFTER_TIMES = 3;

    private final Map<String, Integer> countMap = new HashMap<String, Integer>();

    @Override
    public void process(Message message) throws Exception {

        String body = (String) message.getProperty("body");
        JsonObject jsonMessage = new JsonParser().parse(body).getAsJsonObject();
        String messageId = jsonMessage.get("messageId").getAsString();
        throw new RuntimeException("Failing message: " + messageId);
//        if (!countMap.containsKey(messageId)) {
//            countMap.put(messageId, 1);
//        } else {
//            Integer count = countMap.get(messageId);
//            if (count >= FAIL_AFTER_TIMES) {
//                countMap.remove(messageId);
//                throw new RuntimeException("Failing message: " + messageId);
//            }
//            countMap.put(messageId, count + 1);
//        }

    }


    @Override
    public boolean supports(Message message) {
        return true;
    }

}
