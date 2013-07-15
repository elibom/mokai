package org.mokai.impl.camel;


/**
 * Defines the maximum redeliveries and delay between attempts that the
 * {@link CamelRoutingEngine} should perform on {@link org.mokaiProcessor}s
 * before declaring the message as failed.
 *
 * @author German Escobar
 */
public class RedeliveryPolicy {

	private static final int DEFAULT_MAX_REDELIVERIES = 3;
	private static final long DEFAULT_MAX_REDELIVERY_DELAY = 3000;

	private int maxRedeliveries = DEFAULT_MAX_REDELIVERIES;

	private long maxRedeliveryDelay = DEFAULT_MAX_REDELIVERY_DELAY;

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
