package org.mokai.connector.ironmq;

import io.iron.ironmq.Client;
import io.iron.ironmq.Queue;

import org.mokai.ConnectorContext;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;

/**
 * 
 * @author German Escobar
 */
@Name("IronMQ Processor")
@Description("Sends messages to an IronMQ queue")
public class IronMqProcessor implements Processor, ExposableConfiguration<IronMqProcessorConfig> {
	
	@Resource
	private ConnectorContext context;
	
	private IronMqProcessorConfig configuration;
	
	public IronMqProcessor() {
		this(new IronMqProcessorConfig());
	}
	
	public IronMqProcessor(IronMqProcessorConfig configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public boolean supports(Message message) {
		if (message.getProperty(configuration.getField()) != null) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public void process(Message message) throws Exception {
		Client client = new Client(configuration.getProjectId(), configuration.getToken());
		Queue queue = client.queue(configuration.getQueueName());
		
		queue.push(message.getProperty("body", String.class));
	}

	@Override
	public IronMqProcessorConfig getConfiguration() {
		return configuration;
	}

}
