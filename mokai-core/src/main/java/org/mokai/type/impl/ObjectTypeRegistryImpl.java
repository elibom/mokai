package org.mokai.type.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.mokai.acceptor.ExactMatchAcceptor;
import org.mokai.acceptor.RegExpAcceptor;
import org.mokai.spi.Acceptor;
import org.mokai.spi.Processor;
import org.mokai.spi.type.AcceptorType;
import org.mokai.spi.type.ProcessorType;
import org.mokai.spi.type.ReceiverType;
import org.mokai.type.ObjectTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTypeRegistryImpl implements ObjectTypeRegistry {
	
	Logger log = LoggerFactory.getLogger(ObjectTypeRegistryImpl.class);
	
	private Map<Class<? extends Acceptor>,AcceptorType> acceptorsTypes = new HashMap<Class<? extends Acceptor>,AcceptorType>();
	private Map<Class<?>,ReceiverType> connectorsTypes = new HashMap<Class<?>,ReceiverType>();
	private Map<Class<? extends Processor>,ProcessorType> processorTypes = new HashMap<Class<? extends Processor>,ProcessorType>();
	
	public ObjectTypeRegistryImpl() {
		//addBuiltInAcceptorTypes();
	}
	
	protected void addBuiltInAcceptorTypes() {	
		DefaultAcceptorType at1 = new DefaultAcceptorType("Regular Expression", 
				"Matches the field to a Regular Expression", RegExpAcceptor.class);
		addAcceptorType(at1);
		
		DefaultAcceptorType at2 = new DefaultAcceptorType("Exact Match", 
				"Matches the field to the expression", ExactMatchAcceptor.class);
		addAcceptorType(at2);
	}
	
	@Override
	public void addReceiverType(ReceiverType receiverType) throws IllegalArgumentException {
		Validate.notNull(receiverType);
		Validate.notEmpty(receiverType.getName());
		Validate.notNull(receiverType.getConnectorClass());
		
		connectorsTypes.put(receiverType.getConnectorClass(), receiverType);
	}
	
	@Override
	public ReceiverType getReceiverType(Class<?> connectorClass) throws IllegalArgumentException {
		Validate.notNull(connectorClass);
		
		return connectorsTypes.get(connectorClass);
	}

	@Override
	public Collection<ReceiverType> getReceiverTypes() {
		return Collections.unmodifiableCollection(connectorsTypes.values());
	}
	
	@Override
	public void addAcceptorType(AcceptorType acceptorType) throws IllegalArgumentException {
		Validate.notNull(acceptorType);
		Validate.notEmpty(acceptorType.getName());
		Validate.notNull(acceptorType.getAcceptorClass());
		
		acceptorsTypes.put(acceptorType.getAcceptorClass(), acceptorType);
	}

	@Override
	public AcceptorType getAcceptorType(Class<? extends Acceptor> acceptorClass) throws IllegalArgumentException {
		Validate.notNull(acceptorClass);
		
		return acceptorsTypes.get(acceptorClass);
	}

	@Override
	public Collection<AcceptorType> getAcceptorsTypes() {
		return Collections.unmodifiableCollection(acceptorsTypes.values());
	}
	
	@Override
	public void addProcessorType(ProcessorType processorType) throws IllegalArgumentException {
		Validate.notNull(processorType);
		Validate.notEmpty(processorType.getName());
		Validate.notNull(processorType.getProcessorClass());
		
		processorTypes.put(processorType.getProcessorClass(), processorType);
	}
	
	@Override
	public ProcessorType getProcessorType(Class<? extends Processor> processorClass) throws IllegalArgumentException {
		Validate.notNull(processorClass);
		
		return processorTypes.get(processorClass);
	}

	@Override
	public Collection<ProcessorType> getProcessorTypes() {
		return Collections.unmodifiableCollection(processorTypes.values());
	}

}
