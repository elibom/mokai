package org.mokai;

import java.util.List;

import org.mokai.spi.Processor;

/**
 * Accounts represents applications with the ability to identify themselves when
 * sending messages and the ability of receiving messages.
 * 
 * @author German Escobar
 */
public interface Account {
	
	String getId();

	int getPriority();

	ProcessorService createProcessor(String id, int priority, Processor processor) throws ObjectAlreadyExistsException;
	
	Account deleteProcessor(String id) throws ObjectNotFoundException;
	
	ProcessorService getProcessor(String id);
	
	List<ProcessorService> getProcessors();
	
	
}
