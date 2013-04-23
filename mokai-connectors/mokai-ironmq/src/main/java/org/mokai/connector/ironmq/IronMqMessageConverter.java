package org.mokai.connector.ironmq;

import org.mokai.Message;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
public class IronMqMessageConverter {

    public org.mokai.Message convert(io.iron.ironmq.Message ironMqMessage) {
        org.mokai.Message mokaiMessage = new Message();
        mokaiMessage.setProperty("id", ironMqMessage.getId());
        mokaiMessage.setProperty("body", ironMqMessage.getBody());
        mokaiMessage.setProperty("delay", ironMqMessage.getDelay());
        mokaiMessage.setProperty("expiresIn", ironMqMessage.getExpiresIn());
        mokaiMessage.setProperty("timeout", ironMqMessage.getTimeout());
        return mokaiMessage;
    }
}
