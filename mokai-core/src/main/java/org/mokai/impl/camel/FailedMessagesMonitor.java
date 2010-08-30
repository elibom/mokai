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
	
	private CamelRoutingEngine routingEngine;
	
	private State status = State.STOPPED;
	
	/**
	 * The delay before the first execution in milliseconds
	 */
	private long delay;
	
	/**
	 * The interval between executions in milliseconds
	 */
	private long interval;
	
	private Timer timer;
	
	@Override
	public State getState() {
		return status;
	}

	@Override
	public void start() {
		if (!status.isStartable()) {
			return;
		}
		
		timer = new Timer(true);
		timer.schedule(this, delay, interval);
		
		status = State.STARTED;
	}

	@Override
	public void stop() {
		if (!status.isStoppable()) {
			return;
		}
		
		timer.cancel();
		
		status = State.STOPPED;
	}

	@Override
	public void run() {
		if (routingEngine == null) {
			throw new IllegalStateException("no CamelRoutingContext provided");
		}
		
		routingEngine.retryFailedMessages();
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void setRoutingContext(CamelRoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

}
