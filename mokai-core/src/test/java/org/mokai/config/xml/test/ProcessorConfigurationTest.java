package org.mokai.config.xml.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Processor;
import org.mokai.ProcessorService;
import org.mokai.RoutingEngine;
import org.mokai.config.ConfigurationException;
import org.mokai.config.xml.ProcessorConfiguration;
import org.mokai.config.xml.ReceiverConfiguration;
import org.mokai.plugin.PluginMechanism;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockAction;
import org.mokai.types.mock.MockConfigurableAcceptor;
import org.mokai.types.mock.MockConfigurableAction;
import org.mokai.types.mock.MockConfigurableConnector;
import org.mokai.types.mock.MockConnector;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ProcessorConfigurationTest {

	@Test
	public void testLoadGoodFile() throws Exception {
		String path = "src/test/resources/processors-test/good-processors.xml";
		testGoodFile(path, null);		
	}
	
	@Test
	public void testLoadFileWithNotUsefulPluginMechanism() throws Exception {
		String path = "src/test/resources/processors-test/good-processors.xml";
		
		PluginMechanism pluginMechanism = Mockito.mock(PluginMechanism.class);
		testGoodFile(path, pluginMechanism);
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailWithBadSchema() throws Exception {
		String path = "src/test/resources/processors-test/badschema-processors.xml";
		
		testGoodFile(path, null);
	}
	
	private void testGoodFile(String path, PluginMechanism pluginMechanism) throws Exception {
		
		
		ProcessorService processorService1 = Mockito.mock(ProcessorService.class);
		ProcessorService processorService2 = Mockito.mock(ProcessorService.class);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.createProcessor(Mockito.eq("test-1"), Mockito.anyInt(), Mockito.any(Processor.class)))
			.thenAnswer(new ProcessorServiceAnswer(processorService1, 500));
		Mockito
			.when(routingEngine.createProcessor(Mockito.eq("test-2"), Mockito.anyInt(), Mockito.any(Processor.class)))
			.thenAnswer(new ProcessorServiceAnswer(processorService2, 500));
		
		ThreadPoolExecutor executor = createThreadPool(); 
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPluginMechanism(pluginMechanism);
		config.setPath(path);
		config.setExecutor(executor);
		
		config.load();
		executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
		
		// check that we have created two processors
		Mockito.verify(routingEngine)
			.createProcessor(Mockito.eq("test-1"), Mockito.anyInt(), Mockito.eq(new MockConfigurableConnector("test1", 3)));
		Mockito.verify(routingEngine)
			.createProcessor(Mockito.eq("test-2"), Mockito.anyInt(), Mockito.eq(new MockConfigurableConnector("test2", 5)));
		
		// check that nothing was added to processor service 1
		Mockito.verify(processorService1, Mockito.never()).addAcceptor(Mockito.any(Acceptor.class));
		Mockito.verify(processorService1, Mockito.never()).addPreProcessingAction(Mockito.any(Action.class));
		Mockito.verify(processorService1, Mockito.never()).addPostProcessingAction(Mockito.any(Action.class));
		Mockito.verify(processorService1, Mockito.never()).addPostReceivingAction(Mockito.any(Action.class));
		
		Mockito.verify(processorService2, Mockito.times(2)).addAcceptor(Mockito.any(Acceptor.class));
		Mockito.verify(processorService2).addPreProcessingAction(new MockConfigurableAction("t1", 1));
		Mockito.verify(processorService2).addPostProcessingAction(new MockConfigurableAction("t2", 2));
		Mockito.verify(processorService2).addPostReceivingAction(new MockConfigurableAction("t3", 3));
	}
	
	private ThreadPoolExecutor createThreadPool() {
		return new ThreadPoolExecutor(1, 1, Long.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	@Test
	public void testLoadFileWithPluginMechanism() throws Exception {
		String path = "src/test/resources/processors-test/plugin-processors.xml";
		
		ProcessorService processorService = Mockito.mock(ProcessorService.class);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.createProcessor(Mockito.anyString(), Mockito.anyInt(), Mockito.any(Processor.class)))
			.thenReturn(processorService); 
		
		PluginMechanism pluginMechanism = mockPluginMechanism();
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPluginMechanism(pluginMechanism);
		config.setPath(path);
		
		config.load();
		// we dont need to wait as the plugin mechanism is called before the executor
		
		Mockito.verify(pluginMechanism).loadClass(Mockito.endsWith("MockConnector"));
		Mockito.verify(pluginMechanism).loadClass(Mockito.endsWith("MockAcceptor"));
		Mockito.verify(pluginMechanism, Mockito.times(3)).loadClass(Mockito.endsWith("MockAction"));
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
			.when(pluginMechanism.loadClass(Mockito.endsWith("MockAcceptor")))
			.thenAnswer(new Answer<Class<?>>() {

				@Override
				public Class<?> answer(InvocationOnMock invocation)
						throws Throwable {
					return MockAcceptor.class;
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
		String path = "src/test/resources/processors-test/empty-processors.xml";
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.load();
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadNonExistingFile() throws Exception {
		String path = "src/test/resources/processors-test/nonexisting-processors.xml";
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.load();
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailLoadNonExistentClasses() throws Exception {
		String path = "src/test/resources/processors-test/nonexistentclass-processors.xml";

		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.load();
	}
	
	@Test(expectedExceptions=ConfigurationException.class)
	public void shouldFailWithInvalidFile() throws Exception {
		String path = "src/test/resources/processors-test/invalid-processors.xml";
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		
		ReceiverConfiguration config = new ReceiverConfiguration();
		config.setPath(path);
		config.setRoutingEngine(routingEngine);
		
		config.load();
	}
	
	@AfterMethod
	public void deleteTempProcessorsFolder() throws Exception {
		String path = "src/test/resources/temp-processors/";
		
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
		String path = "src/test/resources/temp-processors/processors.xml";
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getProcessors())
			.thenReturn(new ArrayList<ProcessorService>());
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("processors", rootElement.getName());
				
				// validate no receivers
				Assert.assertEquals(0, rootElement.elements("processor").size());
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveOneProcessorNoAdditionals() throws Exception {
		String path = "src/test/resources/temp-processors/processors.xml";
		
		ProcessorService processorService = mockProcessorService("test", 1000, 
				new MockConnector(), new ArrayList<Acceptor>(), new ArrayList<Action>(), 
				new ArrayList<Action>(), new ArrayList<Action>());
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getProcessors())
			.thenReturn(Collections.singletonList(processorService));
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("processors", rootElement.getName());
				
				// validate processor
				Assert.assertEquals(1, rootElement.elements("processor").size());
				Element processorElement = rootElement.element("processor");
				Assert.assertEquals(2, processorElement.attributeCount());
				Assert.assertEquals("test", processorElement.attributeValue("id"));
				Assert.assertEquals("1000", processorElement.attributeValue("priority"));
				
				// validate connector
				Assert.assertEquals(1, processorElement.elements("connector").size());
				Element connectorElement = processorElement.element("connector");
				Assert.assertEquals(1, connectorElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConnector", connectorElement.attributeValue("className"));
				
				// validate no additional
				Assert.assertEquals(0, processorElement.elements("acceptors").size());
				Assert.assertEquals(0, processorElement.elements("pre-processing-actions").size());
				Assert.assertEquals(0, processorElement.elements("post-processing-actions").size());
				Assert.assertEquals(0, processorElement.elements("post-receiving-actions").size());
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveOneProcessorMultipleAdditionals() throws Exception {
		String path = "src/test/resources/temp-processors/processors.xml";
		
		List<Acceptor> acceptors = new ArrayList<Acceptor>();
		acceptors.add(new MockAcceptor());
		acceptors.add(new MockAcceptor());
		
		List<Action> actions = new ArrayList<Action>();
		actions.add(new MockAction());
		actions.add(new MockAction());
		
		ProcessorService processorService = mockProcessorService("test", 1000, 
				new MockConnector(), acceptors, actions, actions, actions);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getProcessors())
			.thenReturn(Collections.singletonList(processorService));
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("processors", rootElement.getName());
				
				// validate processor
				Assert.assertEquals(1, rootElement.elements("processor").size());
				Element processorElement = rootElement.element("processor");
				Assert.assertEquals(2, processorElement.attributeCount());
				Assert.assertEquals("test", processorElement.attributeValue("id"));
				Assert.assertEquals("1000", processorElement.attributeValue("priority"));
				
				// validate connector
				Assert.assertEquals(1, processorElement.elements("connector").size());
				Element connectorElement = processorElement.element("connector");
				Assert.assertEquals(1, connectorElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConnector", connectorElement.attributeValue("className"));
				
				// validate additional
				Assert.assertEquals(1, processorElement.elements("acceptors").size());
				Element acceptorsElement = processorElement.element("acceptors");
				Assert.assertEquals(2, acceptorsElement.elements("acceptor").size());
				
				Assert.assertEquals(1, processorElement.elements("pre-processing-actions").size());
				Element preProcessingActionsElement = processorElement.element("pre-processing-actions");
				Assert.assertEquals(2, preProcessingActionsElement.elements("action").size());
				
				Assert.assertEquals(1, processorElement.elements("post-processing-actions").size());
				Element postProcessingActionsElement = processorElement.element("post-processing-actions");
				Assert.assertEquals(2, postProcessingActionsElement.elements("action").size());
				
				Assert.assertEquals(1, processorElement.elements("post-receiving-actions").size());
				Element postReceivingActionsElement = processorElement.element("post-receiving-actions");
				Assert.assertEquals(2, postReceivingActionsElement.elements("action").size());
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveMultipleProcessors() throws Exception {
		String path = "src/test/resources/temp-processors/processors.xml";
		
		List<ProcessorService> processors = new ArrayList<ProcessorService>();
		processors.add(mockProcessorService("test-1", 1000, new MockConnector(), new ArrayList<Acceptor>(), 
				new ArrayList<Action>(), new ArrayList<Action>(), new ArrayList<Action>()));
		processors.add(mockProcessorService("test-2", 2000, new MockConnector(), new ArrayList<Acceptor>(), 
				new ArrayList<Action>(), new ArrayList<Action>(), new ArrayList<Action>()));
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getProcessors())
			.thenReturn(processors);
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("processors", rootElement.getName());
				
				// validate receiver
				Assert.assertEquals(2, rootElement.elements("processor").size());
			}
			
		}.validate();
		
	}
	
	@Test
	public void testSaveComplexDocument() throws Exception {
		String path = "src/test/resources/temp-processors/processors.xml";
		
		List<Acceptor> acceptors = new ArrayList<Acceptor>();
		acceptors.add(new MockConfigurableAcceptor("test1", 1));
		
		List<Action> actions = new ArrayList<Action>();
		actions.add(new MockConfigurableAction("test1", 1));
		
		ProcessorService processorService = mockProcessorService("test", 1000, new MockConfigurableConnector("test", 0), 
				acceptors, actions, actions, actions);
		
		RoutingEngine routingEngine = Mockito.mock(RoutingEngine.class);
		Mockito
			.when(routingEngine.getProcessors())
			.thenReturn(Collections.singletonList(processorService));
		
		ProcessorConfiguration config = new ProcessorConfiguration();
		config.setRoutingEngine(routingEngine);
		config.setPath(path);
		
		config.save();
		
		// check that the file exists
		File file = new File(path);
		Assert.assertTrue(file.exists());
		
		new ValidateDoc(file) {

			@Override
			public void validate(Element rootElement) {
				Assert.assertEquals("processors", rootElement.getName());
				
				// validate processor
				Assert.assertEquals(1, rootElement.elements("processor").size());
				Element processorElement = rootElement.element("processor");
				Assert.assertEquals(2, processorElement.attributeCount());
				Assert.assertEquals("test", processorElement.attributeValue("id"));
				Assert.assertEquals("1000", processorElement.attributeValue("priority"));
				
				// validate connector
				Assert.assertEquals(1, processorElement.elements("connector").size());
				Element connectorElement = processorElement.element("connector");
				Assert.assertEquals(1, connectorElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConfigurableConnector", 
						connectorElement.attributeValue("className"));
				validateProperties(connectorElement, "test", "0");
				
				// validate acceptors
				Assert.assertEquals(1, processorElement.elements("acceptors").size());
				Element acceptorsElement = processorElement.element("acceptors");
				
				Assert.assertEquals(1, acceptorsElement.elements("acceptor").size());
				Element acceptorElement = acceptorsElement.element("acceptor");
				Assert.assertEquals(1, acceptorElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConfigurableAcceptor", 
						acceptorElement.attributeValue("className"));
				validateProperties(acceptorElement, "test1", "1");
				
				// validate pre-processing-actions
				Assert.assertEquals(1, processorElement.elements("pre-processing-actions").size());
				Element preProcessingActionsElement = processorElement.element("pre-processing-actions");
				
				Assert.assertEquals(1, preProcessingActionsElement.elements("action").size());
				Element preProcessingActionElement = preProcessingActionsElement.element("action");
				Assert.assertEquals(1, preProcessingActionElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConfigurableAction", 
						preProcessingActionElement.attributeValue("className"));
				validateProperties(preProcessingActionElement, "test1", "1");
				
				// validate post-processing-actions
				Assert.assertEquals(1, processorElement.elements("post-processing-actions").size());
				Element postProcessingActionsElement = processorElement.element("post-processing-actions");
				
				Assert.assertEquals(1, postProcessingActionsElement.elements("action").size());
				Element postProcessingActionElement = postProcessingActionsElement.element("action");
				Assert.assertEquals(1, postProcessingActionElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConfigurableAction", 
						postProcessingActionElement.attributeValue("className"));
				validateProperties(postProcessingActionElement, "test1", "1");
				
				// validate post-receiving-actions
				Assert.assertEquals(1, processorElement.elements("post-receiving-actions").size());
				Element postReceivingActionsElement = processorElement.element("post-receiving-actions");
				
				Assert.assertEquals(1, postReceivingActionsElement.elements("action").size());
				Element postReceivingActionElement = postReceivingActionsElement.element("action");
				Assert.assertEquals(1, postReceivingActionElement.attributeCount());
				Assert.assertEquals("org.mokai.types.mock.MockConfigurableAction", 
						postReceivingActionElement.attributeValue("className"));
				validateProperties(postReceivingActionElement, "test1", "1");
			}
			
		}.validate();
		
	}
	
	@SuppressWarnings("unchecked")
	private void validateProperties(Element parentElement, String config1, String config2) {
		// validate connector properties
		Assert.assertEquals(2, parentElement.elements("property").size());
		Iterator connectorProperties = parentElement.elements("property").iterator();
		while (connectorProperties.hasNext()) {
			Element propertyElement = (Element) connectorProperties.next();
			Assert.assertEquals(1, propertyElement.attributeCount());
			
			String propertyName = propertyElement.attributeValue("name");
			String propertyValue = propertyElement.getText();
			Assert.assertTrue(propertyName.equals("config1") || propertyName.equals("config2"));
			Assert.assertTrue(propertyValue.equals(config1) || propertyValue.equals(config2));					
		}
	}
	
	public ProcessorService mockProcessorService(String id, int priority, Processor processor, 
			List<Acceptor> acceptors, List<Action> preProcessingActions, 
			List<Action> postProcessingActions, List<Action> postReceivingActions) {
		
		ProcessorService processorService = Mockito.mock(ProcessorService.class);
		
		Mockito.when(processorService.getId()).thenReturn(id);
		Mockito.when(processorService.getPriority()).thenReturn(priority);
		
		Mockito.when(processorService.getProcessor()).thenReturn(processor);
		
		Mockito
			.when(processorService.getAcceptors())
			.thenReturn(acceptors);
		
		Mockito
			.when(processorService.getPreProcessingActions())
			.thenReturn(preProcessingActions);
		
		Mockito
			.when(processorService.getPostProcessingActions())
			.thenReturn(postProcessingActions);
		
		Mockito
			.when(processorService.getPostReceivingActions())
			.thenReturn(postReceivingActions);
		
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
	
	private class ProcessorServiceAnswer implements Answer<ProcessorService> {
		
		private ProcessorService processorService;
		private long delay;
		
		public ProcessorServiceAnswer(ProcessorService processorService, long delay) {
			this.processorService = processorService;
			this.delay = delay;
		}

		@Override
		public ProcessorService answer(InvocationOnMock invocation) throws Throwable {
			Thread.sleep(delay);
			return processorService;
		}
		
	}
	
}
