package org.mokai;

import java.util.List;

import org.mokai.Monitorable.Status;

/**
 * Wraps a {@link Processor} implementation and holds a collection of acceptors, 
 * pre-processing actions, post-processing actions and post-receiving actions.
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
	
	ProcessorService addAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	ProcessorService removeAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectNotFoundException;

	List<Acceptor> getAcceptors();
	
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
