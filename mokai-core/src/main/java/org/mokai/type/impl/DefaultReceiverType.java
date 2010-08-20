package org.mokai.type.impl;

import org.mokai.spi.type.ReceiverType;

public class DefaultReceiverType implements ReceiverType {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private Class<?> connectorClass;
	
	public DefaultReceiverType(String name, String description, 
			Class<?> connectorClass) {
		this.name = name;
		this.description = description;
		this.connectorClass = connectorClass;
	}

	@Override
	public Class<?> getConnectorClass() {
		return connectorClass;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

}
