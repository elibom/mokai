package org.mokai.connector.ironmq;

import io.iron.ironmq.Client;
import io.iron.ironmq.EmptyQueueException;
import io.iron.ironmq.Message;
import io.iron.ironmq.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.mokai.Connector;
import org.mokai.ExposableConfiguration;
import org.mokai.MessageProducer;
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
@Name("IronMq")
@Description("Receives messages from ironMq")
public class IronMqMessagesReceiverConnector implements Connector, Serviceable, ExposableConfiguration<IronMqConfiguration> {

    private final Logger log = LoggerFactory.getLogger(IronMqMessagesReceiverConnector.class);

    @Resource
    private MessageProducer messageProducer;

    private Queue ironMqQueue;

    private IronMqConfiguration configuration;

    private List<Thread> threadList;

    private volatile boolean running = false;

    public IronMqMessagesReceiverConnector() {
        this(new IronMqConfiguration());
    }

    public IronMqMessagesReceiverConnector(IronMqConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public IronMqConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void doStart() throws Exception {
        if (running) {
            log.warn("ironMqMessageConsumerThread is already running");
            return;
        }

        log.info("Starting IronMqMessagesReceiverConnector");
        Client ironMqClient = new Client(configuration.getIronMqProjectId(), configuration.getIronMqToken());
        ironMqQueue = ironMqClient.queue(configuration.getIronMqQueueName());

        threadList = new ArrayList<Thread>(configuration.getNumThreads());
        for (int i = 0; i < configuration.getNumThreads(); i++) {
            log.info("Starting ironMqMessageConsumerThread " + i);
            Thread ironMqMessageConsumerThread = new Thread(new IronMqMessageConsumer());
            ironMqMessageConsumerThread.start();
            threadList.add(ironMqMessageConsumerThread);
        }
        running = true;
    }

    @Override
    public void doStop() throws Exception {
        if (!running) {
            log.warn("ironMqMessageConsumerThread is not running");
            return;
        }
        log.info("Stoping ironMqMessageConsumerThread");
        for (int i = 0; i < threadList.size(); i++) {
            log.info("Stoping ironMqMessageConsumerThread " + i);
            threadList.get(i).join();
        }
        running = false;
    }

    private AtomicInteger count = new AtomicInteger(0);

    private class IronMqMessageConsumer implements Runnable {

        private Logger log = LoggerFactory.getLogger(IronMqMessageConsumer.class);

        @Override
        public void run() {
            IronMqMessageConverter messageConverter = new IronMqMessageConverter();
            while (running) {
                try {
                    long sget = System.currentTimeMillis();
                    Message[] messages = ironMqQueue.get(100).getMessages();
                    log.debug("ironMq multiget took {} ms for {} messages", new Object[]{System.currentTimeMillis() - sget, messages.length});
                    if (messages.length > 0) {
                        for (Message ironMqMessage : messages) {
                            log.info("New IronMq message{}", ironMqMessage.toString());
                            messageProducer.produce(messageConverter.convert(ironMqMessage));
                            long sdel = System.currentTimeMillis();
                            ironMqQueue.deleteMessage(ironMqMessage);
                            log.debug("ironMq delete took {} ms", System.currentTimeMillis() - sdel);
                            log.debug("count {} ", count.incrementAndGet());
                        }
                    } else {
                        throw new EmptyQueueException();
                    }
                } catch (EmptyQueueException eqe) {
                    log.info("Message queue is empty");
                    try {
                        Thread.sleep(configuration.getFetchInterval());
                    } catch (InterruptedException ex) {
                    }
                } catch (IOException ioe) {
                    log.error("Error while get message from ironMq", ioe);
                }
            }
        }
    }
}
