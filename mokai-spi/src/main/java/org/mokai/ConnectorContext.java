package org.mokai;

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
}
