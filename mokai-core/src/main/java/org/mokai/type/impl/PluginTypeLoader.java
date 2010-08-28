package org.mokai.type.impl;

import java.util.HashSet;
import java.util.Set;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Processor;
import org.mokai.Receiver;
import org.mokai.plugin.PluginMechanism;
import org.mokai.plugin.jpf.JpfPluginMechanism;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ProcessorType;
import org.mokai.type.ReceiverType;
import org.mokai.type.TypeLoader;

/**
 * 
 * @author German Escobar
 */
public class PluginTypeLoader implements TypeLoader {
	
	private PluginMechanism pluginMechanism;
	
	public PluginTypeLoader() {
		this(new JpfPluginMechanism());
	}
	
	public PluginTypeLoader(PluginMechanism pluginMechanism) {
		this.pluginMechanism = pluginMechanism;
	}

	@Override
	public final Set<AcceptorType> loadAcceptorTypes() {
		
		Set<AcceptorType> acceptorTypes = new HashSet<AcceptorType>();
		
		Set<Class<? extends Acceptor>> acceptorClasses = pluginMechanism.loadTypes(Acceptor.class);
		for (Class<? extends Acceptor> acceptorClass : acceptorClasses) {
			AcceptorType acceptorType = TypeBuilder.buildAcceptorType(acceptorClass);
			acceptorTypes.add(acceptorType);
		}
		
		return acceptorTypes;
	}

	@Override
	public final Set<ActionType> loadActionTypes() {
		
		Set<ActionType> actionTypes = new HashSet<ActionType>();
		
		Set<Class<? extends Action>> actionClasses = pluginMechanism.loadTypes(Action.class);
		for (Class<? extends Action> actionClass : actionClasses) {
			ActionType actionType = TypeBuilder.buildActionType(actionClass);
			actionTypes.add(actionType);
		}

		return actionTypes;
	}

	@Override
	public final Set<ProcessorType> loadProcessorTypes() {
		
		Set<ProcessorType> processorTypes = new HashSet<ProcessorType>();
		
		Set<Class<? extends Processor>> processorClasses = pluginMechanism.loadTypes(Processor.class);
		for (Class<? extends Processor> processorClass : processorClasses) {
			ProcessorType processorType = TypeBuilder.buildProcessorType(processorClass);
			processorTypes.add(processorType);
		}
		
		return processorTypes;
	}

	@Override
	public final Set<ReceiverType> loadReceiverTypes() {
		
		Set<ReceiverType> receiverTypes = new HashSet<ReceiverType>();
		
		Set<Class<? extends Receiver>> receiverClasses = pluginMechanism.loadTypes(Receiver.class);
		for (Class<? extends Receiver> receiverClass : receiverClasses) {
			ReceiverType receiverType = TypeBuilder.buildReceiverType(receiverClass);
			receiverTypes.add(receiverType);
		}
		
		return receiverTypes;
	}

	public final PluginMechanism getPluginMechanism() {
		return pluginMechanism;
	}

	public final void setPluginMechanism(PluginMechanism pluginMechanism) {
		this.pluginMechanism = pluginMechanism;
	}
}
