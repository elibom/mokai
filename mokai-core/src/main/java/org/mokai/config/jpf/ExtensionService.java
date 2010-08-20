package org.mokai.config.jpf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.java.plugin.ObjectFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.standard.StandardPluginLocation;
import org.java.plugin.util.ExtendedProperties;
import org.mokai.spi.type.AcceptorType;
import org.mokai.spi.type.ProcessorType;
import org.mokai.spi.type.ReceiverType;

/**
 * 
 * @author German Escobar
 */
public class ExtensionService {

	private final String PLUGINS_PATH = "plugins/"; // plugins directory
	private final String CORE_PLUGIN_ID = "org.mokai.core"; // core plugin
	
	private final String ACCEPTOR_EXTENSION = "acceptorType"; // acceptor extension
	private final String RECEIVER_EXTENSION = "receiverType"; // receiver extension
	private final String PROCESSOR_EXTENSION = "processorType"; // processor extension
	
	private PluginManager pluginManager;
	
	public ExtensionService() throws Exception {
		init();
	}

	public void init() throws Exception {
		// the directory of the plugins
		File pluginsRoot= new File(PLUGINS_PATH);
		if (!pluginsRoot.exists()) {
			return;
		}
		
		// configuracion de propiedades iniciales
		ExtendedProperties props = new ExtendedProperties(System.getProperties());
        props.put("org.java.plugin.PathResolver", "org.java.plugin.standard.ShadingPathResolver");
        props.put("org.java.plugin.standard.ShadingPathResolver.shadowFolder", "temp/.jpf-shadow");
        props.put("org.java.plugin.standard.ShadingPathResolver.unpackMode", "smart");
		
		// instanciamos el plugin manager
		pluginManager = ObjectFactory.newInstance(props).createManager();
		
		File[] pluginsFolders = pluginsRoot.listFiles();
		PluginLocation[] pluginLocations = discoverPluginLocations(pluginsFolders);
	    pluginManager.publishPlugins(pluginLocations);
	}
	
	private PluginLocation[] discoverPluginLocations(File[] pluginsFolders) throws IOException, MalformedURLException {
		List<PluginLocation> result = new ArrayList<PluginLocation>();
		
		for (File pf : pluginsFolders) {
			result.add(StandardPluginLocation.create(pf));
		}
		
		return result.toArray(new PluginLocation[result.size()]);
	}
	
	public void destroy() throws Exception {
		if (pluginManager != null) {
			pluginManager.shutdown();
		}
	}
	
	public Collection<AcceptorType> getAcceptorTypes() throws Exception {
		Set<AcceptorType> acceptorTypes = new HashSet<AcceptorType>();
		
		loadExtensionPoints(ACCEPTOR_EXTENSION, AcceptorType.class);
		
		return acceptorTypes;
	}
	
	public Collection<ReceiverType> getReceiverTypes() throws Exception {
		Set<ReceiverType> receiverTypes = new HashSet<ReceiverType>();
		
		loadExtensionPoints(RECEIVER_EXTENSION, ReceiverType.class);
	    
	    return receiverTypes;
	}
	
	public Collection<ProcessorType> getProcessorTypes() throws Exception {
		Set<ProcessorType> processorTypes = new HashSet<ProcessorType>();
		
		loadExtensionPoints(PROCESSOR_EXTENSION, ProcessorType.class);
		
		return processorTypes;
	}
	
	@SuppressWarnings("unchecked")
	private <T> void loadExtensionPoints(String type, Class<T> clazz) throws Exception {
		Set<T> ret = new HashSet<T>();
		
		Plugin plugin = pluginManager.getPlugin(CORE_PLUGIN_ID);
	    ExtensionPoint extensionPoint = plugin.getDescriptor().getExtensionPoint(type);
	    
	    for (Extension extension : extensionPoint.getAvailableExtensions()) {
	    	
		    String className = extension.getParameter("className").valueAsString();
		    	
		    pluginManager.activatePlugin(extension.getDeclaringPluginDescriptor().getId());
		    	
		    ClassLoader classLoader = pluginManager.getPluginClassLoader(extension.getDeclaringPluginDescriptor());
		    Class<T> k = (Class<T>) classLoader.loadClass(className);
		    	
		    T objectType = k.newInstance();
		    	
		    ret.add(objectType);
	    	
	    }
	}
}
