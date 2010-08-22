package org.mokai.type;

import java.io.Serializable;

import org.apache.commons.lang.Validate;
import org.mokai.Acceptor;

/**
 * 
 * @author German Escobar
 */
public class AcceptorType implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private Class<? extends Acceptor> acceptorClass;
	
	public AcceptorType(String name, String description, Class<? extends Acceptor> acceptorClass) 
			throws IllegalArgumentException {
		
		Validate.notNull(acceptorClass);
		
		this.name = name;
		this.description = description;
		this.acceptorClass = acceptorClass;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Class<? extends Acceptor> getAcceptorClass() {
		return acceptorClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (AcceptorType.class.isInstance(obj)) {
			AcceptorType at = (AcceptorType) obj;
			
			return at.getAcceptorClass().equals(acceptorClass);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return acceptorClass.hashCode();
	}
	
}
