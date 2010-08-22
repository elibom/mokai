package org.mokai.type.impl;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Processor;
import org.mokai.Receiver;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ProcessorType;
import org.mokai.type.ReceiverType;

public class TypeBuilder {

	public static AcceptorType buildAcceptorType(Class<? extends Acceptor> acceptorClass) {
		
		String name = getName(acceptorClass);
		String description = getDescription(acceptorClass);
		
		return new AcceptorType(name, description, acceptorClass);
	}
	
	public static ActionType buildActionType(Class<? extends Action> actionClass) {
		
		String name = getName(actionClass);
		String description = getDescription(actionClass);
		
		return new ActionType(name, description, actionClass);
	}
	
	public static ProcessorType buildProcessorType(Class<? extends Processor> processorClass) {

		String name = getName(processorClass);
		String description = getDescription(processorClass);
		
		return new ProcessorType(name, description, processorClass);
	}
	
	public static ReceiverType buildReceiverType(Class<? extends Receiver> receiverClass) {
		
		String name = getName(receiverClass);
		String description = getDescription(receiverClass);
		
		return new ReceiverType(name, description, receiverClass);
	}
	
	private static String getName(Class<?> clazz) {
		String name = "";	
		if (clazz.isAnnotationPresent(Name.class)) {
			Name nameAnnotation = clazz.getAnnotation(Name.class);
			name = nameAnnotation.value();
		}
		
		return name;
	}
	
	private static String getDescription(Class<?> clazz) {
		String description = "";
		if (clazz.isAnnotationPresent(Description.class)) {
			Description descriptionAnnotation = clazz.getAnnotation(Description.class);
			description = descriptionAnnotation.value();
		}
		
		return description;
	}
}
