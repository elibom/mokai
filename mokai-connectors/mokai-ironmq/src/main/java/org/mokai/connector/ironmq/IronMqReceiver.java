package org.mokai.connector.ironmq;

import io.iron.ironmq.Client;
import io.iron.ironmq.EmptyQueueException;
import io.iron.ironmq.Message;
import io.iron.ironmq.Queue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mokai.Connector;
import org.mokai.ConnectorContext;
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
 * @author German Escobar
 */
@Name("IronMQ Receiver")
@Description("Receives messages from an IronMQ queue")
public class IronMqReceiver implements Connector, Serviceable, ExposableConfiguration<IronMqReceiverConfig> {

	private final Logger log = LoggerFactory.getLogger(IronMqReceiver.class);

	@Resource
	private ConnectorContext context;

	@Resource
	private MessageProducer messageProducer;

	private Queue queue;

	private IronMqReceiverConfig configuration;

	private List<Thread> consumerThreads;

	private volatile boolean running = false;

	public IronMqReceiver() {
		this(new IronMqReceiverConfig());
	}

	public IronMqReceiver(IronMqReceiverConfig configuration) {
		this.configuration = configuration;
	}

	@Override
	public IronMqReceiverConfig getConfiguration() {
		return configuration;
	}

	@Override
	public void doStart() throws Exception {
		if (running) {
			log.warn(getLogHead() + "IronMqReceiver is already running");
			return;
		}

		log.info(getLogHead() + "starting IronMqReceiver ... ");
		Client ironMqClient = new Client(configuration.getProjectId(), configuration.getToken());
		queue = ironMqClient.queue(configuration.getQueueName());

		consumerThreads = new ArrayList<Thread>(configuration.getNumConsumerThreads());
		for (int i = 0; i < configuration.getNumConsumerThreads(); i++) {
			log.info(getLogHead() + "starting consumer thread " + i);
			Thread consumerThread = new Thread(new IronMqMessageConsumer());
			consumerThread.start();
			consumerThreads.add(consumerThread);
		}
		running = true;
	}

	@Override
	public void doStop() throws Exception {
		if (!running) {
			log.warn(getLogHead() + "IronMqReceiver is already stopped");
			return;
		}
		log.info(getLogHead() + "stopping IronMQReceiver");
		for (int i = 0; i < consumerThreads.size(); i++) {
			log.debug(getLogHead() + "stopping thread " + i);
			consumerThreads.get(i).join();
		}
		running = false;
	}

	private class IronMqMessageConsumer implements Runnable {

		private Logger log = LoggerFactory.getLogger(IronMqMessageConsumer.class);

		@Override
		public void run() {
			while (running) {
				try {
					Message[] messages = queue.get(100).getMessages();

					if (messages.length > 0) {
						for (Message message : messages) {
							log.info(getLogHead() + "IronMQ message received: " + message.toString());
							messageProducer.produce(convert(message));

							queue.deleteMessage(message);
						}
					} else {
						throw new EmptyQueueException();
					}
				} catch (EmptyQueueException eqe) {
					log.trace(getLogHead() + "IronMQ queue is empty");
					try {
						Thread.sleep(configuration.getFetchInterval());
					} catch (InterruptedException ex) {}
				} catch (IOException ioe) {
					log.error("Error while get message from ironMq", ioe);
				}
			}
		}

		public org.mokai.Message convert(Message ironMessage) {
			org.mokai.Message message = new org.mokai.Message();
			message.setProperty("id", ironMessage.getId());
			message.setProperty("body", ironMessage.getBody());
			message.setProperty("delay", ironMessage.getDelay());
			message.setProperty("expiresIn", ironMessage.getExpiresIn());
			message.setProperty("timeout", ironMessage.getTimeout());
			return message;
		}
	}

	/**
	 * Helper method that returns the header that should be appended to all log messages.
	 *
	 * @return the log header.
	 */
	private String getLogHead() {
		return "[connector=" + context.getId() + "] ";
	}
}
