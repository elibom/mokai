package org.mokai;

import java.util.Collection;
import java.util.List;

import org.mokai.Processor;

/**
 * Factory and container of {@link ReceiverService} and {@link ProcessorService}
 * implementations that controls the flow of messages between receivers and 
 * processors.
 * 
 * 
 * 
 * @author German Escobar
 */
public interface RoutingEngine {

	/**
	 * Creates an implementation of {@link ProcessorService} with the provided 
	 * arguments, stores it in a collection of processor services and returns it.
	 * 
	 * @param id the id of this processor service. Any white spaces will
	 * be removed and it will be lower cased.
	 * @param priority the priority of this processor service. Can be 
	 * positive or negative.
	 * @param processor the {@link Processor} implementation
	 * @return a new {@link ProcessorService} implementation.
	 * @throws IllegalArgumentException if id is null or empty, or if processor
	 * is null.
	 * @throws ObjectAlreadyExistsException if a processor service with the
	 * same id already exists.
	 */
	ProcessorService createProcessor(String id, int priority, Processor processor) 
			throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	/**
	 * Locates a the {@link ProcessorService} implementation with the specified id,
	 * calls the {@link ProcessorService#destroy()} method and removes it from the 
	 * collection of processor services.
	 * 
	 * @param id the id of the processor service that is going to be removed.
	 * @return the {@link RoutingEngine} instance.
	 * @throws IllegalArgumentException if the id is null or empty.
	 * @throws ObjectNotFoundException if the id is not found.
	 */
	RoutingEngine removeProcessor(String id) throws IllegalArgumentException, 
			ObjectNotFoundException;
	
	/**
	 * Retrieves the {@link ProcessorService}, identified by the id argument, from 
	 * the collection of processor services.
	 * 
	 * @param id the id of the processor service that is going to be retrieved.
	 * @return the {@link ProcessorService} instance or null if not found.
	 * @throws IllegalArgumentException if the id is null or empty.
	 */
	ProcessorService getProcessor(String id) throws IllegalArgumentException;
	
	/**
	 * Retrieves the registered processors ordered by priority ascendent.
	 * 
	 * @return a list of {@link ProcessorService} instances ordered by priority
	 * or an empty list if no processor service has been registered.
	 */
	List<ProcessorService> getProcessors();
	
	/**
	 * Creates an implementation of {@link ReceiverService} with the specified 
	 * arguments, stores it in a collection of receiver services and returns it.
	 * 
	 * @param id the id of this receiver service. Any white spaces will
	 * be removed and it will be lower cased.
	 * @param receiver the {@link Receiver} implementation
	 * @return a new {@link ReceiverService} implementation
	 * @throws IllegalArgumentException if id is null or empty, or if receiver
	 * is null.
	 * @throws ObjectAlreadyExistsException if a receiver service with the same
	 * id already exists.
	 */
	ReceiverService createReceiver(String id, Receiver receiver) 
			throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	/**
	 * Locates a {@link ReceiverService} implementation with the specified id, 
	 * calls the {@link ReceiverService#destroy()} method and removes it from the
	 * collection of receiver services.
	 * 
	 * @param id the id of the receiver service that is going to be removed.
	 * @return the {@link RoutingEngine} instance.
	 * @throws IllegalArgumentException if the id is null or empty.
	 * @throws ObjectNotFoundException if the id is not found.
	 */
	RoutingEngine removeReceiver(String id) throws IllegalArgumentException, 
			ObjectNotFoundException;
	
	/**
	 * Retrieves the {@link ReceiverService} identified with the id argument
	 * from the collection of receiver services.
	 * 
	 * @param id the id of the receiver service that is going to be retrieved.
	 * @return the {@link ReceiverService} instance or null if not found.
	 * @throws IllegalArgumentException if the id is null or empty.
	 */
	ReceiverService getReceiver(String id) throws IllegalArgumentException;
	
	/**
	 * Retrieves the registered receiver services.
	 * 
	 * @return a collection of {@link ReceiverService} instances or an empty
	 * list if no receiver service has been registered.
	 */
	Collection<ReceiverService> getReceivers();
	
}
