package org.mokai.web.admin.vaadin.dashboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Service;
import org.mokai.Service.State;
import org.mokai.annotation.Name;
import org.mokai.ui.annotation.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Tree;

/**
 * Helper methods for the dashboard view.
 * 
 * @author German Escobar
 */
public class Helper {
	
	private static Logger log = LoggerFactory.getLogger(Helper.class);
	
	/**
	 * Helper method. Returns a sytle name based on the status of the service. 
	 * 
	 * @param service the Service object we are testing.
	 * @return "green" if the service is started, "red" otherwise.
	 */
	public static String getStateStyle(Service service) {
		if (service.getState().equals(State.STARTED)) {
			return "green";
		}
		
		return "red";
	}
	
	/**
	 * Helper method. Adds a list of actions to a tree, each one with a configuration node (if the action
	 * implements ExposableConfiguration). 
	 * 
	 * @param tree the Tree to which we are adding the actions.
	 * @param actions the actions to be added to the tree.
	 * @param name the name of the root of the actions.
	 */
	public static void addActionsToTree(Tree tree, List<Action> actions, String name) {
		
		if (!actions.isEmpty()) {
			
			tree.addItem(name);
			
			for (Action action : actions) {
				String actionClass = action.getClass().getSimpleName();
				
				tree.addItem(actionClass);
				tree.setParent(actionClass, name);
				
				addConfigurationToTree(tree, action, actionClass);

			}
			
		}
	}
	
	/**
	 * Helper method. Adds the configuration node if the object implements org.mokai.ExposableConfiguration.
	 * 
	 * @param tree the Tree object to which we are going to add the configuration node.
	 * @param object the object to test and from which we are retriving the configuration attributes.
	 * @param parentId the parent id to which we are adding the configuration node.
	 */
	public static void addConfigurationToTree(Tree tree, Object object, String parentId) {
		
		if (ExposableConfiguration.class.isInstance(object)) {
			String id = UUID.randomUUID().toString();
			
			tree.addItem(id);
			tree.setItemCaption(id, "Configuration");
			tree.setParent(id, parentId);
			
			ExposableConfiguration<?> exposableConfiguration = (ExposableConfiguration<?>) object;
			
			Object acceptorConfig = exposableConfiguration.getConfiguration();
			List<Field> fields = Helper.getConfigurationFields(acceptorConfig.getClass());
			Helper.addFieldsToTree(tree, fields, acceptorConfig, id);
		}
	}
	
	/**
	 * Helper method. Given a list of fields, it adds a nodes to the tree hierarchy with the label of the 
	 * field (it uses the Helper.getFieldLabel(...) method) and the value.
	 * 
	 * @param tree the Tree object to which we are adding the fields 
	 * @param fields the list of Field objects from which we are retrieving the label and the value.
	 * @param object the Object from which we are retriving the fields values.
	 * @param parentId the parentId to which the fields are going  to be added.
	 */
	private static void addFieldsToTree(Tree tree, List<Field> fields, Object object, String parentId) {
		
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				Object value = field.get(object);
				
				String id = getFieldLabel(field) + ": " + value;
				tree.addItem(id);
				tree.setParent(id, parentId);
				tree.setChildrenAllowed(id, false);
			} catch (Exception e) {
				log.error("Exception retrieving configuration field '" + field.getName() + "': " + e.getMessage(), e);
			} 
			
		}
	}
	
	/**
	 * Helper method. Checks if the field has a {@link Label} annotation and returns its value, otherwise, the name of the field
	 * is returned.
	 * 
	 * @param field the field from which we retrieve the label.
	 * @return a String that is used as the label of the field.
	 */
	private static String getFieldLabel(Field field) {
		
		Label labelAnnotation = field.getAnnotation(Label.class);
		if (labelAnnotation != null) {
			return labelAnnotation.value();
		}
		
		return field.getName();
	}
	
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
