package org.mokai.type.impl;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ConnectorType;
import org.mokai.type.TypeLoader;

/**
 * Loads the types using the java.util.ServiceLoader from the JDK. 
 * 
 * @author German Escobar
 */
public class StandardTypeLoader implements TypeLoader {

	@Override
	public final Set<AcceptorType> loadAcceptorTypes() {
		
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
	public final Set<ActionType> loadActionTypes() {
		
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
	public final Set<ConnectorType> loadConnectorTypes() {
		
		Set<ConnectorType> processorTypes = new HashSet<ConnectorType>();
		
		ServiceLoader<Connector> serviceLoader = ServiceLoader.load(Connector.class);
		
		for (Connector connector : serviceLoader) {
			Class<? extends Connector> connectorClass = connector.getClass();
			ConnectorType processorType = TypeBuilder.buildConnectorType(connectorClass);
			processorTypes.add(processorType);
		}
		
		return processorTypes;
	}
	
}
