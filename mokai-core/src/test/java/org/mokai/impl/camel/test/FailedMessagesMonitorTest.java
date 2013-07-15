package org.mokai.impl.camel.test;

import org.testng.annotations.Test;

public class FailedMessagesMonitorTest {

	@Test
	public void testFailedMessagesMonitor() throws Exception {
		/*MessageStore messageStore = Mockito.mock(MessageStore.class);
		CamelRoutingEngine routingEngine = new CamelRoutingEngine();

		FailedMessagesMonitor fmm = new FailedMessagesMonitor();
		fmm.setDelay(0);
		fmm.setInterval(200);
		fmm.setRoutingContext(routingEngine);

		// start monitor
		fmm.start();
		Assert.assertEquals(State.STARTED, fmm.getState());

		Thread.sleep(1000);

		fmm.stop();
		Assert.assertEquals(State.STOPPED, fmm.getState());

		Mockito.verify(routingEngine, Mockito.atLeastOnce()).retryFailedMessages();*/

	}

}
