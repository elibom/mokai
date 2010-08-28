package org.mokai.type;

import java.io.Serializable;

import org.mokai.Receiver;

/**
 * 
 * @author German Escobar
 */
public class ReceiverType implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private Class<? extends Receiver> receiverClass;
	
	public ReceiverType(String name, String description, Class<? extends Receiver> receiverClass) {
		this.name = name;
		this.description = description;
		this.receiverClass = receiverClass;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return description;
	}

	public final Class<? extends Receiver> getReceiverClass() {
		return receiverClass;
	}

	@Override
	public final boolean equals(Object obj) {
		if (ReceiverType.class.isInstance(obj)) {
			ReceiverType rt = (ReceiverType) obj;
			
			return rt.getReceiverClass().equals(receiverClass);
		}
		
		return false;
	}

	@Override
	public final int hashCode() {
		return receiverClass.hashCode();
	}
	
}
