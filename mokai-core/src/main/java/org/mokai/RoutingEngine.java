package org.mokai;

import java.util.Collection;
import java.util.List;

import org.mokai.spi.Processor;

/**
 * 
 * 
 * @author German Escobar
 */
public interface RoutingEngine {

	/**
	 * Creates a new {@link ProcessorService} and starts it.
	 * @param id the id of this processor service. Any white spaces will
	 * be removed and it will be lower cased.
	 * @param priority the priority of this processor service.
	 * @param processor the {@link Processor} implementation
	 * @return a new, started {@link ProcessorService} implementation.
	 * @throws IllegalArgumentException if id is null or empty, or if processor
	 * is null.
	 * @throws ObjectAlreadyExistsException if the id already exists.
	 */
	ProcessorService createProcessor(String id, int priority, Processor processor) 
			throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	RoutingEngine removeProcessor(String id) throws IllegalArgumentException, 
			ObjectNotFoundException;
	
	/**
	 * Retrieves the {@link ProcessorService} identified by the id argument.
	 * @param id the fixed id (should not contain white spaces or)
	 * @return the {@link ProcessorService} instance or null if not found.
	 */
	ProcessorService getProcessor(String id);
	
	/**
	 * Retrieves the registered processors ordered by priority (1 is the maximum priority)
	 * @return a {@link List} of {@link ProcessorService} instances ordered by priority
	 */
	List<ProcessorService> getProcessors();
	
	ReceiverService createReceiver(String id, Object connector) 
			throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	RoutingEngine removeReceiver(String id) throws IllegalArgumentException, 
			ObjectNotFoundException;
	
	ReceiverService getReceiver(String id);
	
	Collection<ReceiverService> getReceivers();
	
	void retryFailedMessages();
	
	void retryUnRoutableMessages();
	
}
