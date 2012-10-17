package org.mokai.type.impl;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ConnectorType;

/**
 * 
 * @author German Escobar
 */
public final class TypeBuilder {
	
	private TypeBuilder() {}

	public static AcceptorType buildAcceptorType(Class<? extends Acceptor> acceptorClass) {
		
		String name = getName(acceptorClass);
		String description = getDescription(acceptorClass);
		
		return new AcceptorType(name, description, acceptorClass);
	}
	
	public static ActionType buildActionType(Class<? extends Action> actionClass) {
		
		String name = getName(actionClass);
		String description = getDescription(actionClass);
		
		return new ActionType(name, description, actionClass);
	}
	
	public static ConnectorType buildConnectorType(Class<? extends Connector> connectorClass) {

		String name = getName(connectorClass);
		String description = getDescription(connectorClass);
		
		return new ConnectorType(name, description, connectorClass);
	}
	
	private static String getName(Class<?> clazz) {
		String name = "";	
		if (clazz.isAnnotationPresent(Name.class)) {
			Name nameAnnotation = clazz.getAnnotation(Name.class);
			name = nameAnnotation.value();
		}
		
		return name;
	}
	
	private static String getDescription(Class<?> clazz) {
		String description = "";
		if (clazz.isAnnotationPresent(Description.class)) {
			Description descriptionAnnotation = clazz.getAnnotation(Description.class);
			description = descriptionAnnotation.value();
		}
		
		return description;
	}
}
