package org.mokai.config.xml.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.Processor;
import org.mokai.acceptor.AndAcceptor;
import org.mokai.acceptor.OrAcceptor;
import org.mokai.config.ConfigurationException;
import org.mokai.config.xml.AbstractConfiguration;
import org.mokai.plugin.PluginMechanism;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockAcceptorWithAcceptor;
import org.mokai.types.mock.MockAction;
import org.mokai.types.mock.MockConfigurableAcceptor;
import org.mokai.types.mock.MockConfigurableAction;
import org.mokai.types.mock.MockConfigurableConnector;
import org.mokai.types.mock.MockConnector;
import org.mokai.types.mock.MockConnectorWithConnectors;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ConfigurationTest {

	@Test
	public void testLoadGoodFile() throws Exception {
		String path = "src/test/resources/config-test/good-connectors.xml";
		testGoodFile(path, null);		
	}
	
	@Test
	public void testLoadFileWithNotUsefulPluginMechanism() throws Exception {
		String path = "src/test/resources/config-test/good-connectors.xml";
		
		PluginMechanism pluginMechanism = mock(PluginMechanism.class);
		testGoodFile(path, pluginMechanism);
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailWithBadSchema() throws Exception {
		String path = "src/test/resources/config-test/badschema-connectors.xml";
		
		testGoodFile(path, null);
	}
	
	private void testGoodFile(String path, PluginMechanism pluginMechanism) throws Exception {
		
		ConnectorService connectorService1 = mock(ConnectorService.class);
		ConnectorService connectorService2 = mock(ConnectorService.class);
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		when(delegator.addConnector(eq("test-1"), any(Connector.class)))
				.thenAnswer(new ConnectorServiceAnswer(connectorService1, 500));
		when(delegator.addConnector(eq("test-2"), any(Processor.class)))
				.thenAnswer(new ConnectorServiceAnswer(connectorService2, 500));
	
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPluginMechanism(pluginMechanism);
		config.setPath(path);
		
		config.load();
		
		// check that we have created two connectors
		verify(delegator).addConnector(eq("test-1"), eq(new MockConfigurableConnector("test1", 3)));
		verify(delegator).addConnector(eq("test-2"), eq(new MockConfigurableConnector("test2", 5)));
		
		// check that the maxConcurrentMsgs to the ConnectorService
		verify(connectorService1).setMaxConcurrentMsgs(10);
		verify(connectorService2).setMaxConcurrentMsgs(1);
		
		// check that nothing was added to connector service 1
		verify(connectorService1, never()).addAcceptor(any(Acceptor.class));
		verify(connectorService1, never()).addPreProcessingAction(any(Action.class));
		verify(connectorService1, never()).addPostProcessingAction(any(Action.class));
		verify(connectorService1, never()).addPostReceivingAction(any(Action.class));
		
		verify(connectorService2, times(2)).addAcceptor(any(Acceptor.class));
		verify(connectorService2).addPreProcessingAction(new MockConfigurableAction("t1", 1));
		verify(connectorService2).addPostProcessingAction(new MockConfigurableAction("t2", 2));
		verify(connectorService2).addPostReceivingAction(new MockConfigurableAction("t3", 3));
	}

	@Test
	public void testLoadFileWithPluginMechanism() throws Exception {
		String path = "src/test/resources/config-test/plugin-connectors.xml";
		
		ConnectorService connectorService = mock(ConnectorService.class);
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		when(delegator.addConnector(anyString(), any(Connector.class)))
			.thenReturn(connectorService); 
		
		PluginMechanism pluginMechanism = mockPluginMechanism();
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPluginMechanism(pluginMechanism);
		config.setPath(path);
		
		config.load();
		
		verify(pluginMechanism).loadClass(endsWith("MockConnector"));
		verify(pluginMechanism).loadClass(endsWith("MockAcceptor"));
		verify(pluginMechanism, times(3)).loadClass(endsWith("MockAction"));
	}
	
	private PluginMechanism mockPluginMechanism() {
		PluginMechanism pluginMechanism = mock(PluginMechanism.class);
		
		when(pluginMechanism.loadClass(endsWith("MockConnector")))
			.thenAnswer(new Answer<Class<?>>() {

				@Override
				public Class<?> answer(InvocationOnMock invocation)
						throws Throwable {
					return MockConnector.class;
				}
				
			});
		
		when(pluginMechanism.loadClass(endsWith("MockAcceptor")))
			.thenAnswer(new Answer<Class<?>>() {

				@Override
				public Class<?> answer(InvocationOnMock invocation)
						throws Throwable {
					return MockAcceptor.class;
				}
				
			});
		
		when(pluginMechanism.loadClass(endsWith("MockAction")))
			.thenAnswer(new Answer<Class<?>>() {

				@Override
				public Class<?> answer(InvocationOnMock invocation)
						throws Throwable {
					return MockAction.class;
				}
				
			});
		
		return pluginMechanism;
	}
	
	@Test
	public void shouldLoadFileWithNestedConnectors() throws Exception {
		String path = "src/test/resources/config-test/nested-connectors-connectors.xml";
		
		ConfigDelegator delegator = new ConfigDelegatorImpl();
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
		
		List<ConnectorService> connectorServices = delegator.getConnectors();
		Assert.assertNotNull(connectorServices);
		Assert.assertEquals(connectorServices.size(), 1);
		
		ConnectorService connectorService = connectorServices.iterator().next();
		Assert.assertNotNull(connectorService);
		
		MockConnectorWithConnectors connector = (MockConnectorWithConnectors) connectorService.getConnector();
		Assert.assertNotNull(connector);
		
		Assert.assertNotNull(connector.getConnector());
		Assert.assertEquals(connector.getListConnectors().size(), 1);
		Assert.assertEquals(connector.getMapConnectors().size(), 1);
		
		Connector innerConnector = connector.getMapConnectors().get("test-1");
		Assert.assertNotNull(innerConnector);
		Assert.assertEquals(innerConnector.getClass(), MockConfigurableConnector.class);

		Assert.assertEquals(((MockConfigurableConnector) innerConnector).getConfig1(), "config1");
		Assert.assertEquals(((MockConfigurableConnector) innerConnector).getConfig2(), 2);
		
	}
	
	@Test
	public void shouldLoadFileWithNestedAcceptors() throws Exception {
		String path = "src/test/resources/config-test/nested-acceptors-connectors.xml";

		ConfigDelegator delegator = new ConfigDelegatorImpl();
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
		
		List<ConnectorService> connectorServices = delegator.getConnectors();
		Assert.assertNotNull(connectorServices);
		Assert.assertEquals(connectorServices.size(), 1);
		
		ConnectorService connectorService = connectorServices.iterator().next();
		Assert.assertNotNull(connectorService);
		
		List<Acceptor> acceptors = connectorService.getAcceptors();
		Assert.assertNotNull(acceptors);
		Assert.assertEquals(acceptors.size(), 1);
		
		MockAcceptorWithAcceptor acceptor = (MockAcceptorWithAcceptor) acceptors.iterator().next();
		Assert.assertNotNull(acceptor);
		Assert.assertNotNull(acceptor.getAcceptor());
		
		Assert.assertEquals(acceptor.getListAcceptors().size(), 1);
		Assert.assertEquals(acceptor.getMapAcceptors().size(), 1);
		
	}
	
	@Test
	public void shouldLoadFileWithAndAcceptors() throws Exception {
		String path = "src/test/resources/config-test/and-acceptors-connectors.xml";

		ConfigDelegator delegator = new ConfigDelegatorImpl();
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
		
		List<ConnectorService> connectorServices = delegator.getConnectors();
		Assert.assertNotNull(connectorServices);
		Assert.assertEquals(connectorServices.size(), 1);
		
		ConnectorService connectorService = connectorServices.iterator().next();
		Assert.assertNotNull(connectorService);
		
		List<Acceptor> acceptors = connectorService.getAcceptors();
		Assert.assertNotNull(acceptors);
		Assert.assertEquals(acceptors.size(), 3);
		
		Acceptor acceptor0 = acceptors.get(0);
		Assert.assertNotNull(acceptor0);
		Assert.assertEquals(acceptor0.getClass(), AndAcceptor.class);
		Assert.assertEquals(((AndAcceptor) acceptor0).getAcceptors().size(), 2); 
		
		Acceptor acceptor1 = acceptors.get(1);
		Assert.assertNotNull(acceptor1);
		Assert.assertEquals(acceptor1.getClass(), AndAcceptor.class);
		Assert.assertEquals(((AndAcceptor) acceptor1).getAcceptors().size(), 2);
		
		Acceptor acceptor2 = acceptors.get(2);
		Assert.assertNotNull(acceptor2);
		Assert.assertEquals(acceptor2.getClass(), MockAcceptor.class);
	}
	
	@Test
	public void shouldLoadFileWithNestedAndOrAcceptors() throws Exception {
		String path = "src/test/resources/config-test/nested-and-or-connectors.xml";

		ConfigDelegator delegator = new ConfigDelegatorImpl();
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
			
		List<ConnectorService> connectorServices = delegator.getConnectors();
		Assert.assertNotNull(connectorServices);
		Assert.assertEquals(connectorServices.size(), 1);
		
		ConnectorService connectorService = connectorServices.iterator().next();
		Assert.assertNotNull(connectorService);
		
		List<Acceptor> acceptors = connectorService.getAcceptors();
		Assert.assertNotNull(acceptors);
		Assert.assertEquals(acceptors.size(), 2);
		
		Acceptor acceptor0 = acceptors.get(0);
		Assert.assertNotNull(acceptor0);
		Assert.assertEquals(acceptor0.getClass(), AndAcceptor.class);
		Assert.assertEquals(((AndAcceptor) acceptor0).getAcceptors().size(), 2); 
		
		Iterator<Acceptor> andAcceptors = ((AndAcceptor) acceptor0).getAcceptors().iterator();
		boolean hasOrAcceptor = false;
		while (andAcceptors.hasNext()) {
			Acceptor a = andAcceptors.next();
			Assert.assertTrue( OrAcceptor.class.isInstance(a) || MockAcceptor.class.isInstance(a));
			
			if (OrAcceptor.class.isInstance(a)) {
				hasOrAcceptor = true;
				Assert.assertEquals( ((OrAcceptor) a).getAcceptors().size(), 2);
			}
		}
		Assert.assertTrue(hasOrAcceptor);
		
		Acceptor acceptor1 = acceptors.get(1);
		Assert.assertNotNull(acceptor1);
		Assert.assertEquals(acceptor1.getClass(), OrAcceptor.class);
		Assert.assertEquals(((OrAcceptor) acceptor1).getAcceptors().size(), 2);
		
		Iterator<Acceptor> orAcceptors = ((OrAcceptor) acceptor1).getAcceptors().iterator();
		boolean hasAndAcceptor = false;
		while (orAcceptors.hasNext()) {
			Acceptor a = orAcceptors.next();
			Assert.assertTrue( AndAcceptor.class.isInstance(a) || MockAcceptor.class.isInstance(a) );
			
			if (AndAcceptor.class.isInstance(a)) {
				hasAndAcceptor = true;
				Assert.assertEquals( ((AndAcceptor) a).getAcceptors().size(), 2 );
			}
			
		}
		Assert.assertTrue(hasAndAcceptor);
		
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadEmptyFile() throws Exception {
		String path = "src/test/resources/config-test/empty-connectors.xml";
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadNonExistingFile() throws Exception {
		String path = "src/test/resources/config-test/nonexisting-connectors.xml";
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadNonExistentClasses() throws Exception {
		String path = "src/test/resources/config-test/nonexistentclass-connectors.xml";

		ConfigDelegator delegator = mock(ConfigDelegator.class);
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailWithInvalidFile() throws Exception {
		String path = "src/test/resources/config-test/invalid-connectors.xml";
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.load();
	}
	
	@AfterMethod
	public void deleteTempConnectorsFolder() throws Exception {
		String path = "src/test/resources/temp-connectors/";
		
		File dir = new File(path);
		
		if (dir.exists()) {
			String[] children = dir.list();
	        for (int i=0; i < children.length; i++) {
	        	File file = new File(dir, children[i]);
	            file.delete();
	        }
	        
	        dir.delete();
		}
		
	}
	
	@Test
	public void testSaveEmptyDocument() throws Exception {
		String path = "src/test/resources/temp-connectors/connectors.xml";
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		when(delegator.getConnectors())
			.thenReturn(new ArrayList<ConnectorService>());
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals(rootElement.getName(), "connectors");
				
				// validate no receivers
				Assert.assertEquals(rootElement.elements("connector").size(), 0);
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveOneConnectorNoAdditionals() throws Exception {
		String path = "src/test/resources/temp-connectors/connectors.xml";
		
		ConnectorService processorService = mockConnectorService("test", 1000, 
				new MockConnector(), new ArrayList<Acceptor>(), new ArrayList<Action>(), 
				new ArrayList<Action>(), new ArrayList<Action>());
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		when(delegator.getConnectors())
			.thenReturn(Collections.singletonList(processorService));
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals(rootElement.getName(), "connectors");
				
				// validate connector
				Assert.assertEquals(rootElement.elements("connector").size(), 1);
				Element connectorElement = rootElement.element("connector");
				Assert.assertEquals(connectorElement.attributeCount(), 3);
				Assert.assertEquals(connectorElement.attributeValue("id"), "test");
				Assert.assertEquals(connectorElement.attributeValue("priority"), "1000");
				Assert.assertEquals(connectorElement.attributeValue("className"), "org.mokai.types.mock.MockConnector");
				
				// validate no additional
				Assert.assertEquals(connectorElement.elements("acceptors").size(), 0);
				Assert.assertEquals(connectorElement.elements("pre-processing-actions").size(), 0);
				Assert.assertEquals(connectorElement.elements("post-processing-actions").size(), 0);
				Assert.assertEquals(connectorElement.elements("post-receiving-actions").size(), 0);
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveOneConnectorMultipleAdditionals() throws Exception {
		String path = "src/test/resources/temp-connectors/connectors.xml";
		
		List<Acceptor> acceptors = new ArrayList<Acceptor>();
		acceptors.add(new MockAcceptor());
		acceptors.add(new MockAcceptor());
		
		List<Action> actions = new ArrayList<Action>();
		actions.add(new MockAction());
		actions.add(new MockAction());
		
		ConnectorService connectorService = mockConnectorService("test", 1000, 
				new MockConnector(), acceptors, actions, actions, actions);
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		when(delegator.getConnectors())
			.thenReturn(Collections.singletonList(connectorService));
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals(rootElement.getName(), "connectors");
				
				// validate connector
				Assert.assertEquals(1, rootElement.elements("connector").size());
				Element connectorElement = rootElement.element("connector");
				Assert.assertEquals(3, connectorElement.attributeCount());
				Assert.assertEquals(connectorElement.attributeValue("id"), "test");
				Assert.assertEquals(connectorElement.attributeValue("priority"), "1000");
				Assert.assertEquals(connectorElement.attributeValue("className"), "org.mokai.types.mock.MockConnector");
				
				// validate additional
				Assert.assertEquals(connectorElement.elements("acceptors").size(), 1);
				Element acceptorsElement = connectorElement.element("acceptors");
				Assert.assertEquals(acceptorsElement.elements("acceptor").size(), 2);
				
				Assert.assertEquals(connectorElement.elements("pre-processing-actions").size(), 1);
				Element preProcessingActionsElement = connectorElement.element("pre-processing-actions");
				Assert.assertEquals(preProcessingActionsElement.elements("action").size(), 2);
				
				Assert.assertEquals(connectorElement.elements("post-processing-actions").size(), 1);
				Element postProcessingActionsElement = connectorElement.element("post-processing-actions");
				Assert.assertEquals(postProcessingActionsElement.elements("action").size(), 2);
				
				Assert.assertEquals(connectorElement.elements("post-receiving-actions").size(), 1);
				Element postReceivingActionsElement = connectorElement.element("post-receiving-actions");
				Assert.assertEquals(postReceivingActionsElement.elements("action").size(), 2);
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveMultipleConnectors() throws Exception {
		String path = "src/test/resources/temp-connectors/connectors.xml";
		
		List<ConnectorService> connectors = new ArrayList<ConnectorService>();
		connectors.add(mockConnectorService("test-1", 1000, new MockConnector(), new ArrayList<Acceptor>(), 
				new ArrayList<Action>(), new ArrayList<Action>(), new ArrayList<Action>()));
		connectors.add(mockConnectorService("test-2", 2000, new MockConnector(), new ArrayList<Acceptor>(), 
				new ArrayList<Action>(), new ArrayList<Action>(), new ArrayList<Action>()));
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		when(delegator.getConnectors()).thenReturn(connectors);
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals(rootElement.getName(), "connectors");
				
				// validate receiver
				Assert.assertEquals(rootElement.elements("connector").size(), 2);
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveComplexDocument() throws Exception {
		String path = "src/test/resources/temp-connectors/connectors.xml";
		
		List<Acceptor> acceptors = new ArrayList<Acceptor>();
		acceptors.add(new MockConfigurableAcceptor("test1", 1));
		
		List<Action> actions = new ArrayList<Action>();
		actions.add(new MockConfigurableAction("test1", 1));
		
		ConnectorService connectorService = mockConnectorService("test", 1000, new MockConfigurableConnector("test", 0), 
				acceptors, actions, actions, actions);
		
		ConfigDelegator delegator = mock(ConfigDelegator.class);
		when(delegator.getConnectors())
			.thenReturn(Collections.singletonList(connectorService));
		
		MockConfiguration config = new MockConfiguration(delegator);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals(rootElement.getName(), "connectors");
				
				// validate connector
				Assert.assertEquals(rootElement.elements("connector").size(), 1);
				Element connectorElement = rootElement.element("connector");
				Assert.assertEquals(connectorElement.attributeCount(), 3);
				Assert.assertEquals(connectorElement.attributeValue("id"), "test");
				Assert.assertEquals(connectorElement.attributeValue("priority"), "1000");
				Assert.assertEquals(connectorElement.attributeValue("className"), "org.mokai.types.mock.MockConfigurableConnector");
				
				Element configurationElement = connectorElement.element("configuration");
				validateProperties(configurationElement, "test", "0");
				
				// validate acceptors
				Assert.assertEquals(connectorElement.elements("acceptors").size(), 1);
				Element acceptorsElement = connectorElement.element("acceptors");
				
				Assert.assertEquals(acceptorsElement.elements("acceptor").size(), 1);
				Element acceptorElement = acceptorsElement.element("acceptor");
				Assert.assertEquals(acceptorElement.attributeCount(), 1);
				Assert.assertEquals(acceptorElement.attributeValue("className"), "org.mokai.types.mock.MockConfigurableAcceptor");
				validateProperties(acceptorElement, "test1", "1");
				
				// validate pre-processing-actions
				Assert.assertEquals(connectorElement.elements("pre-processing-actions").size(), 1);
				Element preProcessingActionsElement = connectorElement.element("pre-processing-actions");
				
				Assert.assertEquals(preProcessingActionsElement.elements("action").size(), 1);
				Element preProcessingActionElement = preProcessingActionsElement.element("action");
				Assert.assertEquals(preProcessingActionElement.attributeCount(), 1);
				Assert.assertEquals(preProcessingActionElement.attributeValue("className"), "org.mokai.types.mock.MockConfigurableAction");
				validateProperties(preProcessingActionElement, "test1", "1");
				
				// validate post-processing-actions
				Assert.assertEquals(connectorElement.elements("post-processing-actions").size(), 1);
				Element postProcessingActionsElement = connectorElement.element("post-processing-actions");
				
				Assert.assertEquals(postProcessingActionsElement.elements("action").size(), 1);
				Element postProcessingActionElement = postProcessingActionsElement.element("action");
				Assert.assertEquals(postProcessingActionElement.attributeCount(), 1);
				Assert.assertEquals(postProcessingActionElement.attributeValue("className"), "org.mokai.types.mock.MockConfigurableAction");
				validateProperties(postProcessingActionElement, "test1", "1");
				
				// validate post-receiving-actions
				Assert.assertEquals(connectorElement.elements("post-receiving-actions").size(), 1);
				Element postReceivingActionsElement = connectorElement.element("post-receiving-actions");
				
				Assert.assertEquals(postReceivingActionsElement.elements("action").size(), 1);
				Element postReceivingActionElement = postReceivingActionsElement.element("action");
				Assert.assertEquals(postReceivingActionElement.attributeCount(), 1);
				Assert.assertEquals(postReceivingActionElement.attributeValue("className"), "org.mokai.types.mock.MockConfigurableAction");
				validateProperties(postReceivingActionElement, "test1", "1");
			}
			
		}.validate();
		
	}
	
	@SuppressWarnings("rawtypes")
	private void validateProperties(Element parentElement, String config1, String config2) {
		// validate connector properties
		Assert.assertEquals(parentElement.elements("property").size(), 2);
		Iterator connectorProperties = parentElement.elements("property").iterator();
		while (connectorProperties.hasNext()) {
			Element propertyElement = (Element) connectorProperties.next();
			Assert.assertEquals(propertyElement.attributeCount(), 1);
			
			String propertyName = propertyElement.attributeValue("name");
			String propertyValue = propertyElement.getText();
			Assert.assertTrue(propertyName.equals("config1") || propertyName.equals("config2"));
			Assert.assertTrue(propertyValue.equals(config1) || propertyValue.equals(config2));					
		}
	}
	
	private class MockConfiguration extends AbstractConfiguration {
		
		private ConfigDelegator delegator;
		
		public MockConfiguration(ConfigDelegator delegator) {
			this.delegator = delegator;
		}

		@Override
		protected String getDefaultPath() {
			return null;
		}

		@Override
		protected ConnectorService addConnector(String id, Connector connector) {
			return delegator.addConnector(id, connector);
		}

		@Override
		protected List<ConnectorService> getConnectors() {
			return delegator.getConnectors();
		}
		
	}
	
	private interface ConfigDelegator {
		
		ConnectorService addConnector(String id, Connector connector);
		
		List<ConnectorService> getConnectors();
	}
	
	private class ConfigDelegatorImpl implements ConfigDelegator {
		
		private List<ConnectorService> connectorServices = new ArrayList<ConnectorService>();

		@Override
		public ConnectorService addConnector(String id, Connector connector) {
			ConnectorService connectorService = new MockConnectorService(id, connector);
			connectorServices.add(connectorService);
			return connectorService;
		}

		@Override
		public List<ConnectorService> getConnectors() {
			return connectorServices;
		}
		
	}
	
	private ConnectorService mockConnectorService(String id, int priority, Processor processor, 
			List<Acceptor> acceptors, List<Action> preProcessingActions, 
			List<Action> postProcessingActions, List<Action> postReceivingActions) {
		
		ConnectorService processorService = mock(ConnectorService.class);
		
		when(processorService.getId()).thenReturn(id);
		when(processorService.getPriority()).thenReturn(priority);
		
		when(processorService.getConnector()).thenReturn(processor);
		
		when(processorService.getAcceptors()).thenReturn(acceptors);
		
		when(processorService.getPreProcessingActions()).thenReturn(preProcessingActions);
		
		when(processorService.getPostProcessingActions()).thenReturn(postProcessingActions);
		
		when(processorService.getPostReceivingActions()).thenReturn(postReceivingActions);
		
		return processorService;
	}
	
	private abstract class ValidateDoc {
		
		private File file;
		
		public ValidateDoc(File file) {
			this.file = file;
		}
		
		public void validate() throws Exception {
			// load the file and validate
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				SAXReader reader = new SAXReader();
				Document document = reader.read(inputStream);
				
				// check root element
				Element rootElement = document.getRootElement();
				validate(rootElement);
			} finally {
				if (inputStream != null) {
					try { inputStream.close(); } catch (Exception e) {}
				}
			}
		}
		
		public abstract void validate(Element rootElement);
	}
	
	private class ConnectorServiceAnswer implements Answer<ConnectorService> {
		
		private ConnectorService connectorService;
		private long delay;
		
		public ConnectorServiceAnswer(ConnectorService connectorService, long delay) {
			this.connectorService = connectorService;
			this.delay = delay;
		}

		@Override
		public ConnectorService answer(InvocationOnMock invocation) throws Throwable {
			Thread.sleep(delay);
			return connectorService;
		}
		
	}
	
}
