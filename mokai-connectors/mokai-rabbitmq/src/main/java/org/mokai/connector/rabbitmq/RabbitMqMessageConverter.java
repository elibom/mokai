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

	public byte[] fromMessage(Message mokaiMessage) throws UnsupportedEncodingException {
		return mokaiMessage.getProperty("body", String.class).getBytes("UTF-8");
	}
}
