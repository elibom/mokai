package org.mokai.type.impl;

import java.util.HashSet;
import java.util.Set;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.plugin.PluginMechanism;
import org.mokai.plugin.jpf.JpfPluginMechanism;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ConnectorType;
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
	public final Set<ConnectorType> loadConnectorTypes() {
		Set<ConnectorType> processorTypes = new HashSet<ConnectorType>();

		Set<Class<? extends Connector>> connectorClasses = pluginMechanism.loadTypes(Connector.class);
		for (Class<? extends Connector> connectorClass : connectorClasses) {
			ConnectorType connectorType = TypeBuilder.buildConnectorType(connectorClass);
			processorTypes.add(connectorType);
		}

		return processorTypes;
	}

	public final PluginMechanism getPluginMechanism() {
		return pluginMechanism;
	}

	public final void setPluginMechanism(PluginMechanism pluginMechanism) {
		this.pluginMechanism = pluginMechanism;
	}
}
