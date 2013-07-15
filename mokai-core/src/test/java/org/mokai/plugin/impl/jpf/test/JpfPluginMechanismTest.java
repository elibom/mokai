package org.mokai.plugin.impl.jpf.test;

import java.util.Set;

import junit.framework.Assert;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.plugin.jpf.JpfPluginMechanism;
import org.testng.annotations.Test;

/**
 *
 * @author German Escobar
 */
public class JpfPluginMechanismTest {

	private final String PLUGINS_PATH = "src/test/resources/plugins-test/";

	@Test
	public void testLoadNonExistentType() throws Exception {
		JpfPluginMechanism pluginMechanism = new JpfPluginMechanism(PLUGINS_PATH);
		pluginMechanism.configure();

		// check that null is returned when we try to load an unexistent class
		Class<?> nonExistentClass = pluginMechanism.loadClass("non.existent.class");
		Assert.assertNull(nonExistentClass);

		pluginMechanism.destroy();
	}

	@Test
	public void testLoadAcceptorTypes() throws Exception {
		JpfPluginMechanism pluginMechanism = new JpfPluginMechanism(PLUGINS_PATH);
		pluginMechanism.configure();

		// check that we can load the class
		Class<?> acceptorClass = pluginMechanism.loadClass("org.mokai.acceptor.TestAcceptor");
		Assert.assertNotNull(acceptorClass);

		// check that we find the acceptor class in the test plugin
		Set<Class<? extends Acceptor>> acceptorTypes = pluginMechanism.loadTypes(Acceptor.class);
		Assert.assertEquals(1, acceptorTypes.size());
		Assert.assertEquals(acceptorClass, acceptorTypes.iterator().next());

		pluginMechanism.destroy();
	}

	@Test
	public void testLoadActionTypes() throws Exception {
		JpfPluginMechanism pluginMechanism = new JpfPluginMechanism(PLUGINS_PATH);
		pluginMechanism.configure();

		// check that we can load the class
		Class<?> actionClass = (Class<?>) pluginMechanism.loadClass("org.mokai.action.TestAction");
		Assert.assertNotNull(actionClass);

		// check that we find the action class in the test plugin
		Set<Class<? extends Action>> actionTypes = pluginMechanism.loadTypes(Action.class);
		Assert.assertEquals(1, actionTypes.size());
		Assert.assertEquals(actionClass, actionTypes.iterator().next());

		pluginMechanism.destroy();
	}

	@Test
	public void testLoadConnectorTypes() throws Exception {
		JpfPluginMechanism pluginMechanism = new JpfPluginMechanism(PLUGINS_PATH);
		pluginMechanism.configure();

		// check that we can load the class
		Class<?> receiverClass = (Class<?>) pluginMechanism.loadClass("org.mokai.connector.TestConnector");
		Assert.assertNotNull(receiverClass);

		// check that we find the receiver class in the test plugin
		Set<Class<? extends Connector>> receiverTypes = pluginMechanism.loadTypes(Connector.class);
		Assert.assertEquals(1, receiverTypes.size());
		Assert.assertEquals(receiverClass, receiverTypes.iterator().next());

		pluginMechanism.destroy();
	}

}
