package org.mokai.type;

import java.util.Set;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Processor;
import org.mokai.Receiver;

/**
 * Mantains a 
 * 
 * @author German Escobar
 */
public interface TypeRegistry {
	
	ReceiverType getReceiverType(Class<? extends Receiver> receiverClass) throws IllegalArgumentException;
	
	Set<ReceiverType> getReceiverTypes();

	AcceptorType getAcceptorType(Class<? extends Acceptor> acceptorClass) throws IllegalArgumentException;
	
	Set<AcceptorType> getAcceptorsTypes();

	ProcessorType getProcessorType(Class<? extends Processor> processorClass) throws IllegalArgumentException;
	
	Set<ProcessorType> getProcessorTypes();
	
	ActionType getActionType(Class<? extends Action> actionClass) throws IllegalArgumentException;
	
	Set<ActionType> getActionTypes();
	
	/**
	 * Reloads the types from the {@link TypeLoader}s. 
	 * 
	 * Usually, implementations of this interface will use a caching mechanism to 
	 * store the types and avoid calling the {@TypeLoader}s every time, which is a 
	 * costly operation. This method will clear the cache (if exists) and reload 
	 * the types. 
	 */
	void reload();
	
	void addTypeLoader(TypeLoader typeLoader) throws IllegalArgumentException;
	
	void removeTypeLoader(TypeLoader typeLoader) throws IllegalArgumentException;
	
	Set<TypeLoader> getTypeLoaders();
}
