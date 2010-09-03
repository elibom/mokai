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
public class ResourceInjector {

	public static void inject(Object object, Object objectToInject) {
		
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Resource.class) 
					&& field.getType().isInstance(objectToInject)) {
				field.setAccessible(true);
				try {
					field.set(object, objectToInject);
				} catch (Exception e) {
					throw new ExecutionException(e);
				} 
			}
		}
		
	}
}
