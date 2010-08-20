package org.mokai.impl.camel;

import java.util.Timer;
import java.util.TimerTask;

import org.mokai.RoutingEngine;
import org.mokai.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailedMessagesMonitor extends TimerTask implements Service {
	
	private Logger log = LoggerFactory.getLogger(FailedMessagesMonitor.class);
	
	private RoutingEngine routingContext;
	
	private Status status = Status.STOPPED;
	
	private long interval;
	
	private Timer timer;
	
	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void start() {
		if (!status.isStartable()) {
			return;
		}
		
		timer = new Timer(true);
		timer.schedule(this, 30000, interval);
		
		status = Status.STARTED;
	}

	@Override
	public void stop() {
		if (!status.isStoppable()) {
			return;
		}
		
		timer.cancel();
		
		status = Status.STOPPED;
	}

	@Override
	public void run() {
		// TODO check that routing context is not null
		
		routingContext.retryFailedMessages();
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void setRoutingContext(RoutingEngine routingContext) {
		this.routingContext = routingContext;
	}
	
}
