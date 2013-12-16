package org.mokai.connector.cloudamqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import org.mokai.Connector;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
@Name("CloudAmqp")
@Description("Receives messages from cloudAmqp")
public class CloudAmqpMessagesReceiverConnector implements Connector, Serviceable, Monitorable, ExposableConfiguration<CloudAmqpConfiguration> {

    private final Logger log = LoggerFactory.getLogger(CloudAmqpMessagesReceiverConnector.class);

    @Resource
    private MessageProducer messageProducer;

    private Connection connection;

    private Channel channel;

    private CloudAmqpConfiguration configuration;

    private Status status = MonitorStatusBuilder.unknown();

    private boolean started;

    public CloudAmqpMessagesReceiverConnector() {
        this(new CloudAmqpConfiguration());
    }

    public CloudAmqpMessagesReceiverConnector(CloudAmqpConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public CloudAmqpConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void doStart() throws Exception {
        log.info("Starting cloudAmqpMessagesReceiverConnector");
        started = true;
        new ConnectionRunnable(1, 0).run();
        if (status.equals(Status.FAILED)) {
            new Thread(new ConnectionRunnable(Integer.MAX_VALUE, configuration.getReconnectDelay())).start();
        }
    }

    @Override
    public void doStop() throws Exception {
        log.info("Stoping cloudAmqpMessagesReceiverConnector");
        started = false;
        disconnect();
    }

    private void connect() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setRequestedHeartbeat(configuration.getHeartBeat());
        connectionFactory.setUsername(configuration.getUsername());
        connectionFactory.setPassword(configuration.getPassword());
        connectionFactory.setHost(configuration.getHost());
        connectionFactory.setPort(configuration.getPort());
        connectionFactory.setVirtualHost(configuration.getVirtualHost());
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(20);
        channel.exchangeDeclare(configuration.getExchange(), "direct", true);
        channel.queueDeclare(configuration.getQueueName(), true, false, false, null);
        channel.queueBind(configuration.getQueueName(), configuration.getExchange(), configuration.getRoutingKey());
        channel.basicConsume(configuration.getQueueName(), false, "CloudAmqpMessageConsumer", new CloudAmqpMessageConsumer(channel));
    }

    private void disconnect() {
        try {
            connection.close();
        } catch (Exception ex) {
            log.warn("Error while closing amqp connection", ex);
        }
        status = MonitorStatusBuilder.unknown();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    private class CloudAmqpMessageConsumer extends DefaultConsumer {

        private CloudAmqpMessageConverter messageConverter;

        public CloudAmqpMessageConsumer(Channel channel) {
            super(channel);
            messageConverter = new CloudAmqpMessageConverter();
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            Message message = messageConverter.convert(properties, body);
            log.info("processing new Message: {}", message.getProperty("body"));
            messageProducer.produce(message);
            long deliveryTag = envelope.getDeliveryTag();
            channel.basicAck(deliveryTag, false);
            log.info("message acknowledge {}", deliveryTag);
        }

        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            log.error("Rabbit amqp disconnected trying to re-connect");
            status = MonitorStatusBuilder.failed("connection lost: " + sig.getMessage());
            new Thread(new ConnectionRunnable(Integer.MAX_VALUE, configuration.getReconnectDelay())).start();
        }
    }

    private class ConnectionRunnable implements Runnable {

        private int maxRetries;

        private long initialDelay;

        public ConnectionRunnable(int maxRetries, long initialDelay) {
            this.maxRetries = maxRetries;
            this.initialDelay = initialDelay;
        }

        @Override
        public void run() {
            int attempt = 0;
            try {
                Thread.sleep(initialDelay);
            } catch (InterruptedException ie) {
            }
            while (started && !status.equals(Status.OK) && attempt < maxRetries) {
                try {
                    log.info("Attempt #{} - Trying to connect to rabbit amqp: {}", new Object[]{(++attempt), configuration});
                    connect();
                    status = MonitorStatusBuilder.ok();
                    log.info("Success connected to cloudamqp");
                } catch (Exception e) {
                    log.info("Error while trying to reconnect to rabbit amqp", e);
                    status = MonitorStatusBuilder.failed(e.getMessage());
                    try {
                        connection.close();
                    } catch (Exception ex) {
                        log.warn("Error while closing amqp connection", ex);
                    }
                    logException(e, attempt == 1);
                    try {
                        Thread.sleep(configuration.getReconnectDelay());
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        private void logException(Exception e, boolean firstTime) {
            if (firstTime) {
                log.error("Error while connect to cloudAmqp", e);
            } else {
                log.error("Error while connect to cloudAmqp: " + e.getMessage());
            }
        }
    }
}
