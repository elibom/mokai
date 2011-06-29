package org.mokai.type;

import java.util.Set;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;

/**
 * Mantains a cached registry of the different types.
 * 
 * @author German Escobar
 */
public interface TypeRegistry {

	AcceptorType getAcceptorType(Class<? extends Acceptor> acceptorClass) throws IllegalArgumentException;
	
	Set<AcceptorType> getAcceptorsTypes();

	ConnectorType getConnectorType(Class<? extends Connector> connectorClass) throws IllegalArgumentException;
	
	Set<ConnectorType> getConnectorTypes();
	
	ActionType getActionType(Class<? extends Action> actionClass) throws IllegalArgumentException;
	
	Set<ActionType> getActionTypes();
	
	/**
	 * Reloads the types from the {@link TypeLoader}s. 
	 * 
	 * Usually, implementations of this interface will use a caching mechanism to 
	 * store the types and avoid calling the {@link TypeLoader}s every time, which is a 
	 * costly operation. This method will clear the cache (if exists) and reload 
	 * the types. 
	 */
	void reload();
	
	void addTypeLoader(TypeLoader typeLoader) throws IllegalArgumentException;
	
	void removeTypeLoader(TypeLoader typeLoader) throws IllegalArgumentException;
	
	Set<TypeLoader> getTypeLoaders();
}
