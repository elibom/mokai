package org.mokai.types.mock;

import org.mokai.spi.ExposableConfiguration;
import org.mokai.spi.Message;
import org.mokai.spi.Processor;

public class MockConfigurableConnector implements Processor, ExposableConfiguration<MockConfigurableConnector> {
	
	private String config1;
	private int config2;
	
	public MockConfigurableConnector() {
		
	}
	
	public MockConfigurableConnector(String config1, int config2) {
		this.config1 = config1;
		this.config2 = config2;
			
	}

	@Override
	public void process(Message message) {
		
	}

	@Override
	public boolean supports(Message message) {
		return false;
	}

	@Override
	public MockConfigurableConnector getConfiguration() {
		return this;
	}

	public String getConfig1() {
		return config1;
	}

	public void setConfig1(String config1) {
		this.config1 = config1;
	}

	public int getConfig2() {
		return config2;
	}

	public void setConfig2(int config2) {
		this.config2 = config2;
	}

	@Override
	public boolean equals(Object obj) {
		if (MockConfigurableConnector.class.isInstance(obj)) {
			MockConfigurableConnector conn = (MockConfigurableConnector) obj;
			return conn.getConfig1().equals(config1) && conn.getConfig2() == config2;
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return (config1 + config2).hashCode();
	}

	@Override
	public String toString() {
		return "config1: " + config1 + " - config2: " + config2;
	}
	
}
