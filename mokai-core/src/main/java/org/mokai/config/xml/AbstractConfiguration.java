package org.mokai.config.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.mokai.acceptor.AndAcceptor;
import org.mokai.acceptor.OrAcceptor;
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
		final Connector connector = buildConnector(connectorElement, true);
		
		// build the acceptors
		final List<Acceptor> acceptors = buildAcceptors(connectorElement.element("acceptors"));
		
		// build the pre-processing actions
		final List<Action> preProcessingActions = buildActions(connectorElement.element("pre-processing-actions"));
		
		// build the post-processing actions
		final List<Action> postProcessingActions = buildActions(connectorElement.element("post-processing-actions"));
		
		// build the post-receiving actions
		final List<Action> postReceivingActions = buildActions(connectorElement.element("post-receiving-actions"));
		
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
	private Connector buildConnector(Element element, boolean hasConfigElement) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, 
				IllegalArgumentException, NoSuchFieldException, NoSuchMethodException {
		
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

			if (hasConfigElement) {
				Element configurationElement = element.element("configuration");
				if (configurationElement != null) {
					setConfigurationFields(configurationElement, configurableConnector.getConfiguration());
				}
			} else {
				setConfigurationFields(element, configurableConnector.getConfiguration());
			}
		}
		
		return connector;
	}
	
	@SuppressWarnings("rawtypes")
	private List<Acceptor> buildAcceptors(Element acceptorsElement) throws Exception {
		
		List<Acceptor> acceptors = new ArrayList<Acceptor>(); // this is what we are finally returning
		
		if (acceptorsElement == null) {
			return acceptors;
		}
		
		// iterate through all elements (can be "acceptor" or "and" elements)
		Iterator iterator = acceptorsElement.elementIterator();
		while (iterator.hasNext()) {
			Element acceptorElement = (Element) iterator.next();
			
			Acceptor acceptor = buildAcceptor(acceptorElement);
			acceptors.add(acceptor);
		}
		
		return acceptors;
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Acceptor buildAcceptor(Element acceptorElement) throws ClassNotFoundException, InstantiationException, 
			IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchFieldException, NoSuchMethodException {
		
		Acceptor acceptor = null;
		
		if (acceptorElement.getName().equals("and")) {
			
			// and is just a wrapper of the AndAcceptor
			AndAcceptor andAcceptor = new AndAcceptor();
			
			Iterator acceptorIterator = acceptorElement.elementIterator();
			while (acceptorIterator.hasNext()) {
				Acceptor inner = buildAcceptor((Element) acceptorIterator.next());
				andAcceptor.addAcceptor(inner);
			}
			
			acceptor = andAcceptor;
			
		} else if (acceptorElement.getName().equals("or")) {
			
			// or is just a wrapper of the OrAcceptor
			OrAcceptor orAcceptor = new OrAcceptor();
			
			Iterator acceptorIterator = acceptorElement.elementIterator();
			while (acceptorIterator.hasNext()) {
				Acceptor inner = buildAcceptor((Element) acceptorIterator.next());
				orAcceptor.addAcceptor(inner);
			}
			
			acceptor = orAcceptor;
			
		} else {
		
			// create acceptor instance
			String className = acceptorElement.attributeValue("className");
			Class<? extends Acceptor> acceptorClass = null;
			if (pluginMechanism != null) {
				acceptorClass = (Class<? extends Acceptor>) pluginMechanism.loadClass(className);
			} 
			
			if (acceptorClass == null) {
				acceptorClass = (Class<? extends Acceptor>) Class.forName(className);
			}
			
			acceptor = acceptorClass.newInstance();
			
			if (ExposableConfiguration.class.isInstance(acceptor)) {
				ExposableConfiguration<?> exposableAcceptor = (ExposableConfiguration<?>) acceptor;
				
				setConfigurationFields(acceptorElement, exposableAcceptor.getConfiguration());
			}
			
		}
		
		return acceptor;
	}
	
	/**
	 * Helper method to build {@link Action}s from an XML element.
	 * 
	 * @param routingEngine 
	 * @param pluginMechanism
	 * @param actionsElement
	 * @return a list of {@link Action} objects or an empty list.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Action> buildActions(Element actionsElement) throws ClassNotFoundException, InstantiationException, IllegalAccessException, 
			SecurityException, IllegalArgumentException, NoSuchFieldException, NoSuchMethodException {
		
		List<Action> actions = new ArrayList<Action>();
		
		if (actionsElement == null) {
			return actions;
		}
		
		Iterator iterator = actionsElement.elementIterator();
		while (iterator.hasNext()) {
			Element actionElement = (Element) iterator.next();
			
			// create action instance
			String className = actionElement.attributeValue("className");
			Class<? extends Action> actionClass = null;
			if (pluginMechanism != null) {
				actionClass = (Class<? extends Action>) pluginMechanism.loadClass(className);
			}
			
			if (actionClass == null) {
				actionClass = (Class<? extends Action>) Class.forName(className);
			}
			
			Action action = actionClass.newInstance();
			
			if (ExposableConfiguration.class.isInstance(action)) {
				ExposableConfiguration<?> exposableAction = (ExposableConfiguration<?>) action;
				
				setConfigurationFields(actionElement, exposableAction.getConfiguration());
			}
			
			actions.add(action);
		}
		
		return actions;
	}
	
	@SuppressWarnings("rawtypes")
	private void setConfigurationFields(Element parentElement, Object configuration) throws SecurityException, IllegalArgumentException, 
			IllegalAccessException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, InstantiationException {
		
		Iterator properties = parentElement.elementIterator();
		while (properties.hasNext()) {
			Element propertyElement = (Element) properties.next();
			setConfigurationField(propertyElement, configuration, routingEngine);
		}
	}
	
	/**
	 * Helper method to handle <pre><property /></pre>, <pre><mapProperty /></pre>
	 * and <pre><listProperty /></pre> elements. 
	 * 
	 * @param element the element to handle. It can be property, mapProperty and 
	 * listProperty.
	 * @param configuration the object to which we are going to set the property.
	 * @param routingEngine the {@link RoutingEngine}. Not currently in use. 
	 * 
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setConfigurationField(Element element, Object configuration, 
			RoutingEngine routingEngine) throws IllegalAccessException, SecurityException, 
			NoSuchFieldException, IllegalArgumentException, NoSuchMethodException, ClassNotFoundException, InstantiationException {
		
		if (element.getName().equals("property")) {
			// retrieve field
			Field field = XmlConfigurationUtils.retrieveField(element, configuration);
			
			// retrieve value
			Object value = retrieveValue(element);

			// set value
			XmlConfigurationUtils.setValue(field, configuration, XmlConfigurationUtils.convert(field.getType(), value));
			
		} else if (element.getName().equals("mapProperty")) {
			
			Field field = XmlConfigurationUtils.retrieveField(element, configuration);
			
			if (!Map.class.isAssignableFrom(field.getType())) {
				throw new IllegalArgumentException("field " + field.getName() + " is not a Map");
			}
			
			field.setAccessible(true);
			Map map = (Map) field.get(configuration);
			
			Iterator iterator = element.elementIterator();
			while (iterator.hasNext()) {
				Element entry = (Element) iterator.next();
				String entryKey = entry.attributeValue("key");
				Object entryValue = retrieveValue(entry);
				
				map.put(entryKey, entryValue);
			}
			
			XmlConfigurationUtils.setValue(field, configuration, map);
			
		} else if (element.getName().equals("listProperty")) {
			Field field = XmlConfigurationUtils.retrieveField(element, configuration);
			
			if (!Collection.class.isAssignableFrom(field.getType())) {
				throw new IllegalArgumentException("field " + field.getName() + " is not a List");
			}
			
			field.setAccessible(true);
			Collection list = (Collection) field.get(configuration);
			
			Iterator iterator = element.elementIterator();
			while (iterator.hasNext()) {
				Element item = (Element) iterator.next();
				Object value = retrieveValue(item);
				list.add(value);
			}
			
		}
		
	}
	
	/**
	 * Helper method to retrieve a value from an element. It first checks if the element
	 * has an attribute named 'value'. If it does, it returns that value, otherwise, it 
	 * checks if the element is text only. If it is, it returns that value, otherwise, 
	 * it checks if the element has a child <pre><value /></pre> element. If it does, it 
	 * returns its text. Otherwise, it checks if the element has a <pre><null /></pre> 
	 * element. If it does, returns null. In any other case, it returns null.
	 *  
	 * @param element the Element from which we want to retrieve the value.
	 * 
	 * @return an Object value of the element or null if a <pre><null /></pre> element is
	 * found or no value is found.
	 * @throws NoSuchMethodException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	private Object retrieveValue(Element element) throws SecurityException, IllegalArgumentException, ClassNotFoundException, 
			InstantiationException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
		
		// check if we have a value attribute
		String valueText = element.attributeValue("value");
		if (valueText != null) {
			return valueText;
		}
		
		// check if the value was set directly
		if (element.isTextOnly()) {
			valueText = element.getText();
			if (valueText != null) {
				return valueText;
			}
		}
		
		// retrieve the child element 
		Element elementValue = (Element) element.elementIterator().next();
		if (elementValue.getName().equals("value")) {
			
			valueText = element.getText();
			if (valueText != null && !"".equals(valueText)) {
				return valueText;
			}
			
		} else if (elementValue.getName().equals("null")) {
			
			return null;
			
		} else if (elementValue.getName().equals("acceptor") || elementValue.getName().equals("and") 
				|| elementValue.getName().equals("or")) {
			
			return buildAcceptor(elementValue);
			
		} else if (elementValue.getName().equals("connector")) {
			
			return buildConnector(elementValue, false);
		}
		
		return "";
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
	
	private final Document createProcessorsDocument() throws Exception {
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
