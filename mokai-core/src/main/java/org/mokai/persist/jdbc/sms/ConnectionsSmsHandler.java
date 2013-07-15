package org.mokai.persist.jdbc.sms;

import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.persist.jdbc.MessageHandler;

/**
 * A {@link MessageHandler} implementation that supports messages with
 * type {@link Message#SMS_TYPE} and {@link Direction#TO_CONNECTIONS}.
 *
 * @author German Escobar
 */
public class ConnectionsSmsHandler extends AbstractSmsHandler {

	public static final String DEFAULT_TABLENAME = "CONNECTIONS_MSGS";

	@Override
	public final boolean supportsDirection(Direction direction) {
		if (direction != null && direction.equals(Direction.TO_CONNECTIONS)) {
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
		return Direction.TO_CONNECTIONS;
	}

}
