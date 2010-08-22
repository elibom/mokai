package org.mokai.config.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class ProcessorConfiguration implements Configuration {
	
	private String path = "data/processors.xml";
	
	private RoutingEngine routingEngine;
	
	private PluginMechanism pluginMechanism;

	@Override
	public void load() {
		// search from file
		InputStream inputStream = searchFromFile(path);

		// if not found, throw exception
		if (inputStream == null) {
			throw new ConfigurationException("path " + path + " couldn't be found");
		}
		
		try {
			load(inputStream);
		} catch (Exception e) {
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
	public void load(InputStream inputStream) throws Exception {
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
	
	private void handleProcessorElement(Element processorElement) throws Exception {
		
		// build the processor connector
		Element connectorElement = processorElement.element("connector");
		Processor connector = buildProcessorConnector(connectorElement);
		
		// create the receiver service
		String id = processorElement.attributeValue("id");
		int priority = Integer.parseInt(processorElement.attributeValue("priority"));
		ProcessorService processorService = routingEngine.createProcessor(id, priority, connector);
		
		// handle acceptors
		Element acceptorsElement = processorElement.element("acceptors");
		if (acceptorsElement != null) {
			List<Acceptor> acceptors = buildAcceptors(acceptorsElement);
			for (Acceptor acceptor : acceptors) {
				processorService.addAcceptor(acceptor);
			}
		}
		
		// handle pre-processing-actions
		Element preProcessingActionsElement = processorElement.element("pre-processing-actions");
		if (preProcessingActionsElement != null) {
			List<Action> preProcessingActions = buildActions(preProcessingActionsElement);
			for (Action action : preProcessingActions) {
				processorService.addPreProcessingAction(action);
			}
		}
		
		// handle post-processing-actions
		Element postProcessingActionsElement = processorElement.element("post-processing-actions");
		if (postProcessingActionsElement != null) {
			List<Action> postProcessingActions = buildActions(postProcessingActionsElement);
			for (Action action : postProcessingActions) {
				processorService.addPostProcessingAction(action);
			}
		}
		
		// handle post-receiving-actions
		Element postReceivingActionsElement = processorElement.element("post-receiving-actions");
		if (postReceivingActionsElement != null) {
			List<Action> postReceivingActions = buildActions(postReceivingActionsElement);
			for (Action action : postReceivingActions) {
				processorService.addPostReceivingAction(action);
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private Processor buildProcessorConnector(Element element) throws Exception {
		String className = element.attributeValue("className");
		
		Class<? extends Processor> processorClass = null;
		
		if (pluginMechanism != null) {
			processorClass = (Class<? extends Processor>) pluginMechanism.loadClass(className);
		} else {
			processorClass = (Class<? extends Processor>) Class.forName(className);
		}
		Processor processorConnector = processorClass.newInstance();
		
		if (ExposableConfiguration.class.isInstance(processorConnector)) {
			ExposableConfiguration<?> configurableConnector = 
				(ExposableConfiguration<?>) processorConnector;
			
			XmlUtils.setConfigurationFields(element, configurableConnector.getConfiguration(), routingEngine);
		}
		
		return processorConnector;
	}
	
	@SuppressWarnings("unchecked")
	private List<Acceptor> buildAcceptors(Element acceptorsElement) throws Exception {
		
		List<Acceptor> acceptors = new ArrayList<Acceptor>();
		
		Iterator iterator = acceptorsElement.elementIterator();
		while (iterator.hasNext()) {
			Element acceptorElement = (Element) iterator.next();
			
			// create acceptor instance
			String className = acceptorElement.attributeValue("className");
			Class<? extends Acceptor> acceptorClass = null;
			if (pluginMechanism != null) {
				acceptorClass = (Class<? extends Acceptor>) pluginMechanism.loadClass(className);
			} else {
				acceptorClass = (Class<? extends Acceptor>) Class.forName(className);
			}
			Acceptor acceptor = acceptorClass.newInstance();
			
			if (ExposableConfiguration.class.isInstance(acceptor)) {
				ExposableConfiguration<?> exposableAcceptor = (ExposableConfiguration<?>) acceptor;
				
				XmlUtils.setConfigurationFields(acceptorElement, exposableAcceptor.getConfiguration(), routingEngine);
			}

			acceptors.add(acceptor);
		}
		
		return acceptors;
	}
	
	@SuppressWarnings("unchecked")
	private List<Action> buildActions(Element actionsElement) throws Exception {
		
		List<Action> actions = new ArrayList<Action>();
		
		Iterator iterator = actionsElement.elementIterator();
		while (iterator.hasNext()) {
			Element actionElement = (Element) iterator.next();
			
			// create action instance
			String className = actionElement.attributeValue("className");
			Class<? extends Action> actionClass = null;
			if (pluginMechanism != null) {
				actionClass = (Class<? extends Action>) pluginMechanism.loadClass(className);
			} else {
				actionClass = (Class<? extends Action>) Class.forName(className);
			}
			Action action = actionClass.newInstance();
			
			if (ExposableConfiguration.class.isInstance(action)) {
				ExposableConfiguration<?> exposableAction = (ExposableConfiguration<?>) action;
				
				XmlUtils.setConfigurationFields(actionElement, exposableAction.getConfiguration(), routingEngine);
			}
			
			actions.add(action);
		}
		
		return actions;
	}
	

	@Override
	public void save() {
		try {
			
	        Document document = createProcessorsDocument();
	        XmlUtils.writeDocument(document, path);
	        
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Document createProcessorsDocument() throws Exception {
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
        		
        		XmlUtils.addConfigurationFields(connectorElement, configurableProcessor.getConfiguration());
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
	        			
	        			XmlUtils.addConfigurationFields(acceptorElement, configurableAcceptor.getConfiguration());
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
        			
        			XmlUtils.addConfigurationFields(actionElement, configurableAction.getConfiguration());
        		}
    		}
    	}
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

	public void setPluginMechanism(PluginMechanism pluginMechanism) {
		this.pluginMechanism = pluginMechanism;
	}
	
}
