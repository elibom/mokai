package org.mokai.connector.rabbitmq;

import com.rabbitmq.client.AMQP;
import java.io.UnsupportedEncodingException;
import org.mokai.Message;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
public class RabbitMqMessageConverter {

    public Message fromByteArray(AMQP.BasicProperties basicProperties, byte[] body) throws UnsupportedEncodingException {
        Message mokaiMessage = new Message();
        mokaiMessage.setProperty("body", new String(body, "UTF-8"));
        return mokaiMessage;
    }

    private void setAmqpProperties(AMQP.BasicProperties basicProperties, Message mokaiMessage) {
        if (basicProperties != null) {
            mokaiMessage.setProperty("bp_appId", basicProperties.getAppId());
            mokaiMessage.setProperty("bp_classId", basicProperties.getClassId());
            mokaiMessage.setProperty("bp_className", basicProperties.getClassName());
            mokaiMessage.setProperty("bp_clusterId", basicProperties.getClusterId());
            mokaiMessage.setProperty("bp_contentEncoding", basicProperties.getContentEncoding());
            mokaiMessage.setProperty("bp_contentType", basicProperties.getContentType());
            mokaiMessage.setProperty("bp_correlationId", basicProperties.getCorrelationId());
            mokaiMessage.setProperty("bp_deliveryMode", basicProperties.getDeliveryMode());
            mokaiMessage.setProperty("bp_expiration", basicProperties.getExpiration());
            mokaiMessage.setProperty("bp_messageId", basicProperties.getMessageId());
            mokaiMessage.setProperty("bp_priority", basicProperties.getPriority());
            mokaiMessage.setProperty("bp_timeStamp", basicProperties.getTimestamp());
            mokaiMessage.setProperty("bp_type", basicProperties.getType());
        }
    }

    public byte[] fromMessage(Message mokaiMessage) throws UnsupportedEncodingException {
        return mokaiMessage.getProperty("body", String.class).getBytes("UTF-8");
    }
}
