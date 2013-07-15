package org.mokai.types.mock;

import org.mokai.Acceptor;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

public class MockConfigurableAcceptor implements Acceptor, ExposableConfiguration<MockConfigurableAcceptor> {

	private String config1;

	private int config2;

	public MockConfigurableAcceptor() {

	}

	public MockConfigurableAcceptor(String config1, int config2) {
		this.config1 = config1;
		this.config2 = config2;
	}

	@Override
	public boolean accepts(Message message) {
		return false;
	}

	@Override
	public MockConfigurableAcceptor getConfiguration() {
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
		if (MockConfigurableAcceptor.class.isInstance(obj)) {
			MockConfigurableAcceptor acceptor = (MockConfigurableAcceptor) obj;

			return acceptor.getConfig1().equals(config1) && acceptor.getConfig2() == config2;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (config1 + config2).hashCode();
	}

	@Override
	public String toString() {
		return "MockConfigurableAcceptor[config1: " + config1 + " - config2: " + config2 + "]";
	}

}
