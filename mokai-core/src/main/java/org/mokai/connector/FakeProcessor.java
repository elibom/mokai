package org.mokai.connector;

import java.util.Map;
import java.util.Map.Entry;

import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.Processor;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("Fake Processor")
@Description("Fakes a processor that only logs messages")
public class FakeProcessor implements Processor, ExposableConfiguration<FakeProcessor> {

	private Logger log = LoggerFactory.getLogger(FakeProcessor.class);

	private long delay;

	@Override
	public void process(Message message) throws Exception {
		try { Thread.sleep(delay); } catch (InterruptedException e) {}

		String strProperties = "direction=" + message.getDirection();
		strProperties += ", destination=" + message.getDestination();
		strProperties += ", source=" + message.getSource();

		Map<String,Object> properties = message.getProperties();
		for (Entry<String,Object> entry : properties.entrySet()) {
			strProperties += ", " + entry.getKey() + "=" + entry.getValue();
		}

		log.info("[" + strProperties + "]");
	}

	@Override
	public boolean supports(Message message) {
		return true;
	}

	@Override
	public FakeProcessor getConfiguration() {
		return this;
	}

}
