package org.mokai.impl.camel;

public class RedeliveryPolicy {

	private int maxRedeliveries = 3;
	
	private long maxRedeliveryDelay = 3000;

	public int getMaxRedeliveries() {
		return maxRedeliveries;
	}

	public void setMaxRedeliveries(int maxRedeliveries) {
		this.maxRedeliveries = maxRedeliveries;
	}

	public long getMaxRedeliveryDelay() {
		return maxRedeliveryDelay;
	}

	public void setMaxRedeliveryDelay(long maxRedeliveryDelay) {
		this.maxRedeliveryDelay = maxRedeliveryDelay;
	}
	
}
