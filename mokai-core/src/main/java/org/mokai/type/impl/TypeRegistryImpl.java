package org.mokai.type.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Processor;
import org.mokai.Receiver;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ProcessorType;
import org.mokai.type.ReceiverType;
import org.mokai.type.TypeLoader;
import org.mokai.type.TypeRegistry;

/**
 * 
 * @author German Escobar
 */
public class TypeRegistryImpl implements TypeRegistry {
	
	/*private Map<Class<? extends Acceptor>,AcceptorType> acceptorTypes;
	
	private Map<Class<? extends Action>,ActionType> actionTypes;
	
	private Map<Class<? extends Processor>,ProcessorType> processorTypes;
	
	private Map<Class<? extends Receiver>,ReceiverType> receiverTypes;*/
	
	private Set<TypeLoader> typeLoaders = Collections.synchronizedSet(new HashSet<TypeLoader>());

	@Override
	public AcceptorType getAcceptorType(Class<? extends Acceptor> acceptorClass)
			throws IllegalArgumentException {
		return null;
	}
	
	@Override
	public Set<AcceptorType> getAcceptorsTypes() {
		return null;
	}

	@Override
	public ActionType getActionType(Class<? extends Action> actionClass)
			throws IllegalArgumentException {
		return null;
	}

	@Override
	public Set<ActionType> getActionTypes() {
		return null;
	}

	@Override
	public ProcessorType getProcessorType(Class<? extends Processor> processorClass)
			throws IllegalArgumentException {
		return null;
	}

	@Override
	public Set<ProcessorType> getProcessorTypes() {
		return null;
	}

	@Override
	public ReceiverType getReceiverType(Class<? extends Receiver> receiverClass)
			throws IllegalArgumentException {
		return null;
	}

	@Override
	public Set<ReceiverType> getReceiverTypes() {
		return null;
	}

	@Override
	public void reload() {
		
	}
	
	@Override
	public void addTypeLoader(TypeLoader typeLoader) throws IllegalArgumentException {
		typeLoaders.add(typeLoader);
	}

	@Override
	public void removeTypeLoader(TypeLoader typeLoader)	throws IllegalArgumentException {
		typeLoaders.remove(typeLoader);
	}

	@Override
	public Set<TypeLoader> getTypeLoaders() {
		return Collections.unmodifiableSet(typeLoaders);
	}

}
