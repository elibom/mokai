package org.mokai.plugin.jpf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;
import org.java.plugin.util.ExtendedProperties;
import org.mokai.Configurable;
import org.mokai.plugin.PluginException;
import org.mokai.plugin.PluginMechanism;

/**
 * A Java Plugin Framework (JPF) implementation of the {@link PluginMechanism} 
 * interface.
 * 
 * @author German Escobar
 */
public class JpfPluginMechanism implements PluginMechanism, Configurable {
	
	private String pluginsPath = "plugins/";
	
	private PluginManager pluginManager;
	
	public JpfPluginMechanism() {
		
	}
	
	public JpfPluginMechanism(String pluginsPath) {
		this.pluginsPath = pluginsPath;
	}
	
	@Override
	public final void configure() throws Exception {
		// the directory of the plugins
		File pluginsRoot= new File(pluginsPath);
		if (!pluginsRoot.exists()) {
			pluginsRoot.mkdirs();
		}
		
		// configure initial properties
		ExtendedProperties props = new ExtendedProperties(System.getProperties());
        props.put("org.java.plugin.PathResolver", "org.java.plugin.standard.ShadingPathResolver");
        props.put("org.java.plugin.standard.ShadingPathResolver.shadowFolder", "temp/.jpf-shadow");
        props.put("org.java.plugin.standard.ShadingPathResolver.unpackMode", "smart");
        props.put("org.java.plugin.standard.PluginLifecycleHandler", "org.mokai.plugin.jpf.CustomPluginLifecycleHandler");
		
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

	@Override
	public final Class<?> loadClass(String className) throws IllegalArgumentException, PluginException {
		
		// validate that configure has been called
		checkPluginManager();
		
		Collection<PluginDescriptor> pluginDescriptors = pluginManager.getRegistry().getPluginDescriptors();
		
		for (PluginDescriptor pluginDescriptor : pluginDescriptors) {
			ClassLoader classLoader = pluginManager.getPluginClassLoader(pluginDescriptor);
			
			try {
				return classLoader.loadClass(className);
			} catch (ClassNotFoundException e) {}
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T> Set<Class<? extends T>> loadTypes(Class<T> type) throws IllegalArgumentException, PluginException {
		
		// validate that configure has been called
		checkPluginManager();
		
		Set<Class<? extends T>> ret = new HashSet<Class<? extends T>>();
		
		Collection<PluginDescriptor> pluginDescriptors = pluginManager.getRegistry().getPluginDescriptors();
		
		for (PluginDescriptor pluginDescriptor : pluginDescriptors) {
			ClassLoader classLoader = pluginManager.getPluginClassLoader(pluginDescriptor);
			
			ServiceLoader<T> sl = ServiceLoader.load(type, classLoader);
			for (T item : sl) {
				ret.add((Class<? extends T>) item.getClass());
			}
		}
		
		return ret;
	}
	
	private void checkPluginManager() throws PluginException {
		if (pluginManager == null) {
			try {
				configure();
			} catch (Exception e) {
				throw new PluginException(e);
			}
		}
	}

	@Override
	public final void destroy() throws Exception {
		if (pluginManager != null) {
			pluginManager.shutdown();
		}
	}

}
