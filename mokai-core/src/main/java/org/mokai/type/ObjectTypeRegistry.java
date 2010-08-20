package org.mokai.type;

import java.util.Collection;

import org.mokai.spi.Acceptor;
import org.mokai.spi.Processor;
import org.mokai.spi.type.AcceptorType;
import org.mokai.spi.type.ReceiverType;
import org.mokai.spi.type.ProcessorType;

/**
 * 
 * @author German Escobar
 */
public interface ObjectTypeRegistry {
	
	void addReceiverType(ReceiverType receiverType) throws IllegalArgumentException;
	
	ReceiverType getReceiverType(Class<?> receiverClass) throws IllegalArgumentException;
	
	Collection<ReceiverType> getReceiverTypes();
	
	void addAcceptorType(AcceptorType acceptorType) throws IllegalArgumentException;

	AcceptorType getAcceptorType(Class<? extends Acceptor> acceptorClass) throws IllegalArgumentException;
	
	Collection<AcceptorType> getAcceptorsTypes();
	
	void addProcessorType(ProcessorType processorType) throws IllegalArgumentException;

	ProcessorType getProcessorType(Class<? extends Processor> processorClass) throws IllegalArgumentException;
	
	Collection<ProcessorType> getProcessorTypes();
	
}
