package org.mokai.impl.camel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author German Escobar
 */
public class ResourceRegistry {

	private Map<Class<?>,Object> resources = new HashMap<Class<?>,Object>();

	private static ResourceRegistry instance;

	public static ResourceRegistry getInstance() {
		if (instance == null) {
			instance = new ResourceRegistry();
		}

		return instance;
	}

	public final void putResource(Class<?> clazz, Object resource) {
		resources.put(clazz, resource);
	}

	public final void removeResource(Class<?> resourceClass) {
		resources.remove(resourceClass);
	}

	@SuppressWarnings("unchecked")
	public final <T> T getResource(Class<T> resourceClass) {
		return (T) resources.get(resourceClass);
	}

	public final Collection<Object> getResources() {
		return resources.values();
	}
}
