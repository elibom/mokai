package org.mokai.spi.type;

import java.io.Serializable;

import org.mokai.spi.Processor;

public interface ProcessorType extends Serializable {

	String getName();
	
	String getDescription();
	
	Class<? extends Processor> getProcessorClass();
	
}
