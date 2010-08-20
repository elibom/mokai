package org.mokai.spi.type;

import java.io.Serializable;

public interface ReceiverType extends Serializable {
	
	String getName();
	
	String getDescription();

	Class<?> getConnectorClass();

}
