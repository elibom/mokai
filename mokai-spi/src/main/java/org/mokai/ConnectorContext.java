package org.mokai;

import org.mokai.Message.Direction;

/**
 * Holds external information about the {@link Connector} implementation.
 *
 * @author German Escobar
 */
public interface ConnectorContext {

	/**
	 * @return the id of the {@link Connector} object.
	 */
	String getId();

	/**
	 * Tells if the connector is configured as a connection or application.
	 *
	 * @return the direction of the {@link Connector} object.
	 */
	Direction getDirection();
}
