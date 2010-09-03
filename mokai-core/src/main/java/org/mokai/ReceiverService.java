package org.mokai;

import java.util.List;

import org.mokai.Monitorable.Status;

/**
 * <p>A wrapper of {@link Receiver} that holds a collection of post-receiving 
 * actions. Post-receiving actions are implementations of {@link Action}
 * interface and are called after a message has been received by the 
 * {@link Receiver}.</p>
 * 
 * @see RoutingEngine
 * @see Receiver
 * @see Action
 * 
 * @author German Escobar
 */
public interface ReceiverService extends Service {
	
	/**
	 * A receiver service is identified by a unique id that set by the caller in
	 * the {@link RoutingEngine#createReceiver(String, Receiver)} method.
	 * 
	 * @return the unique id of the processor service.
	 */
	String getId();
	
	/**
	 * The wrapped receiver.
	 * 
	 * @return the {@link Receiver} that this receiver service is managing.
	 */
	Receiver getReceiver();
	
	/**
	 * If the {@link Receiver} implements {@link Monitorable}, the status tells
	 * whether the receiver is working OK or not.
	 * 
	 * @return if the {@link Reciever} implements {@link Monitorable}, it returns
	 * the result of the {@link Monitorable#getStatus()} method, otherwise, it 
	 * returns {@link Status#UNKNOWN}.
	 *  
	 * @see Status
	 */
	Status getStatus();

	/**
	 * Adds an {@link Action} to the collection of post-receiving actions if it
	 * doesn't exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be added.
	 * @return the {@link ReceiverService} instance (fluent API).
	 * @throws IllegalArgumentException if the action to be added is null.
	 * @throws ObjectAlreadyExistsException f the action already exists in the 
	 * collection of post-receiving actions.
	 */
	ReceiverService addPostReceivingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	/**
	 * Removes an {@link Action} from the collection of post-receiving actions if
	 * it exists. Existence is determined by the {@link Action#equals(Object)}
	 * method.
	 * 
	 * @param action the {@link Action} to be removed.
	 * @return the {@link ReceiverService} instance (fluent API).
	 * @throws IllegalArgumentException if the action to be removed is null.
	 * @throws ObjectNotFoundException if the action is not found in the 
	 * collection of post-receiving actions.
	 */
	ReceiverService removePostReceivingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;
	
	/**
	 * Retrieves the registered post-receiving actions.
	 * 
	 * @return a list of {@link Action} objects or an empty list if no
	 * post-receiving actions have been added.
	 */
	List<Action> getPostReceivingActions();
	
	/**
	 * Called when the receiver service is removed from the {@link RoutingEngine}
	 */
	void destroy();
	
}
