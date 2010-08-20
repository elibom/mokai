package org.mokai.type.impl;

import org.mokai.spi.Processor;
import org.mokai.spi.type.ProcessorType;

public class DefaultProcessorType implements ProcessorType {

	private static final long serialVersionUID = 374131088144629805L;

	private String name;
	
	private String description;
	
	private Class<? extends Processor> processorClass;
	
	public DefaultProcessorType(String name, String description, 
			Class<? extends Processor> processorClass) {	
		this.name = name;
		this.description = description;
		this.processorClass = processorClass;
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
	public Class<? extends Processor> getProcessorClass() {
		return processorClass;
	}

}
