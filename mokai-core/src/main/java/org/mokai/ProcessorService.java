package org.mokai;

import java.util.List;

import org.mokai.Monitorable.Status;

/**
 * Wraps a {@link Processor} and adds the acceptors, pre-processing actions,
 * post-processing actions and post-receiving actions.
 * 
 * @author German Escobar
 */
public interface ProcessorService extends Service {

	/**
	 * @return the unique id of the processor service.
	 */
	String getId();

	int getPriority();
	
	void setPriority(int priority);
	
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
	 * {@link Processor} (if it implements {@link Monitoreable}) and then checking
	 * the last message failed or was successfully processed.
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
	
	void destroy();
	
}
