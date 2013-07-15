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

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return description;
	}

	public final Class<? extends Acceptor> getAcceptorClass() {
		return acceptorClass;
	}

	@Override
	public final boolean equals(Object obj) {
		if (getClass() == obj.getClass()) {
			AcceptorType at = (AcceptorType) obj;

			return at.getAcceptorClass().equals(acceptorClass);
		}

		return false;
	}

	@Override
	public final int hashCode() {
		return acceptorClass.hashCode();
	}

}
