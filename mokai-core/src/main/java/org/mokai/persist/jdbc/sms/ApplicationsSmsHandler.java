package org.mokai.persist.jdbc.sms;

import org.mokai.Message.Direction;

public class ApplicationsSmsHandler extends AbstractSmsHandler {

	public static final String DEFAULT_TABLENAME = "APPLICATIONS_MSGS";

	@Override
	public boolean supportsDirection(Direction direction) {
		if (direction != null && direction.equals(Direction.TO_APPLICATIONS)) {
			return true;
		}

		return false;
	}

	@Override
	protected String getDefaultTableName() {
		return DEFAULT_TABLENAME;
	}

	@Override
	protected Direction getMessageDirection() {
		return Direction.TO_APPLICATIONS;
	}

}
