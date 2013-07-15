package org.mokai.impl.camel;

import java.lang.reflect.Field;

import org.mokai.ExecutionException;
import org.mokai.annotation.Resource;

/**
 * Helper class to inject resources into objects. Resource objects must be
 * annotated with {@link Resource}.
 *
 * @author German Escobar
 */
public final class ResourceInjector {

	private ResourceInjector() {}

	public static void inject(Object object, Object resource) {
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Resource.class)
					&& field.getType().isInstance(resource)) {
				field.setAccessible(true);
				try {
					field.set(object, resource);
				} catch (Exception e) {
					throw new ExecutionException(e);
				}
			}
		}
	}

	public static void inject(Object object, ResourceRegistry resourceRegistry) {
		for (Object resource : resourceRegistry.getResources()) {
			ResourceInjector.inject(object, resource);
		}
	}
}
