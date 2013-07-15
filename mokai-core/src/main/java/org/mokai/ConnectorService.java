package org.mokai;

import java.util.List;

import org.mokai.Monitorable.Status;

/**
 * <p>A wrapper of a {@link Connector} that holds a collection of acceptors, pre-processing actions,
 * post-processing actions and post-receiving actions.</p>
 * 
 * <p>Inside a {@link RoutingEngine}, it represents an application or a connection
 * capable of receiving or processing messages.</p>
 * 
 * @see RoutingEngine
 * @see Connector
 * @see Acceptor
 * @see Action
 * 
 * @author German Escobar
 */
public interface ConnectorService extends Service {

	/**
	 * @return the assigned id.
	 */
	String getId();

	/**
	 * The priority is used by the {@link RoutingEngine} to determine the order in which the
	 * connector services should be query in order to accept or reject a message. The priority
	 * only applies to connectors that implement the {@link Processor} interface.
	 * 
	 * @return the priority of this connector service.
	 */
	int getPriority();

	/**
	 * Sets the priority. This value is used to determine the order in which the connector
	 * services should be query in order to accept or reject a message. The priority
	 * only applies to connectors that implement the {@link Processor} interface.
	 * 
	 * @param priority can be a positive or negative number.
	 */
	void setPriority(int priority);

	/**
	 * Helper method to set a priority using a fluent API.
	 * 
	 * @param priority a positive or negative number.
	 * @return this connector service.
	 * 
	 * @see #setPriority(int)
	 */
	ConnectorService withPriority(int priority);

	/**
	 * The maximum number of messages that can be processed concurrently by the
	 * connector. This only applies to connectors that implement the {@link Processor}
	 * interface.
	 * 
	 * @return the maximum number of messages that can be processed concurrently.
	 */
	int getMaxConcurrentMsgs();

	/**
	 * Sets the maximum number of messages that can be processed concurrently by
	 * the connector. This only applies to connectors that implement the {@link Processor}
	 * interface.
	 * 
	 * @param maxConcurrentMsgs the maximum number of messages that can be processed
	 * concurrently.
	 */
	void setMaxConcurrentMsgs(int maxConcurrentMsgs);

	/**
	 * The wrapped connector.
	 * 
	 * @return the {@link Connector} that this connector service is wrapping.
	 */
	Connector getConnector();

	/**
	 * After a {@link Message} is accepted by the processor service, it queues
	 * the messages for future processing.
	 * @return the number of messages that are queued
	 */
	int getNumQueuedMessages();

	/**
	 * The status tells whether the service is ok or not. The status is calculated by first
	 * checking the status of the {@link Connector} (if it implements {@link Monitorable}).
	 * If the connector implements {@Processor} then this method also checks if the last
	 * message failed or was successfully processed.
	 * 
	 * @return the status of the connector service.
	 * @see Status
	 */
	Status getStatus();

	/**
	 * Adds an {@link Acceptor} to the collection of acceptors if it doesn't
	 * exists. Existence of an acceptor is determined by the
	 * {@link Acceptor#equals(Object)} method.
	 * 
	 * @param acceptor the {@link Acceptor} instance to be added.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the acceptor is null.
	 * @throws ObjectAlreadyExistsException if the acceptor already exists.
	 */
	ConnectorService addAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectAlreadyExistsException;

	/**
	 * Removes an {@link Acceptor} from the collection of acceptors if it exists.
	 * Existence of an acceptor is determined by the {@link Acceptor#equals(Object)}
	 * method.
	 * 
	 * @param acceptor the {@link Acceptor} instance to be removed.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the argument is null.
	 * @throws ObjectNotFoundException if the acceptor is not found.
	 */
	ConnectorService removeAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectNotFoundException;

	/**
	 * Retrieves the registered acceptors.
	 * 
	 * @return a list of {@link Acceptor} objects or an empty list if no
	 * acceptor has been added.
	 */
	List<Acceptor> getAcceptors();

	/**
	 * Adds an {@link Action} to the collection of pre-processing actions if it
	 * doesn't exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be added.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the action is null.
	 * @throws ObjectAlreadyExistsException if the action already exists in the
	 * collection of pre-processing actions.
	 */
	ConnectorService addPreProcessingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;

	/**
	 * Removes an {@link Action} from the collection of pre-processing actions if
	 * it exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be removed.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the action to be removed is null.
	 * @throws ObjectNotFoundException if the action is not found.
	 */
	ConnectorService removePreProcessingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;

	/**
	 * Retrieves the registered pre-processing actions.
	 * 
	 * @return a list of {@link Action} objects or an empty list if no
	 * pre-processing actions have been added.
	 */
	List<Action> getPreProcessingActions();

	/**
	 * Adds an {@link Action} to the collection of post-processing actions if it
	 * doesn't exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be added.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the action to be added is null.
	 * @throws ObjectAlreadyExistsException if the action already exists in the
	 * collection of post-processing actions.
	 */
	ConnectorService addPostProcessingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;

	/**
	 * Removes an {@link Action} from the collection of post-processing actions if
	 * it exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be removed.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the action to be removed is null.
	 * @throws ObjectNotFoundException if the action is not found in the
	 * collection of post-processing actions.
	 */
	ConnectorService removePostProcessingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;

	/**
	 * Retrieves the registered post-processing actions.
	 * 
	 * @return a list of {@link Action} objects or an empty list if no
	 * post-processing actions have been added.
	 */
	List<Action> getPostProcessingActions();

	/**
	 * Adds an {@link Action} to the collection of post-receiving actions if it
	 * doesn't exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be added.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the action to be added is null.
	 * @throws ObjectAlreadyExistsException if the action already exists in the
	 * collection of post-receiving actions.
	 */
	ConnectorService addPostReceivingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;

	/**
	 * Removes an {@link Action} from the collection of post-receiving actions if
	 * it exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be removed.
	 * @return the {@link ConnectorService} instance (fluent API).
	 * @throws IllegalArgumentException if the action to be removed is null.
	 * @throws ObjectNotFoundException if the action is not found in the
	 * collection of post-receiving actions.
	 */
	ConnectorService removePostReceivingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;

	/**
	 * Retrieves the registered post-receiving actions.
	 * 
	 * @return a list of {@link Action} objects or an empty list if no
	 * post-receiving actions have been added.
	 */
	List<Action> getPostReceivingActions();

	/**
	 * Called when the processor service is removed from the {@link RoutingEngine}
	 */
	void destroy();

}
