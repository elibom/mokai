package org.mokai.plugin;

import java.util.Set;

/**
 * Defines methods to interact with a plugin mechanism.
 * 
 * @author German Escobar
 */
public interface PluginMechanism {

	public <T> Set<Class<? extends T>> loadTypes(Class<T> type) 
			throws IllegalArgumentException, PluginException;
	
	public Class<?> loadClass(String className) throws IllegalArgumentException, 
			PluginException;
	
}
