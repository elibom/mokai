package org.mokai.types.mock;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

public class MockConfigurableAction implements Action, ExposableConfiguration<MockConfigurableAction> {

	private String config1;

	private int config2;

	public MockConfigurableAction() {

	}

	public MockConfigurableAction(String config1, int config2) {
		this.config1 = config1;
		this.config2 = config2;
	}

	@Override
	public void execute(Message message) throws Exception {

	}

	@Override
	public MockConfigurableAction getConfiguration() {
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
		if (MockConfigurableAction.class.isInstance(obj)) {
			MockConfigurableAction action = (MockConfigurableAction) obj;
			return action.getConfig1().equals(config1) && action.getConfig2() == config2;
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
