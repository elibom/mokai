package org.mokai.config.xml.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mokai.Action;
import org.mokai.Receiver;
import org.mokai.ReceiverService;
import org.mokai.RoutingEngine;
import org.mokai.config.ConfigurationException;
import org.mokai.config.xml.ReceiverConfiguration;
import org.mokai.plugin.PluginMechanism;
import org.mokai.types.mock.MockAction;
import org.mokai.types.mock.MockConfigurableAction;
import org.mokai.types.mock.MockConfigurableConnector;
import org.mokai.types.mock.MockConnector;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ReceiverConfigurationTest {

	@Test
	public void testLoadGoodFile() throws Exception {
		testGoodFile(null);
	}
	
	@Test
	public void testLoadGoodFileNotUsefulPluginMechanism() throws Exception {
		PluginMechanism pluginMechanism = Mockito.mock(PluginMechanism.class);
		testGoodFile(pluginMechanism);
	}
	
	private void testGoodFile(PluginMechanism pluginMechanism) throws Exception {
		String path = "src/test/resources/receivers-test/good-receivers.xml";
		
		ReceiverService receiverService1 = Mockito.mock(ReceiverService.class);
		ReceiverService receiverService2 = Mockito.mock(ReceiverService.class);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.createReceiver(Mockito.eq("test-1"), Mockito.any(Receiver.class)))
			.thenReturn(receiverService1);
		Mockito
			.when(routingEngine.createReceiver(Mockito.eq("test-2"), Mockito.any(Receiver.class)))
			.thenReturn(receiverService2);
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setPath(path);
		config.setRoutingEngine(routingEngine);
		config.setPluginMechanism(pluginMechanism);
		
		config.load();
		
		// check that we have created 2 receivers
		Mockito.verify(routingEngine, Mockito.times(2)).createReceiver(Mockito.anyString(), Mockito.any(Receiver.class));
				
		Mockito.verify(receiverService2).addPostReceivingAction(Mockito.any(Action.class));
		
		Action testAction = new MockConfigurableAction("test", 2);
		Mockito.verify(receiverService2).addPostReceivingAction(testAction);
	}
	
	@Test
	public void testLoadFileWithPluginMechanism() throws Exception {
		String path = "src/test/resources/receivers-test/plugin-receivers.xml";
		
		ReceiverService receiverService = Mockito.mock(ReceiverService.class);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.createReceiver(Mockito.anyString(), Mockito.any(Receiver.class)))
			.thenReturn(receiverService);
		
		PluginMechanism pluginMechanism = mockPluginMechanism();
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setPath(path);
		config.setRoutingEngine(routingEngine);
		config.setPluginMechanism(pluginMechanism);
		
		config.load();
		
		Mockito.verify(pluginMechanism).loadClass(Mockito.endsWith("MockConnector"));
		Mockito.verify(pluginMechanism).loadClass(Mockito.endsWith("MockAction"));
		
	}
	
	private PluginMechanism mockPluginMechanism() {
		PluginMechanism pluginMechanism = Mockito.mock(PluginMechanism.class);
		
		Mockito
			.when(pluginMechanism.loadClass(Mockito.endsWith("MockConnector")))
			.thenAnswer(new Answer<Class<?>>() {

				@Override
				public Class<?> answer(InvocationOnMock invocation)
						throws Throwable {
					return MockConnector.class;
				}
				
			});
		
		Mockito
		.when(pluginMechanism.loadClass(Mockito.endsWith("MockAction")))
		.thenAnswer(new Answer<Class<?>>() {

			@Override
			public Class<?> answer(InvocationOnMock invocation)
					throws Throwable {
				return MockAction.class;
			}
			
		});
		
		return pluginMechanism;
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadEmptyFile() throws Exception {
		String path = "src/test/resources/receivers-test/empty-receivers.xml";
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setPath(path);
		config.setRoutingEngine(routingEngine);
		
		config.load();
		
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadNonExistingFile() throws Exception {
		String path = "src/test/resources/receivers-test/nonexisting-receivers.xml";
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setPath(path);
		config.setRoutingEngine(routingEngine);
		
		config.load();
		
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadNonExistentClasses() throws Exception {
		String path = "src/test/resources/receivers-test/invalid-receivers.xml";

		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setPath(path);
		config.setRoutingEngine(routingEngine);
		
		config.load();
	}
	
	@AfterMethod
	public void deleteTempReceiversFolder() throws Exception {
		String path = "src/test/resources/temp-receivers/";
		
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
		String path = "src/test/resources/temp-receivers/receivers.xml";
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getReceivers())
			.thenReturn(new ArrayList<ReceiverService>());
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("receivers", rootElement.getName());
				
				// validate no receivers
				Assert.assertEquals(0, rootElement.elements("receiver").size());
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveOneReceiverNoActions() throws Exception {
		String path = "src/test/resources/temp-receivers/receivers.xml";
		
		ReceiverService receiverService = mockReceiverService("test", new MockConnector(), new ArrayList<Action>());
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getReceivers())
			.thenReturn(Collections.singleton(receiverService));
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("receivers", rootElement.getName());
				
				// validate receiver
				Assert.assertEquals(1, rootElement.elements("receiver").size());
				Element receiverElement = rootElement.element("receiver");
				Assert.assertEquals(1, receiverElement.attributeCount());
				Assert.assertEquals("test", receiverElement.attributeValue("id"));
				
				// validate connector
				Assert.assertEquals(1, receiverElement.elements("connector").size());
				Element connectorElement = receiverElement.element("connector");
				Assert.assertEquals(1, connectorElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConnector", connectorElement.attributeValue("className"));
				
				// validate no actions
				Assert.assertEquals(0, receiverElement.elements("post-receiving-actions").size());
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveOneReceiverMultipleActions() throws Exception {
		String path = "src/test/resources/temp-receivers/receivers.xml";
		
		List<Action> actions = new ArrayList<Action>();
		actions.add(new MockAction());
		actions.add(new MockAction());
		
		ReceiverService receiverService = mockReceiverService("test", new MockConnector(), actions);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getReceivers())
			.thenReturn(Collections.singleton(receiverService));
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("receivers", rootElement.getName());
				
				// validate receiver
				Assert.assertEquals(1, rootElement.elements("receiver").size());
				Element receiverElement = rootElement.element("receiver");
				
				// validate multiple actions
				Assert.assertEquals(1, receiverElement.elements("post-receiving-actions").size());
				Element actionsElement = receiverElement.element("post-receiving-actions");
				Assert.assertEquals(2, actionsElement.elements("action").size());
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveMultipleReceivers() throws Exception {
		String path = "src/test/resources/temp-receivers/receivers.xml";
		
		List<ReceiverService> receiverServices = new ArrayList<ReceiverService>();
		receiverServices.add(mockReceiverService("test1", new MockConnector(), new ArrayList<Action>()));
		receiverServices.add(mockReceiverService("test2", new MockConnector(), new ArrayList<Action>()));
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getReceivers())
			.thenReturn(receiverServices);
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("receivers", rootElement.getName());
				
				// validate receiver
				Assert.assertEquals(2, rootElement.elements("receiver").size());
			}
			
		}.validate();
	}
	
	@Test
	public void testSaveComplexDocument() throws Exception {
		String path = "src/test/resources/temp-receivers/receivers.xml";
		
		List<Action> actions = new ArrayList<Action>();
		actions.add(new MockConfigurableAction("test", 1));

		ReceiverService receiverService = mockReceiverService("test", 
				new MockConfigurableConnector("test", 2), actions);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getReceivers())
			.thenReturn(Collections.singletonList(receiverService));
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@SuppressWarnings("unchecked")
			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("receivers", rootElement.getName());
				
				// validate receiver
				Assert.assertEquals(1, rootElement.elements("receiver").size());
				Element receiverElement = rootElement.element("receiver");
				Assert.assertEquals(1, receiverElement.attributeCount());
				Assert.assertEquals("test", receiverElement.attributeValue("id"));
				
				// validate connector
				Assert.assertEquals(1, receiverElement.elements("connector").size());
				Element connectorElement = receiverElement.element("connector");
				Assert.assertEquals(1, connectorElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConfigurableConnector", 
						connectorElement.attributeValue("className"));
				
				// validate connector properties
				Assert.assertEquals(2, connectorElement.elements("property").size());
				Iterator connectorProperties = connectorElement.elements("property").iterator();
				while (connectorProperties.hasNext()) {
					Element propertyElement = (Element) connectorProperties.next();
					Assert.assertEquals(1, propertyElement.attributeCount());
					
					String propertyName = propertyElement.attributeValue("name");
					String propertyValue = propertyElement.getText();
					Assert.assertTrue(propertyName.equals("config1") || propertyName.equals("config2"));
					Assert.assertTrue(propertyValue.equals("test") || propertyValue.equals("2"));					
				}
				
				// validate actions
				Assert.assertEquals(1, receiverElement.elements("post-receiving-actions").size());
				Element actionsElement = receiverElement.element("post-receiving-actions");
				Assert.assertEquals(1, actionsElement.elements("action").size());
				
				// validate actions properties
				Element actionElement = actionsElement.element("action");
				Assert.assertEquals(2, actionElement.elements("property").size());	
				Iterator actionProperties = actionElement.elements("property").iterator();
				while (actionProperties.hasNext()) {
					Element propertyElement = (Element) actionProperties.next();
					Assert.assertEquals(1, propertyElement.attributeCount());
					
					String propertyName = propertyElement.attributeValue("name");
					String propertyValue = propertyElement.getText();
					Assert.assertTrue(propertyName.equals("config1") || propertyName.equals("config2"));
					Assert.assertTrue(propertyValue.equals("test") || propertyValue.equals("1"));					
				}				
			}
			
		}.validate();
	}
	
	private ReceiverService mockReceiverService(String id, Receiver receiver, List<Action> actions) {
		ReceiverService receiverService = Mockito.mock(ReceiverService.class);
		
		Mockito
			.when(receiverService.getId())
			.thenReturn(id);
		
		Mockito
			.when(receiverService.getReceiver())
			.thenReturn(receiver);

		Mockito
			.when(receiverService.getPostReceivingActions())
			.thenReturn(actions);
		
		return receiverService;
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

}
