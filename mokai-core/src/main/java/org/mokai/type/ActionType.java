package org.mokai.type;

import java.io.Serializable;

import org.mokai.Action;

/**
 * 
 * @author German Escobar
 */
public class ActionType implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private Class<? extends Action> actionClass;
	
	public ActionType(String name, String description, Class<? extends Action> actionClass) {
		this.name = name;
		this.description = description;
		this.actionClass = actionClass;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Class<? extends Action> getActionClass() {
		return actionClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (ActionType.class.isInstance(obj)) {
			ActionType at = (ActionType) obj;
			
			return at.getActionClass().equals(actionClass);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return actionClass.hashCode();
	}
	
}
