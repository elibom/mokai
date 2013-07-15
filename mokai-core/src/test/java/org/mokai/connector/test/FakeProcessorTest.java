package org.mokai.connector.test;

import org.mokai.Message;
import org.mokai.connector.FakeProcessor;
import org.testng.annotations.Test;

public class FakeProcessorTest {

	@Test
	public void shouldLogMessage() throws Exception {
		FakeProcessor processor = new FakeProcessor();

		Message message = new Message();
		processor.process(message);
	}
}
