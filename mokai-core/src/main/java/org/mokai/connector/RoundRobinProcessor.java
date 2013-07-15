package org.mokai.connector;

import java.util.ArrayList;
import java.util.List;

import org.mokai.Configurable;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.Monitorable;
import org.mokai.Processor;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.ui.annotation.ConnectorsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("Round Robin Processor")
@Description("Sends messages in a round-robin fashion using a list of Processors")
public class RoundRobinProcessor implements Processor, ExposableConfiguration<RoundRobinProcessor>,
		Configurable, Serviceable, Monitorable {

	private Logger log = LoggerFactory.getLogger(RoundRobinProcessor.class);

	@ConnectorsList
	private List<Processor> processors = new ArrayList<Processor>();

	/**
	 * The index of the connector that should process the next message
	 */
	private int index = 0;

	@Override
	public synchronized void process(Message message) throws Exception {
		int retry = 0;
		process(message, retry);

		incrementIndex();
	}

	/**
	 * Helper method that recursively tries to send a message through one of the processors.
	 *
	 * @param message the message to be processed
	 * @param retry the number of attempts that we have made.
	 * @throws Exception if the message couldn't be processed by any of the processors.
	 */
	private void process(Message message, int retry) throws Exception {
		// if we have tested all the processors and none of them worked, throw an exception
		if (retry == processors.size()) {
			throw new Exception("Message could not be processed: all of the processors failed.");
		}

		try {
			Processor processor = processors.get(index);
			processor.process(message);
		} catch (Exception e) {
			log.error("Processor " + index + " threw exception, trying with the next", e);

			// try with the next processor
			incrementIndex();
			process(message, retry + 1);
		}
	}

	/**
	 * Helper method to increment the index (i.e. select the index of the processor that should process the next
	 * message). If there are no more processors, it turns around and starts with the first.
	 */
	private void incrementIndex() {
		index++;

		if (index == processors.size()) {
			index = 0;
		}
	}

	@Override
	public boolean supports(Message message) {
		if (processors == null || processors.isEmpty()) {
			return false;
		}

		for (Processor processor : processors) {
			if (!processor.supports(message)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void configure() throws Exception {
		for (Processor processor : processors) {
			if (Configurable.class.isInstance(processor)) {
				Configurable configurable = (Configurable) processor;
				configurable.configure();
			}
		}
	}

	@Override
	public void destroy() throws Exception {
		for (Processor processor : processors) {
			if (Configurable.class.isInstance(processor)) {
				Configurable configurable = (Configurable) processor;
				configurable.destroy();
			}
		}
	}

	@Override
	public void doStart() throws Exception {
		for (Processor processor : processors) {
			if (Serviceable.class.isInstance(processor)) {
				Serviceable serviceable = (Serviceable) processor;
				serviceable.doStart();
			}
		}
	}

	@Override
	public void doStop() throws Exception {
		for (Processor processor : processors) {
			if (Serviceable.class.isInstance(processor)) {
				Serviceable serviceable = (Serviceable) processor;
				serviceable.doStop();
			}
		}
	}

	@Override
	public Status getStatus() {
		Status status = Status.OK;

		boolean atLeastOneHasStatus = false;
		for (Processor processor : processors) {
			if (Monitorable.class.isInstance(processor)) {
				atLeastOneHasStatus = true;

				Monitorable monitorable = (Monitorable) processor;
				if (monitorable.getStatus() == Status.FAILED) {
					status = Status.FAILED;
				}
			}
		}

		if (!atLeastOneHasStatus) {
			status = Status.UNKNOWN;
		}

		return status;
	}

	public List<Processor> getProcessors() {
		return processors;
	}

	public void setProcessors(List<Processor> processors) {
		this.processors = processors;
	}

	public RoundRobinProcessor addProcessor(Processor processor) {
		this.processors.add(processor);

		return this;
	}

	@Override
	public RoundRobinProcessor getConfiguration() {
		return this;
	}

}
