package org.mokai.type.impl.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.Processor;
import org.mokai.plugin.PluginMechanism;
import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ConnectorType;
import org.mokai.type.TypeLoader;
import org.mokai.type.impl.PluginTypeLoader;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockAction;
import org.mokai.types.mock.MockConnector;
import org.testng.Assert;
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
		Assert.assertEquals(acceptorTypes.size(), 1);
		
		AcceptorType acceptorType = acceptorTypes.iterator().next();
		Assert.assertNotNull(acceptorType);
		Assert.assertEquals(acceptorType.getName(), "MockAcceptor");
		Assert.assertEquals(acceptorType.getDescription(), "Mock Acceptor Description");
		Assert.assertEquals(acceptorType.getAcceptorClass(), MockAcceptor.class);
	}
	
	@Test
	public void testLoadActionTyes() throws Exception {
		TypeLoader typeLoader = new PluginTypeLoader(mockPluginMechanism());
		
		Set<ActionType> actionTypes = typeLoader.loadActionTypes();
		Assert.assertEquals(1,actionTypes.size());
		
		ActionType actionType = actionTypes.iterator().next();
		Assert.assertNotNull(actionType);
		Assert.assertEquals(actionType.getName(), "");
		Assert.assertEquals(actionType.getDescription(), "");
		Assert.assertEquals(actionType.getActionClass(), MockAction.class);	
	}
	
	@Test
	public void testLoadConnectorsTypes() throws Exception {
		TypeLoader typeLoader = new PluginTypeLoader(mockPluginMechanism());
		
		Set<ConnectorType> processorTypes = typeLoader.loadConnectorTypes();
		Assert.assertEquals(processorTypes.size(), 1);
		
		ConnectorType processorType = processorTypes.iterator().next();
		Assert.assertNotNull(processorType);
		Assert.assertEquals(processorType.getName(), "");
		Assert.assertEquals(processorType.getDescription(), "");
		Assert.assertEquals(processorType.getConnectorClass(), MockConnector.class);
	}
	
	private PluginMechanism mockPluginMechanism() {
		PluginMechanism pluginMechanism = mock(PluginMechanism.class);
		
		Set<Class<? extends Acceptor>> acceptorClasses = new HashSet<Class<? extends Acceptor>>();
		acceptorClasses.add(MockAcceptor.class);
		
		when(pluginMechanism.loadTypes(Acceptor.class))
			.thenReturn(acceptorClasses);
		
		Set<Class<? extends Action>> actionClasses = new HashSet<Class<? extends Action>>();
		actionClasses.add(MockAction.class);
		
		when(pluginMechanism.loadTypes(Action.class))
			.thenReturn(actionClasses);
		
		Set<Class<? extends Connector>> receiverClasses = new HashSet<Class<? extends Connector>>();
		receiverClasses.add(MockConnector.class);
		
		when(pluginMechanism.loadTypes(Connector.class))
			.thenReturn(receiverClasses);
		
		Set<Class<? extends Processor>> processorClasses = new HashSet<Class<? extends Processor>>();
		processorClasses.add(MockConnector.class);
		
		when(pluginMechanism.loadTypes(Processor.class))
			.thenReturn(processorClasses);
		
		return pluginMechanism;
	}
}
