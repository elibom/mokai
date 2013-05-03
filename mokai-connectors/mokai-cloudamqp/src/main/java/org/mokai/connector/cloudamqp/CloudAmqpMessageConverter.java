package org.mokai.connector.cloudamqp;

import com.rabbitmq.client.AMQP;
import org.mokai.Message;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
public class CloudAmqpMessageConverter {

    public Message convert(AMQP.BasicProperties basicProperties, byte[] body) {
        Message mokaiMessage = new Message();
        mokaiMessage.setProperty("body", new String(body));
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
        return mokaiMessage;
    }
}
