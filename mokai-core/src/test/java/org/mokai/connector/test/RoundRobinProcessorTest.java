package org.mokai.connector.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.mokai.Configurable;
import org.mokai.Message;
import org.mokai.Monitorable;
import org.mokai.Processor;
import org.mokai.Serviceable;
import org.mokai.Monitorable.Status;
import org.mokai.connector.RoundRobinProcessor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RoundRobinProcessorTest {

	@Test
	public void shouldProcessInRoundRobinFashion() throws Exception {
		
		Processor p1 = mock(Processor.class);
		Processor p2 = mock(Processor.class);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		processor.process(new Message());
		verify(p1).process(any(Message.class));
		verify(p2, never()).process(any(Message.class));
		
		processor.process(new Message());
		verify(p1).process(any(Message.class));
		verify(p2).process(any(Message.class));
		
	}
	
	@Test
	public void shouldProcessThroughNextProcessorIfFirstFails() throws Exception {
		
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(true);
		doThrow(new Exception()).when(p1).process(any(Message.class));
		
		Processor p2 = mock(Processor.class);
		when(p2.supports(any(Message.class))).thenReturn(true);
		
		Processor p3 = mock(Processor.class);
		when(p3.supports(any(Message.class))).thenReturn(true);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2)
			.addProcessor(p3);
	
		processor.process(new Message());
		verify(p2).process(any(Message.class));
		verify(p3, never()).process(any(Message.class));
		
		processor.process(new Message());
		verify(p2).process(any(Message.class));
		verify(p3).process(any(Message.class));
		
	}
	
	@Test
	public void shouldSupportMessage() throws Exception {
		
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(true);
		
		Processor p2 = mock(Processor.class);
		when(p2.supports(any(Message.class))).thenReturn(true);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		Assert.assertTrue(processor.supports(new Message()));
	}
	
	@Test
	public void shouldNotSupportMessage() throws Exception {
		
		Processor p1 = mock(Processor.class);
		when(p1.supports(any(Message.class))).thenReturn(true);
		
		Processor p2 = mock(Processor.class);
		when(p2.supports(any(Message.class))).thenReturn(false);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		Assert.assertFalse(processor.supports(new Message()));
	}
	
	@Test
	public void shouldNotSupportMessageIfNoProcessors() throws Exception {
		
		RoundRobinProcessor processor = new RoundRobinProcessor();
	
		Assert.assertFalse(processor.supports(new Message()));
	}
	
	@Test
	public void shouldConfigureAndDestroyProcessors() throws Exception {
		
		Processor p1 = mock(Processor.class, withSettings().extraInterfaces(Configurable.class));
		Processor p2 = mock(Processor.class);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		processor.configure();
		
		verify((Configurable) p1).configure();
		
		processor.destroy();
		
		verify((Configurable) p1).destroy();
	}
	
	@Test
	public void shouldStartAndStopProcessors() throws Exception {
		Processor p1 = mock(Processor.class, withSettings().extraInterfaces(Serviceable.class));
		Processor p2 = mock(Processor.class);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		processor.doStart();
		
		verify((Serviceable) p1).doStart();
		
		processor.doStop();
		
		verify((Serviceable) p1).doStop();
	}
	
	@Test
	public void statusShouldBeUnknown() throws Exception {
		Processor p1 = mock(Processor.class);
		Processor p2 = mock(Processor.class);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		Assert.assertEquals(processor.getStatus(), Status.UNKNOWN);
	}
	
	@Test
	public void statusShouldBeOK() throws Exception {
		Processor p1 = mock(Processor.class, withSettings().extraInterfaces(Monitorable.class));
		when(((Monitorable) p1).getStatus()).thenReturn(Status.OK);
		
		Processor p2 = mock(Processor.class);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		Assert.assertEquals(processor.getStatus(), Status.OK);
	}
	
	@Test
	public void statusShouldBeFailed() throws Exception {
		Processor p1 = mock(Processor.class, withSettings().extraInterfaces(Monitorable.class));
		when(((Monitorable) p1).getStatus()).thenReturn(Status.FAILED);
		
		Processor p2 = mock(Processor.class);
		
		RoundRobinProcessor processor = new RoundRobinProcessor()
			.addProcessor(p1)
			.addProcessor(p2);
		
		Assert.assertEquals(processor.getStatus(), Status.FAILED);
	}
	
}
