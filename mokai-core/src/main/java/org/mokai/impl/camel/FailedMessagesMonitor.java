package org.mokai.impl.camel;

import java.util.Timer;
import java.util.TimerTask;

import org.mokai.Service;

/**
 * 
 * 
 * @author German Escobar
 */
public class FailedMessagesMonitor extends TimerTask implements Service {

	//private Logger log = LoggerFactory.getLogger(FailedMessagesMonitor.class);
	
	private static final long DEFAULT_DELAY = 0;
	private static final long DEFAULT_INTERVAL = 30000;
	
	private CamelRoutingEngine routingEngine;
	
	private State status = State.STOPPED;
	
	/**
	 * The delay before the first execution in milliseconds
	 */
	private long delay = DEFAULT_DELAY;
	
	/**
	 * The interval between executions in milliseconds
	 */
	private long interval = DEFAULT_INTERVAL;
	
	private Timer timer;
	
	@Override
	public final State getState() {
		return status;
	}

	@Override
	public final void start() {
		if (!status.isStartable()) {
			return;
		}
		
		timer = new Timer(true);
		timer.schedule(this, delay, interval);
		
		status = State.STARTED;
	}

	@Override
	public final void stop() {
		if (!status.isStoppable()) {
			return;
		}
		
		timer.cancel();
		
		status = State.STOPPED;
	}

	@Override
	public final void run() {
		if (routingEngine == null) {
			throw new IllegalStateException("no CamelRoutingContext provided");
		}
		
		routingEngine.retryFailedMessages();
	}

	public final long getDelay() {
		return delay;
	}

	public final void setDelay(long delay) {
		this.delay = delay;
	}

	public final long getInterval() {
		return interval;
	}

	public final void setInterval(long interval) {
		this.interval = interval;
	}

	public final void setRoutingEngine(CamelRoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

}
