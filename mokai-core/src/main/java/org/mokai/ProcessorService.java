package org.mokai;

import java.util.List;

import org.mokai.Monitorable.Status;

/**
 * <p>A wrapper of a {@link Processor} implementation that holds a collection of 
 * acceptors, pre-processing actions, post-processing actions and post-receiving 
 * actions.</p>
 * 
 * <p>The {@link RoutingEngine} uses the collection of {@link Acceptor}s and the 
 * {@link Processor#supports(Message)} method to determine if this processor service
 * will handle the message. If it does, the message should be queued before it is 
 * processed (this is not mandatory but a good practice).</p> 
 * 
 * <p>The pre-processing actions, post-processing actions and post-receiving actions
 * are implementations of the {@link Action} interface. The pre-processing actions
 * are called before the message is processed. The post-processing actions are called
 * after a message is processed and the post-receiving actions are called after a 
 * message has been received by the {@link Processor}.</p> 
 * 
 * @author German Escobar
 */
public interface ProcessorService extends Service {

	/**
	 * @return the unique id of the processor service.
	 */
	String getId();

	/**
	 * Value is used to determine the order in which the processor services should 
	 * be query in order to accept or reject a message.
	 * 
	 * @return the priority of this processor service.
	 */
	int getPriority();
	
	/**
	 * Sets the priority of the processor service. This value is used to determine 
	 * the order in which the processor services should be query in order to accept 
	 * or reject a message.
	 * 
	 * @param priority can be a positive or negative number.
	 */
	void setPriority(int priority);
	
	/**
	 * The wrapped processor.
	 * @return the {@link Processor} that this processor service is managing.
	 */
	Processor getProcessor();
	
	/**
	 * After a {@link Message} is accepted by the processor service, it queues
	 * the messages for future processing.
	 * @return the number of messages that are queued
	 */
	int getNumQueuedMessages();
	
	/**
	 * The status tells whether the service is in good health or {@link Message}s 
	 * are failing. The status is calculated by first checking the status of the 
	 * {@link Processor} (if it implements {@link Monitorable}) and then checking
	 * if the last message failed or was successfully processed.
	 *  
	 * @return the status of the processor service.
	 */
	Status getStatus();
	
	/**
	 * Adds an {@link Acceptor} to the collection of acceptors if it doesn't 
	 * exists. Existence of an acceptor is determined by the 
	 * {@link Acceptor#equals(Object)} method. 
	 * 
	 * @param acceptor the {@link Acceptor} instance to be added. 
	 * @return the {@link ProcessorService} instance (fluent API).
	 * @throws IllegalArgumentException if the acceptor is null.
	 * @throws ObjectAlreadyExistsException if the acceptor already exists.
	 */
	ProcessorService addAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	/**
	 * Removes an {@link Acceptor} from the collection of acceptors if it exists.
	 * Existence of an acceptor is determined by the {@link Acceptor#equals(Object)} 
	 * method.
	 * 
	 * @param acceptor the {@link Acceptor} instance to be removed.
	 * @return the {@link ProcessorService} instance (fluent API).
	 * @throws IllegalArgumentException if the argument is null.
	 * @throws ObjectNotFoundException if the acceptor is not found.
	 */
	ProcessorService removeAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectNotFoundException;

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
	 * @return the {@link ProcessorService} instance (fluent API).
	 * @throws IllegalArgumentException if the action is null.
	 * @throws ObjectAlreadyExistsException if the action already exists.
	 */
	ProcessorService addPreProcessingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	ProcessorService removePreProcessingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;
	
	List<Action> getPreProcessingActions();
	
	ProcessorService addPostProcessingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	ProcessorService removePostProcessingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;
	
	List<Action> getPostProcessingActions();
	
	ProcessorService addPostReceivingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	ProcessorService removePostReceivingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;
	
	List<Action> getPostReceivingActions();
	
	/**
	 * Called when the processor service is removed from the {@link RoutingEngine}
	 */
	void destroy();
	
}
