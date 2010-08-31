package org.mokai.plugin;

import java.util.Set;

/**
 * Defines methods to interact with a plugin mechanism.
 * 
 * @author German Escobar
 */
public interface PluginMechanism {

	<T> Set<Class<? extends T>> loadTypes(Class<T> type) 
			throws IllegalArgumentException, PluginException;
	
	Class<?> loadClass(String className) throws IllegalArgumentException, 
			PluginException;
	
}
