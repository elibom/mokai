package org.mokai.type.impl.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.mockito.Mockito;
import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Processor;
import org.mokai.Receiver;
import org.mokai.plugin.PluginMechanism;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ProcessorType;
import org.mokai.type.ReceiverType;
import org.mokai.type.TypeLoader;
import org.mokai.type.impl.PluginTypeLoader;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockAction;
import org.mokai.types.mock.MockConnector;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class PluginTypeLoaderTest {

	@Test
	public void testLoadAcceptorTypes() throws Exception {
		TypeLoader typeLoader = new PluginTypeLoader(mockPluginMechanism());
		
		Set<AcceptorType> acceptorTypes =  typeLoader.loadAcceptorTypes();
		Assert.assertEquals(1, acceptorTypes.size());
		
		AcceptorType acceptorType = acceptorTypes.iterator().next();
		Assert.assertNotNull(acceptorType);
		Assert.assertEquals("MockAcceptor", acceptorType.getName());
		Assert.assertEquals("Mock Acceptor Description", acceptorType.getDescription());
		Assert.assertEquals(MockAcceptor.class, acceptorType.getAcceptorClass());
	}
	
	@Test
	public void testLoadActionTyes() throws Exception {
		TypeLoader typeLoader = new PluginTypeLoader(mockPluginMechanism());
		
		Set<ActionType> actionTypes = typeLoader.loadActionTypes();
		Assert.assertEquals(1,actionTypes.size());
		
		ActionType actionType = actionTypes.iterator().next();
		Assert.assertNotNull(actionType);
		Assert.assertEquals("", actionType.getName());
		Assert.assertEquals("", actionType.getDescription());
		Assert.assertEquals(MockAction.class, actionType.getActionClass());	
	}
	
	@Test
	public void testLoadReceiverTypes() throws Exception {
		TypeLoader typeLoader = new PluginTypeLoader(mockPluginMechanism());
		
		Set<ReceiverType> receiverTypes = typeLoader.loadReceiverTypes();
		Assert.assertEquals(1, receiverTypes.size());
		
		ReceiverType receiverType = receiverTypes.iterator().next();
		Assert.assertNotNull(receiverType);
		Assert.assertEquals("", receiverType.getName());
		Assert.assertEquals("", receiverType.getDescription());
		Assert.assertEquals(MockConnector.class, receiverType.getReceiverClass());
	}
	
	@Test
	public void testLoadProcessorTypes() throws Exception {
		TypeLoader typeLoader = new PluginTypeLoader(mockPluginMechanism());
		
		Set<ProcessorType> processorTypes = typeLoader.loadProcessorTypes();
		Assert.assertEquals(1, processorTypes.size());
		
		ProcessorType processorType = processorTypes.iterator().next();
		Assert.assertNotNull(processorType);
		Assert.assertEquals("", processorType.getName());
		Assert.assertEquals("", processorType.getDescription());
		Assert.assertEquals(MockConnector.class, processorType.getProcessorClass());
	}
	
	private PluginMechanism mockPluginMechanism() {
		PluginMechanism pluginMechanism = Mockito.mock(PluginMechanism.class);
		
		Set<Class<? extends Acceptor>> acceptorClasses = new HashSet<Class<? extends Acceptor>>();
		acceptorClasses.add(MockAcceptor.class);
		
		Mockito
			.when(pluginMechanism.loadTypes(Acceptor.class))
			.thenReturn(acceptorClasses);
		
		Set<Class<? extends Action>> actionClasses = new HashSet<Class<? extends Action>>();
		actionClasses.add(MockAction.class);
		
		Mockito
			.when(pluginMechanism.loadTypes(Action.class))
			.thenReturn(actionClasses);
		
		Set<Class<? extends Receiver>> receiverClasses = new HashSet<Class<? extends Receiver>>();
		receiverClasses.add(MockConnector.class);
		
		Mockito
			.when(pluginMechanism.loadTypes(Receiver.class))
			.thenReturn(receiverClasses);
		
		Set<Class<? extends Processor>> processorClasses = new HashSet<Class<? extends Processor>>();
		processorClasses.add(MockConnector.class);
		
		Mockito
			.when(pluginMechanism.loadTypes(Processor.class))
			.thenReturn(processorClasses);
		
		return pluginMechanism;
	}
}
