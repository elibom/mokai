package org.mokai.web.admin.jogger.controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.mokai.annotation.Name;

public final class Helper {
	
	private Helper() {}

	/**
	 * Helper method. Check if the field has a {@link Name} annotation and returns its value, otherwise, the name of 
	 * the class is returned.
	 * 
	 * @param component the Object from which we are retrieving the name. 
	 * @return a String that is used as the name of the component.
	 */
	public static String getComponentName(Object component) {
		Class<?> componentClass = component.getClass();

		Name nameAnnotation = componentClass.getAnnotation(Name.class);
		if (nameAnnotation != null) {
			return nameAnnotation.value();
		}

		return componentClass.getSimpleName();
	}

	/**
	 * Helper method that retrieves all the fields that have a getter method (i.e. are readable) of an object.
	 * 
	 * @param clazz the class from which we are going to retrieve the readable fields.
	 * @return a List of Field objects that are readable.
	 */
	public static List<Field> getConfigurationFields(Class<?> clazz) {

		List<Field> ret = new ArrayList<Field>();

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {

			boolean existsGetter = existsGetter(clazz, field);
			if (existsGetter) {
				ret.add(field);
			}
		}

		return ret;

	}

	/**
	 * Helper method. Checks if there is a getter method of the field in the configClass.
	 * 
	 * @param configClass the class in which we are checking for the getter method.
	 * @param field the field for whose getter method we are searching.
	 * @return true if a getter method exists in the configClass, false otherwise.
	 */
	private static boolean existsGetter(Class<?> configClass, Field field) {
		try {
			configClass.getMethod("get" + capitalize(field.getName()));
		} catch (NoSuchMethodException e) {
			return false;
		}
		return true;
	}

	/**
	 * Helper method. Capitalizes the first letter of a string. 
	 * 
	 * @param name the string that we want to capitalize.
	 * @return the capitalized string.
	 */
	private static String capitalize(String name) {
		 if (name == null || name.length() == 0) {
			 return name;
		 }

		 return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}
