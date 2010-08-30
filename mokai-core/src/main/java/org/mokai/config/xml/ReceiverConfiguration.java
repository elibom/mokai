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
	
	private String path = "data/receivers.xml";
	
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
	
	@SuppressWarnings("unchecked")
	public final void load(InputStream inputStream) throws Exception {
			
		// create the document
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
			
		// iterate through 'receiver' elements
		Iterator iterator = document.getRootElement().elementIterator();
		while (iterator.hasNext()) {
				
			// handle 'receiver' element
			Element receiverElement = (Element) iterator.next();
			handleReceiverElement(receiverElement);
				
		}	
		
	}
	
	private void handleReceiverElement(Element receiverElement) throws Exception {
		
		// build the receiver connector
		Element connectorElement = receiverElement.element("connector");
		Receiver receiver = buildReceiverConnector(connectorElement);
		
		// create the receiver service
		String id = receiverElement.attributeValue("id");
		ReceiverService receiverService = routingEngine.createReceiver(id, receiver);
		
		// handle 'post-receiving' actions element
		Element postReceivingActionsElement = 
			receiverElement.element("post-receiving-actions");
		if (postReceivingActionsElement != null) {
			handlePostReceivingActionsElement(postReceivingActionsElement, receiverService);
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
			
			XmlUtils.setConfigurationFields(element, configurableConnector.getConfiguration(), routingEngine);
		}
		
		return receiverConnector;
	}
	
	@SuppressWarnings("unchecked")
	private void handlePostReceivingActionsElement(Element postReceivingActionsElement, 
			ReceiverService receiverService) throws Exception {
		
		Iterator postReceivingActions = postReceivingActionsElement.elementIterator("action");
		
		while (postReceivingActions.hasNext()) {
			
			Element postReceivingAction = (Element) postReceivingActions.next();
			String className = postReceivingAction.attributeValue("className");
			
			Class<? extends Action> actionClass = null;
			if (pluginMechanism != null) {
				actionClass = (Class<? extends Action>) pluginMechanism.loadClass(className);
			}
			
			if (actionClass == null) {
				actionClass = (Class<? extends Action>) Class.forName(className);
			}
			
			Action action = actionClass.newInstance();
			
			if (ExposableConfiguration.class.isInstance(action)) {
				ExposableConfiguration<?> configurableAction = (ExposableConfiguration<?>) action;
				
				XmlUtils.setConfigurationFields(postReceivingAction, configurableAction.getConfiguration(), routingEngine);
			}
			
			receiverService.addPostReceivingAction(action);
			
		}
	}

	@Override
	public final void save() {
		
		try {
			
			Document document = createReceiversDocument();
	        XmlUtils.writeDocument(document, path);
	        
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

        		XmlUtils.addConfigurationFields(connectorElement, configurableReceiver.getConfiguration());
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
        				
        				XmlUtils.addConfigurationFields(actionElement, configurableAction.getConfiguration());
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
