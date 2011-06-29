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
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.ExposableConfiguration;
import org.mokai.Processor;
import org.mokai.RoutingEngine;
import org.mokai.config.Configuration;
import org.mokai.config.ConfigurationException;
import org.mokai.plugin.PluginMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of {@link ApplicationsConfiguration} and {@link ConnectionsConfiguration} used to load
 * connectors from an XML file. 
 * 
 * @author German Escobar
 */
public abstract class AbstractConfiguration implements Configuration {

	private Logger log = LoggerFactory.getLogger(AbstractConfiguration.class);

	private String path = getDefaultPath();
	
	protected RoutingEngine routingEngine;
	
	protected PluginMechanism pluginMechanism;

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
	
	/**
	 * Helper method. Retrieves an InputStream from a file path.
	 * 
	 * @param path the path from which we are retrieving the InputStream.
	 * @return the InputStream object or null if the file is not found.
	 */
	private InputStream searchFromFile(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void load(InputStream inputStream) throws Exception {
		// create the document
		SAXReader reader = new SAXReader();
		reader.setEntityResolver(new SchemaEntityResolver());
		reader.setValidation(true);
		
		reader.setFeature("http://xml.org/sax/features/validation", true);
        reader.setFeature("http://apache.org/xml/features/validation/schema", true );
        reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
		
		Document document = reader.read(inputStream);
		
		// iterate through 'connector' elements
		Iterator iterator = document.getRootElement().elementIterator();
		while (iterator.hasNext()) {
			
			Element connectorElement = (Element) iterator.next();
			handleConnectorElement(connectorElement);
			
		}
	}
	
	private void handleConnectorElement(final Element connectorElement) throws Exception {
		
		// build the processor connector
		final Connector connector = buildConnector(connectorElement);
		
		// build the acceptors
		final List<Acceptor> acceptors = buildAcceptors(connectorElement.element("acceptors"));
		
		// build the pre-processing actions
		final List<Action> preProcessingActions = XmlConfigurationUtils.buildActions(routingEngine, 
				pluginMechanism, connectorElement.element("pre-processing-actions"));
		
		// build the post-processing actions
		final List<Action> postProcessingActions = XmlConfigurationUtils.buildActions(routingEngine, 
				pluginMechanism, connectorElement.element("post-processing-actions"));
		
		// build the post-receiving actions
		final List<Action> postReceivingActions = XmlConfigurationUtils.buildActions(routingEngine, 
				pluginMechanism, connectorElement.element("post-receiving-actions"));
		
		String id = connectorElement.attributeValue("id");
		int priority = getPriority(connectorElement);
		ConnectorService processorService = addConnector(id, connector);
		processorService.setPriority(priority);
		
		// set maxConcurrentMsgs of the ProcessorService
		int maxConcurrentMsgs = getMaxConcurrentMsgs(connectorElement);
		processorService.setMaxConcurrentMsgs(maxConcurrentMsgs);
		
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
	
	private int getPriority(Element connectorElement) throws Exception {
		int priority = 1000;
		
		String value = connectorElement.attributeValue("priority");
		if (value != null && !"".equals(value)) {
			priority = Integer.parseInt(value);
		}
		
		return priority;
	}
	
	private int getMaxConcurrentMsgs(Element connectorElement) throws Exception {
		int maxConcurrentMsgs = 1;
		
		String value = connectorElement.attributeValue("maxConcurrentMsgs");
		if (value != null && !"".equals(value)) {
			maxConcurrentMsgs = Integer.parseInt(value);
		}
		
		return maxConcurrentMsgs;
	}
	
	@SuppressWarnings("unchecked")
	private Connector buildConnector(Element element) throws Exception {
		String className = element.attributeValue("className");
		
		Class<? extends Connector> connectorClass = null;
		
		if (pluginMechanism != null) {
			connectorClass = (Class<? extends Processor>) pluginMechanism.loadClass(className);
		} 
		
		if (connectorClass == null) {
			connectorClass = (Class<? extends Processor>) Class.forName(className);
		}
		
		Connector connector = connectorClass.newInstance();
		
		if (ExposableConfiguration.class.isInstance(connector)) {
			ExposableConfiguration<?> configurableConnector = (ExposableConfiguration<?>) connector;

			Element configurationElement = element.element("configuration");
			if (configurationElement != null) {
				XmlConfigurationUtils.setConfigurationFields(configurationElement, 
						configurableConnector.getConfiguration(), routingEngine);
			}
		}
		
		return connector;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		// retrieve connectors
		List<ConnectorService> connectors = getConnectors();
		
		Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("connectors");
        
        for (ConnectorService connectorService : connectors) {
        	
        	Element connectorElement = rootElement.addElement("connector")
        		.addAttribute("id", connectorService.getId())
        		.addAttribute("priority", connectorService.getPriority() + "")
        		.addAttribute("className", connectorService.getConnector().getClass().getCanonicalName());
        	
        	// if exposes configuration, save it
        	if (ExposableConfiguration.class.isInstance(connectorService.getConnector())) {
        		ExposableConfiguration<?> configurableProcessor = (ExposableConfiguration<?>) connectorService.getConnector();
        		
        		Element configurationElement = connectorElement.addElement("configuration");
        		XmlConfigurationUtils.addConfigurationFields(configurationElement, configurableProcessor.getConfiguration());
        	}
        	
        	// add acceptors
        	List<Acceptor> acceptors = connectorService.getAcceptors();
        	if (acceptors != null && !acceptors.isEmpty()) {
	        	Element acceptorsElement = connectorElement.addElement("acceptors");
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
        	addActions(connectorService.getPreProcessingActions(), connectorElement, "pre-processing-actions");
        	
        	// add post-processing-actions
        	addActions(connectorService.getPostProcessingActions(), connectorElement, "post-processing-actions");
        	
        	// add post-receiving-actions
        	addActions(connectorService.getPostReceivingActions(), connectorElement, "post-receiving-actions");

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
	
	/**
	 * @return the default path of the XML configuration file (just in case one is not specified).
	 */
	protected abstract String getDefaultPath();
	
	/**
	 * Helper method. Called to add a connector, usually to a {@link RoutingEngine}.
	 * 
	 * @param id
	 * @param connector
	 * @return a {@link ConnectorService} object.
	 */
	protected abstract ConnectorService addConnector(String id, Connector connector);
	
	/**
	 * Helper method. Called to retrieve a list of connectors, usually of a {@link RoutingEngine}
	 * 
	 * @return a list of {@link ConnectorService} objects.
	 */
	protected abstract List<ConnectorService> getConnectors();

}
