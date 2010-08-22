package org.mokai.type;

import java.io.Serializable;

import org.mokai.Processor;

/**
 * 
 * @author German Escobar
 */
public class ProcessorType implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private Class<? extends Processor> processorClass;
	
	public ProcessorType(String name, String description, Class<? extends Processor> processorClass) {
		this.name = name;
		this.description = description;
		this.processorClass = processorClass;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Class<? extends Processor> getProcessorClass() {
		return processorClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (ProcessorType.class.isInstance(obj)) {
			ProcessorType pt = (ProcessorType) obj;
			
			return pt.getProcessorClass().equals(processorClass);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return processorClass.hashCode();
	}
	
	
	
}
