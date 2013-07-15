package org.mokai.types.mock;

import org.apache.commons.lang.Validate;
import org.mokai.Acceptor;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

/**
 * Used to test if the enums are handled correctly from XML.
 *
 * @author German Escobar
 */
public class MockAcceptorWithEnum implements Acceptor, ExposableConfiguration<MockAcceptorWithEnum> {

	public enum MockEnum {
		FIRST_OPTION, SECOND_OPTION;

		public static MockEnum convert(String value) {
			Validate.notNull(value);

			if (value.equals("first")) {
				return FIRST_OPTION;
			} else if (value.equals("second")) {
				return SECOND_OPTION;
			}

			throw new IllegalArgumentException("value " + value + " not supported");
		}
	}

	private MockEnum mockEnum;

	@Override
	public boolean accepts(Message message) {
		return false;
	}

	@Override
	public MockAcceptorWithEnum getConfiguration() {
		return this;
	}

	public MockEnum getMockEnum() {
		return mockEnum;
	}

	public void setMockEnum(MockEnum mockEnum) {
		this.mockEnum = mockEnum;
	}

}
