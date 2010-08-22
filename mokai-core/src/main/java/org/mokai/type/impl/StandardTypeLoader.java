package org.mokai.type.impl;

import java.util.HashSet;
import java.util.ServiceLoader;
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

/**
 * Loads the types using the java.util.ServiceLoader from the JDK. 
 * 
 * @author German Escobar
 */
public class StandardTypeLoader implements TypeLoader {

	@Override
	public Set<AcceptorType> loadAcceptorTypes() {
		
		Set<AcceptorType> acceptorTypes = new HashSet<AcceptorType>();
		
		ServiceLoader<Acceptor> serviceLoader = ServiceLoader.load(Acceptor.class, this.getClass().getClassLoader());
		
		for (Acceptor acceptor : serviceLoader) {
			Class<? extends Acceptor> acceptorClass = acceptor.getClass();
			AcceptorType acceptorType = TypeBuilder.buildAcceptorType(acceptorClass);
			acceptorTypes.add(acceptorType);
		}
		
		return acceptorTypes;
	}

	@Override
	public Set<ActionType> loadActionTypes() {
		
		Set<ActionType> actionTypes = new HashSet<ActionType>();
		
		ServiceLoader<Action> serviceLoader = ServiceLoader.load(Action.class);
		
		for (Action action : serviceLoader) {
			Class<? extends Action> actionClass = action.getClass();
			ActionType actionType = TypeBuilder.buildActionType(actionClass);
			actionTypes.add(actionType);
		}
		
		return actionTypes;
	}

	@Override
	public Set<ProcessorType> loadProcessorTypes() {
		
		Set<ProcessorType> processorTypes = new HashSet<ProcessorType>();
		
		ServiceLoader<Processor> serviceLoader = ServiceLoader.load(Processor.class);
		
		for (Processor processor : serviceLoader) {
			Class<? extends Processor> processorClass = processor.getClass();
			ProcessorType processorType = TypeBuilder.buildProcessorType(processorClass);
			processorTypes.add(processorType);
		}
		
		return processorTypes;
	}

	@Override
	public Set<ReceiverType> loadReceiverTypes() {
		
		Set<ReceiverType> receiverTypes = new HashSet<ReceiverType>();
		
		ServiceLoader<Receiver> serviceLoader = ServiceLoader.load(Receiver.class);
		
		for (Receiver receiver : serviceLoader) {
			Class<? extends Receiver> receiverClass = receiver.getClass();
			ReceiverType receiverType = TypeBuilder.buildReceiverType(receiverClass);
			receiverTypes.add(receiverType);
		}
		
		return receiverTypes;
	}
	
}
