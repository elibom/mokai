package org.mokai.type.impl;

import org.mokai.spi.Acceptor;
import org.mokai.spi.type.AcceptorType;

public class DefaultAcceptorType implements AcceptorType {
	
	private static final long serialVersionUID = -4556664317935119401L;

	private String name;
	
	private String description;
	
	private Class<? extends Acceptor> acceptorClass;
	
	public DefaultAcceptorType(String name, String description, 
			Class<? extends Acceptor> acceptorClass) {
		this.name = name;
		this.description = description;
		this.acceptorClass = acceptorClass;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Class<? extends Acceptor> getAcceptorClass() {
		return acceptorClass;
	}

}
