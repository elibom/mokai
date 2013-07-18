package org.mokai;

import java.util.List;

import org.mokai.persist.MessageStore;

/**
 * Controls the message flow inside the gateway between applications and connections, which are represented
 * by a {@link ConnectorService}.
 *
 * @author German Escobar
 */
public interface RoutingEngine {

	/**
	 * Adds an application with the specified id and {@link Connector}. The application is represented by
	 * a {@link ConnectorService}.
	 *
	 * @param id the id of the application which must be unique and not null. Any white spaces will
	 * be removed and it will be lower cased.
	 * @param connector the {@link Connector} implementation.
	 * @return a {@link ConnectorService} implementation that represents the application. It is NOT started
	 * by default.
	 * @throws IllegalArgumentException if id is null or empty, or if connector is null.
	 * @throws ObjectAlreadyExistsException if an application with the same id already exists.
	 */
	ConnectorService addApplication(String id, Connector connector) throws IllegalArgumentException,
			ObjectAlreadyExistsException;

	/**
	 * Removes the application with the specified id. It calls the {@link ConnectorService#destroy()} method.
	 *
	 * @param id the id of the application to be removed.
	 * @return this routing engine instance.
	 * @throws IllegalArgumentException if the id is null or empty.
	 * @throws ObjectNotFoundException if the id is not found.
	 */
	RoutingEngine removeApplication(String id) throws IllegalArgumentException, ObjectNotFoundException;

	/**
	 * Retrieves the application that matches the specified id.
	 *
	 * @param id the id of the application that is going to be retrieved.
	 * @return a {@link ConnectorService} implementation representing the application.
	 * @throws IllegalArgumentException if the id is null or empty.
	 */
	ConnectorService getApplication(String id) throws IllegalArgumentException;

	/**
	 * Returns the list of applications ordered by priority in ascendent order.
	 *
	 * @return a list of {@link ConnectorService} instances ordered by priority or an empty list if no application
	 * has been registered.
	 */
	List<ConnectorService> getApplications();

	/**
	 * Adds a connection with the specified id and {@link Connector}. The connection is represented by
	 * a {@link ConnectorService}.
	 *
	 * @param id the id of the connection which must be unique and not null. Any white spaces will
	 * be removed and it will be lower cased.
	 * @param connector the {@link Connector} implementation.
	 * @return a {@link ConnectorService} implementation that represents the connection. It is NOT started
	 * by default.
	 * @throws IllegalArgumentException if id is null or empty, or if connector is null.
	 * @throws ObjectAlreadyExistsException if a connection with the same id already exists.
	 */
	ConnectorService addConnection(String id, Connector connector) throws IllegalArgumentException,
			ObjectAlreadyExistsException;

	/**
	 * Removes the connection with the specified id. It calls the {@link ConnectorService#destroy()} method.
	 *
	 * @param id the id of the connection to be removed.
	 * @return this routing engine instance.
	 * @throws IllegalArgumentException if the id is null or empty.
	 * @throws ObjectNotFoundException if the id is not found.
	 */
	RoutingEngine removeConnection(String id) throws IllegalArgumentException, ObjectNotFoundException;

	/**
	 * Returns the list of connections ordered by priority in ascendent order.
	 *
	 * @param id the id of the connection that is going to be retrieved.
	 * @return a {@link ConnectorService} implementation representing the connection or null if not found.
	 * @throws IllegalArgumentException if the id is null or empty.
	 */
	ConnectorService getConnection(String id) throws IllegalArgumentException;

	/**
	 * Returns the list of connections ordered by priority in ascendent order.
	 *
	 * @return a list of {@link ConnectorService} instances ordered by priority or an empty list if no connection
	 * has been registered.
	 */
	List<ConnectorService> getConnections();

	/**
	 * @return the implementation of the MessageStore that is being used. It should never be null.
	 */
	MessageStore getMessageStore();

}
