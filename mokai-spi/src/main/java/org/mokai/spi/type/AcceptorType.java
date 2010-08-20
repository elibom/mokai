package org.mokai.spi.type;

import java.io.Serializable;

import org.mokai.spi.Acceptor;

public interface AcceptorType extends Serializable {

	String getName();
	
	String getDescription();
	
	Class<? extends Acceptor> getAcceptorClass();
	
}
