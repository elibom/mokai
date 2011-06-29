package org.mokai.type;

import java.io.Serializable;

import org.mokai.Connector;

/**
 * 
 * @author German Escobar
 */
public class ConnectorType implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private Class<? extends Connector> connectorClass;
	
	public ConnectorType(String name, String description, Class<? extends Connector> connectorClass) {
		this.name = name;
		this.description = description;
		this.connectorClass = connectorClass;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return description;
	}

	public final Class<? extends Connector> getConnectorClass() {
		return connectorClass;
	}

	@Override
	public final boolean equals(Object obj) {
		if (ConnectorType.class.isInstance(obj)) {
			ConnectorType pt = (ConnectorType) obj;
			
			return pt.getConnectorClass().equals(connectorClass);
		}
		
		return false;
	}

	@Override
	public final int hashCode() {
		return connectorClass.hashCode();
	}
	
	
	
}
