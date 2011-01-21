package org.mokai.config.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Receiver;
import org.mokai.ReceiverService;
import org.mokai.RoutingEngine;
import org.mokai.config.Configuration;
import org.mokai.config.ConfigurationException;
import org.mokai.plugin.PluginMechanism;

/**
 * Loads and saves {@link ReceiverService}s information to and from an 
 * XML file.
 * 
 * @author German Escobar
 */
public class ReceiverConfiguration implements Configuration {
	
	private String path = "conf/receivers.xml";
	
	private RoutingEngine routingEngine;
	
	private PluginMechanism pluginMechanism;

	@Override
	public final void load() throws ConfigurationException {
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
		} finally {
			if (inputStream != null) {
				try { inputStream.close(); } catch (Exception e) {}
			}
		}
		
	}
	
	private InputStream searchFromFile(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public final void load(InputStream inputStream) throws Exception {
			
		// create the document
		SAXReader reader = new SAXReader();
		reader.setEntityResolver(new SchemaEntityResolver());
		reader.setValidation(true);
		
		reader.setFeature("http://xml.org/sax/features/validation", true);
        reader.setFeature("http://apache.org/xml/features/validation/schema", true );
        reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
		
		Document document = reader.read(inputStream);
			
		// iterate through 'receiver' elements
		Iterator iterator = document.getRootElement().elementIterator();
		while (iterator.hasNext()) {
				
			// handle 'receiver' element
			Element receiverElement = (Element) iterator.next();
			handleReceiverElement(receiverElement);
				
		}	
		
	}
	
	private void handleReceiverElement(final Element receiverElement) throws Exception {
		
		// build the receiver connector
		final Receiver receiver = buildReceiverConnector(receiverElement);
		
		// build the post-receiving actions
		final List<Action> postReceivingActions = XmlConfigurationUtils.buildActions(routingEngine, 
				pluginMechanism, receiverElement.element("post-receiving-actions"));
		
		String id = receiverElement.attributeValue("id");
		ReceiverService receiverService = routingEngine.createReceiver(id, receiver);
				
		// add post-receiving actions to receiver
		for (Action action : postReceivingActions) {
			receiverService.addPostReceivingAction(action);
		}	
		
	}
	
	@SuppressWarnings("unchecked")
	private Receiver buildReceiverConnector(Element element) throws Exception {
		String className = element.attributeValue("className");
		
		Class<? extends Receiver> receiverClass = null;
		if (pluginMechanism != null) {
			receiverClass = (Class<? extends Receiver>) pluginMechanism.loadClass(className);
		} 
		
		if (receiverClass == null) {
			receiverClass = (Class<? extends Receiver>) Class.forName(className);
		}
		
		Receiver receiverConnector = receiverClass.newInstance();
		
		if (ExposableConfiguration.class.isInstance(receiverConnector)) {
			ExposableConfiguration<?> configurableConnector = 
				(ExposableConfiguration<?>) receiverConnector;
			
			Element configurationElement = element.element("configuration");
			if (configurationElement != null) {
				XmlConfigurationUtils.setConfigurationFields(configurationElement, 
						configurableConnector.getConfiguration(), routingEngine);
			}
		}
		
		return receiverConnector;
	}

	@Override
	public final void save() {
		
		try {
			
			Document document = createReceiversDocument();
	        XmlConfigurationUtils.writeDocument(document, path);
	        
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
	}
	
	public final Document createReceiversDocument() throws Exception {
		// retrieve receivers
		Collection<ReceiverService> receivers = routingEngine.getReceivers();
		
		Document document = DocumentHelper.createDocument();
        Element root = document.addElement("receivers");
        
        for (ReceiverService receiver : receivers) {
        	Element receiverElement = root.addElement("receiver")
        			.addAttribute("id", receiver.getId());
        	
        	Element connectorElement = receiverElement.addElement("connector")
        			.addAttribute("className", receiver.getReceiver().getClass().getCanonicalName());
        	
        	// if exposes configuration, save it
        	if (ExposableConfiguration.class.isInstance(receiver.getReceiver())) {
        		ExposableConfiguration<?> configurableReceiver = 
        			(ExposableConfiguration<?>) receiver.getReceiver();

        		XmlConfigurationUtils.addConfigurationFields(connectorElement, configurableReceiver.getConfiguration());
        	}
        	
        	// save post receiving actions
        	List<Action> postReceivingActions = receiver.getPostReceivingActions();
        	
        	// only add the tag if the list is not empty
        	if (postReceivingActions != null && !postReceivingActions.isEmpty()) {
        		Element postReceivingActionsElement = receiverElement.addElement("post-receiving-actions");
        		for (Action action : postReceivingActions) {
        			Element actionElement = postReceivingActionsElement.addElement("action")
        				.addAttribute("className", action.getClass().getCanonicalName());
        			
        			if (ExposableConfiguration.class.isInstance(action)) {
        				ExposableConfiguration<?> configurableAction = (ExposableConfiguration<?>) action;
        				
        				XmlConfigurationUtils.addConfigurationFields(actionElement, configurableAction.getConfiguration());
        			}
        		}
        		
        	}
        }
        
        return document;
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
	
}
