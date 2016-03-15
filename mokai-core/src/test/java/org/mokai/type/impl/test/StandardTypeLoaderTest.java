package org.mokai.type.impl.test;

import java.util.Set;


import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ConnectorType;
import org.mokai.type.impl.StandardTypeLoader;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockAction;
import org.mokai.types.mock.MockConnector;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StandardTypeLoaderTest {

	@Test
	public void testLoadAcceptorTypes() throws Exception {
		StandardTypeLoader typeLoader = new StandardTypeLoader();

		Set<AcceptorType> acceptorTypes = typeLoader.loadAcceptorTypes();
		Assert.assertTrue(acceptorTypes.size() > 0);

		AcceptorType test = new AcceptorType("", "", MockAcceptor.class);
		Assert.assertTrue(acceptorTypes.contains(test));
	}

	@Test
	public void testLoadActionTypes() throws Exception {
		StandardTypeLoader typeLoader = new StandardTypeLoader();

		Set<ActionType> actionTypes = typeLoader.loadActionTypes();
		Assert.assertTrue(actionTypes.size() > 0);

		ActionType test = new ActionType("", "", MockAction.class);
		Assert.assertTrue(actionTypes.contains(test));
	}

	@Test
	public void testLoadConnectorTypes() throws Exception {
		StandardTypeLoader typeLoader = new StandardTypeLoader();

		Set<ConnectorType> connectorTypes = typeLoader.loadConnectorTypes();
		Assert.assertTrue(connectorTypes.size() > 0);

		ConnectorType test = new ConnectorType("", "", MockConnector.class);
		Assert.assertTrue(connectorTypes.contains(test));
	}

}
