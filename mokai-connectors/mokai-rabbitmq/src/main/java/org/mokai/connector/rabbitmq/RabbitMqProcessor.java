package org.mokai.connector.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MonitorStatusBuilder;
import org.mokai.Monitorable;
import org.mokai.Processor;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
@Name("RabbitMqProcessor")
@Description("Sends messages through RabbitMq")
public class RabbitMqProcessor implements Processor, Serviceable, Monitorable, ExposableConfiguration<RabbitMqConfiguration> {

    private final Logger log = LoggerFactory.getLogger(RabbitMqProcessor.class);

    private Connection connection;

    private Channel channel;

    private RabbitMqConfiguration configuration;

    private Status status = MonitorStatusBuilder.unknown();

    private boolean started;

    public RabbitMqProcessor() {
        this(new RabbitMqConfiguration());
    }

    public RabbitMqProcessor(RabbitMqConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void doStart() throws Exception {
        log.info("Starting RabbitMqProcessor");
        started = true;
        new RabbitMqProcessor.ConnectionRunnable(1, 0).run();
        if (status.equals(Status.FAILED)) {
            new Thread(new RabbitMqProcessor.ConnectionRunnable(Integer.MAX_VALUE, configuration.getReconnectDelay())).start();
        }
    }

    @Override
    public void doStop() throws Exception {
        log.info("Stoping RabbitMqProcessor");
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
        connection.addShutdownListener(new ShutdownListener() {
            @Override
            public void shutdownCompleted(ShutdownSignalException cause) {
                log.warn("RabbitMQ connection lost", cause);
                status = MonitorStatusBuilder.failed("RabbitMQ connection lost", cause);
            }
        });
        channel = connection.createChannel();
        channel.exchangeDeclare(configuration.getExchange(), "direct", true);
        channel.queueDeclare(configuration.getQueueName(), true, false, false, null);
        channel.queueBind(configuration.getQueueName(), configuration.getExchange(), configuration.getRoutingKey());
        status = MonitorStatusBuilder.ok();
    }

    private void disconnect() {
        try {
            connection.close();
        } catch (Exception ex) {
            log.warn("Error while closing rabbitMq connection", ex);
        }
        status = MonitorStatusBuilder.unknown();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public RabbitMqConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void process(Message message) throws Exception {
        if (!status.equals(Status.OK)) {
            try {
                log.info("trying to reconnect to RabbitMQ");
                connect();
            } catch (Exception ex) {
                log.error("Error while reconnect to RabbitMQ", ex);
                throw new RuntimeException(ex);
            }
        }
        try {
            channel.basicPublish(configuration.getExchange(), configuration.getRoutingKey(), true, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getProperty("body", String.class).getBytes("UTF-8"));
        } catch (Exception ioe) {
            disconnect();
            log.error("Error while publishing message to RabbitMQ", ioe);
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public boolean supports(Message message) {
        return true;
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
                    log.info("Attempt #{} - Trying to connect to rabbitMq: {}", new Object[]{(++attempt), configuration});
                    connect();
                    status = MonitorStatusBuilder.ok();
                    log.info("Success connected to rabbitMq");
                } catch (Exception e) {
                    log.info("Error while trying to reconnect to rabbitMq", e);
                    status = MonitorStatusBuilder.failed(e.getMessage());
                    try {
                        connection.close();
                    } catch (Exception ex) {
                        log.warn("Error while closing rabbitMq connection", ex);
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
                log.error("Error while connect to rabbitMq", e);
            } else {
                log.error("Error while connect to rabbitMq: " + e.getMessage());
            }
        }
    }
}
