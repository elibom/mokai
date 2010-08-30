package org.mokai.impl.camel;

import org.mokai.Processor;

/**
 * Defines the maximum redeliveries and delay between attempts that the 
 * {@link CamelRoutingEngine} should perform on {@link Processor}s before
 * declaring the message as failed.
 * 
 * @author German Escobar
 */
public class RedeliveryPolicy {

	private int maxRedeliveries = 3;
	
	private long maxRedeliveryDelay = 3000;

	public final int getMaxRedeliveries() {
		return maxRedeliveries;
	}

	public final void setMaxRedeliveries(int maxRedeliveries) {
		this.maxRedeliveries = maxRedeliveries;
	}

	public final long getMaxRedeliveryDelay() {
		return maxRedeliveryDelay;
	}

	public final void setMaxRedeliveryDelay(long maxRedeliveryDelay) {
		this.maxRedeliveryDelay = maxRedeliveryDelay;
	}
	
}