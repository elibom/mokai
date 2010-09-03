package org.mokai.config.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Processor;
import org.mokai.ProcessorService;
import org.mokai.RoutingEngine;
import org.mokai.config.Configuration;
import org.mokai.config.ConfigurationException;
import org.mokai.plugin.PluginMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and saves {@link ProcessorService}s information to and from an 
 * XML file.
 * 
 * @author German Escobar
 */
public class ProcessorConfiguration implements Configuration {
	
	private Logger log = LoggerFactory.getLogger(ProcessorConfiguration.class);
	
	private static final int DEFAULT_POOL_SIZE = 3;
	private static final int DEFAULT_MAX_POOL_SIZE = 4; 
	
	private String path = "data/processors.xml";
	
	private RoutingEngine routingEngine;
	
	private PluginMechanism pluginMechanism;
	
	private Executor executor = 
		new ThreadPoolExecutor(DEFAULT_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, Long.MAX_VALUE, 
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

	@Override
	public final void load() {
		// search from file
		InputStream inputStream = searchFromFile(path);

		// if not found, throw exception
		if (inputStream == null) {
			throw new ConfigurationException("path " + path + " couldn't be found");
		}
		
		try {
			load(inputStream);
		} catch (Exception e) {
			log.error("Exception loading configuration: " + e.getMessage(), e);
			throw new ConfigurationException(e);			
		}
	}
	
	private InputStream searchFromFile(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public final void load(InputStream inputStream) throws Exception {
		// create the document
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		
		// iterate through 'connector' elements
		Iterator iterator = document.getRootElement().elementIterator();
		while (iterator.hasNext()) {
			
			Element processorElement = (Element) iterator.next();
			handleProcessorElement(processorElement);
			
		}
	}
	
	private void handleProcessorElement(final Element processorElement) throws Exception {
		
		// build the processor connector
		Element connectorElement = processorElement.element("connector");
		final Processor connector = buildProcessorConnector(connectorElement);
		
		// build the acceptors
		final List<Acceptor> acceptors = buildAcceptors(processorElement.element("acceptors"));
		
		// build the pre-processing actions
		final List<Action> preProcessingActions = XmlConfigurationUtils.buildActions(routingEngine, 
				pluginMechanism, processorElement.element("pre-processing-actions"));
		
		// build the post-processing actions
		final List<Action> postProcessingActions = XmlConfigurationUtils.buildActions(routingEngine, 
				pluginMechanism, processorElement.element("post-processing-actions"));
		
		// build the post-receiving actions
		final List<Action> postReceivingActions = XmlConfigurationUtils.buildActions(routingEngine, 
				pluginMechanism, processorElement.element("post-receiving-actions"));
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// create the processor service
				String id = processorElement.attributeValue("id");
				int priority = Integer.parseInt(processorElement.attributeValue("priority"));
				ProcessorService processorService = routingEngine.createProcessor(id, priority, connector);
				
				// add acceptors to the processor
				for (Acceptor acceptor : acceptors) {
					processorService.addAcceptor(acceptor);
				}
				
				// add pre-processing-actions to the processor 
				for (Action action : preProcessingActions) {
					processorService.addPreProcessingAction(action);
				}
				
				// add post-processing-actions to the processor
				for (Action action : postProcessingActions) {
					processorService.addPostProcessingAction(action);
				}
				
				// add post-receiving-actions to the processor
				for (Action action : postReceivingActions) {
					processorService.addPostReceivingAction(action);
				}
			}
			
		};
		executor.execute(runnable);
		
	}
	
	@SuppressWarnings("unchecked")
	private Processor buildProcessorConnector(Element element) throws Exception {
		String className = element.attributeValue("className");
		
		Class<? extends Processor> processorClass = null;
		
		if (pluginMechanism != null) {
			processorClass = (Class<? extends Processor>) pluginMechanism.loadClass(className);
		} 
		
		if (processorClass == null) {
			processorClass = (Class<? extends Processor>) Class.forName(className);
		}
		
		Processor processorConnector = processorClass.newInstance();
		
		if (ExposableConfiguration.class.isInstance(processorConnector)) {
			ExposableConfiguration<?> configurableConnector = 
				(ExposableConfiguration<?>) processorConnector;
			
			XmlConfigurationUtils.setConfigurationFields(element, configurableConnector.getConfiguration(), routingEngine);
		}
		
		return processorConnector;
	}
	
	@SuppressWarnings("unchecked")
	private List<Acceptor> buildAcceptors(Element acceptorsElement) throws Exception {
		
		List<Acceptor> acceptors = new ArrayList<Acceptor>();
		
		if (acceptorsElement == null) {
			return acceptors;
		}
		
		Iterator iterator = acceptorsElement.elementIterator();
		while (iterator.hasNext()) {
			Element acceptorElement = (Element) iterator.next();
			
			// create acceptor instance
			String className = acceptorElement.attributeValue("className");
			Class<? extends Acceptor> acceptorClass = null;
			if (pluginMechanism != null) {
				acceptorClass = (Class<? extends Acceptor>) pluginMechanism.loadClass(className);
			} 
			
			if (acceptorClass == null) {
				acceptorClass = (Class<? extends Acceptor>) Class.forName(className);
			}
			
			Acceptor acceptor = acceptorClass.newInstance();
			
			if (ExposableConfiguration.class.isInstance(acceptor)) {
				ExposableConfiguration<?> exposableAcceptor = (ExposableConfiguration<?>) acceptor;
				
				XmlConfigurationUtils.setConfigurationFields(acceptorElement, exposableAcceptor.getConfiguration(), routingEngine);
			}

			acceptors.add(acceptor);
		}
		
		return acceptors;
	}
	
	
	

	@Override
	public final void save() {
		try {
			
	        Document document = createProcessorsDocument();
	        XmlConfigurationUtils.writeDocument(document, path);
	        
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public final Document createProcessorsDocument() throws Exception {
		// retrieve processors
		List<ProcessorService> processors = routingEngine.getProcessors();
		
		Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("processors");
        
        for (ProcessorService processor : processors) {
        	
        	Element processorElement = rootElement.addElement("processor")
        		.addAttribute("id", processor.getId())
        		.addAttribute("priority", processor.getPriority() + "");
        	
        	Element connectorElement = processorElement.addElement("connector")
        		.addAttribute("className", processor.getProcessor().getClass().getCanonicalName());
        	
        	// if exposes configuration, save it
        	if (ExposableConfiguration.class.isInstance(processor.getProcessor())) {
        		ExposableConfiguration<?> configurableProcessor = 
        			(ExposableConfiguration<?>) processor.getProcessor();
        		
        		XmlConfigurationUtils.addConfigurationFields(connectorElement, configurableProcessor.getConfiguration());
        	}
        	
        	// add acceptors
        	List<Acceptor> acceptors = processor.getAcceptors();
        	if (acceptors != null && !acceptors.isEmpty()) {
	        	Element acceptorsElement = processorElement.addElement("acceptors");
	        	for (Acceptor acceptor : acceptors) {
	        		Element acceptorElement = acceptorsElement.addElement("acceptor")
	        			.addAttribute("className", acceptor.getClass().getCanonicalName());
	        		
	        		if (ExposableConfiguration.class.isInstance(acceptor)) {
	        			ExposableConfiguration<?> configurableAcceptor = (ExposableConfiguration<?>) acceptor;
	        			
	        			XmlConfigurationUtils.addConfigurationFields(acceptorElement, configurableAcceptor.getConfiguration());
	        		}
	        		
	        	}
        	}
        	
        	// add pre-processing-actions
        	addActions(processor.getPreProcessingActions(), processorElement, "pre-processing-actions");
        	
        	// add post-processing-actions
        	addActions(processor.getPostProcessingActions(), processorElement, "post-processing-actions");
        	
        	// add post-receiving-actions
        	addActions(processor.getPostReceivingActions(), processorElement, "post-receiving-actions");

        }
        
        return document;
		
	}
	
	private void addActions(List<Action> actions, Element processorElement, String elementName) throws Exception {
		
    	if (actions != null && !actions.isEmpty()) {
    		Element actionsElement = processorElement.addElement(elementName);
    		for (Action action : actions) {
    			Element actionElement = actionsElement.addElement("action")
    				.addAttribute("className", action.getClass().getCanonicalName());
    			
    			if (ExposableConfiguration.class.isInstance(action)) {
        			ExposableConfiguration<?> configurableAction = (ExposableConfiguration<?>) action;
        			
        			XmlConfigurationUtils.addConfigurationFields(actionElement, configurableAction.getConfiguration());
        		}
    		}
    	}
	}

	public final void setPath(String path) {
		this.path = path;
	}

	public final void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

	public final void setPluginMechanism(PluginMechanism pluginMechanism) {
		this.pluginMechanism = pluginMechanism;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
	
}
