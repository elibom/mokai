package org.mokai.config.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.mokai.Action;
import org.mokai.ExecutionException;
import org.mokai.ExposableConfiguration;
import org.mokai.Processor;
import org.mokai.RoutingEngine;
import org.mokai.plugin.PluginMechanism;

/**
 * Utility class to handle common tasks used by the {@link ProcessorConfiguration}
 * and {@link ReceiverConfiguration}.
 * 
 * @author German Escobar
 */
public class XmlConfigurationUtils {

	/**
	 * Converts a String value to the specified class.
	 * 
	 * @param clazz
	 * @param value
	 * @return the converted object from the String value.
	 * @throws NoSuchMethodException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static <T> Object convert(Class<T> clazz, String value) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException {
		
		if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
			return Integer.valueOf(value);
		} else if (Double.class.equals(clazz) || double.class.equals(clazz)) {
			return Double.valueOf(value);
		} else if (Long.class.equals(clazz) || long.class.equals(clazz)) {
			return Long.valueOf(value);
		} else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
			return Boolean.valueOf(value);
		} else if (Byte.class.equals(clazz) || byte.class.equals(clazz)) {
			return Byte.valueOf(value);
		} else if (clazz.isEnum()) {
			try {
				
				Method valueOfMethod = clazz.getMethod("convert", String.class);
				if (!Modifier.isStatic(valueOfMethod.getModifiers())) {
					throw new NoSuchMethodException("method convert must be declared static");
				}
				
				Object ret = valueOfMethod.invoke(null, value);
				return ret;
				
			} catch (InvocationTargetException e) {
				if (e.getCause() != null && RuntimeException.class.isInstance(e.getCause())) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new ExecutionException(e);
				}
			}
		}

		return value;
	}
	
	public static void writeDocument(Document document, String fileName) throws Exception {
			
		// make dirs if necessary
		makeDirs(fileName);
		
		// obtain the output stream
		File filePath = new File(fileName);
		OutputStream out = new FileOutputStream(filePath);
		
		OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(out, format);
        writer.write( document );
        
        out.flush();
        out.close();
        writer.close();
	}
	
	private static void makeDirs(String fileName) {
		// get the last / or \ position, return if not found
		int lastSlashPos = fileName.lastIndexOf("/");
		if (lastSlashPos == -1) {
			lastSlashPos = fileName.lastIndexOf("\\");
			
			if (lastSlashPos == -1) {
				return;
			}
		}
		
		String folderPath = fileName.substring(0, lastSlashPos);
		File folderFile = new File(folderPath);
		folderFile.mkdirs();
	}
	
	public static void addConfigurationFields(Element element, Object configuration) throws Exception {
		Field[] fields = configuration.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			
			Element propertyElement = element.addElement("property")
				.addAttribute("name", field.getName());
			
			if (field.getType().equals(Processor.class)) {
				/*Processor processor = (Processor) field.get(configuration);
				propertyElement.addElement("connector")
					.addAttribute("id", connector.getId());*/
			} else {
				Object obj = field.get(configuration);
				if (obj != null) {
					propertyElement.setText(field.get(configuration).toString());
				} else {
					propertyElement.addElement("null");					
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setConfigurationField(Element element, Object configuration, 
			RoutingEngine routingEngine) throws IllegalAccessException, SecurityException, 
			NoSuchFieldException, IllegalArgumentException, NoSuchMethodException {
		
		if (element.getName().equals("property")) {
			
			Field field = retrieveField(element, configuration);
			
			// check if we have a value attribute
			String valueAttribute = element.attributeValue("value");
			if (valueAttribute != null) {
				setValue(field, configuration, XmlConfigurationUtils.convert(field.getType(), valueAttribute));
				return;
			}
			
			// check if the value was set directly
			if (element.isTextOnly()) {
				String valueText = element.getText();
				if (valueText != null) {
					setValue(field, configuration, XmlConfigurationUtils.convert(field.getType(), valueText));
				}
				return;
			}
			
			// retrieve the child element 
			Element elementValue = (Element) element.elementIterator().next();
			if (elementValue.getName().equals("value")) {
				
				String valueText = element.getText();
				if (valueText != null && !"".equals(valueText)) {
					setValue(field, configuration, XmlConfigurationUtils.convert(field.getType(), valueText));
				}
				
			} else if (elementValue.getName().equals("null")) {
				
				setValue(field, configuration, null);
				
			}
			
		} else if (element.getName().equals("mapProperty")) {
			
			Field field = retrieveField(element, configuration);
			
			if (!Map.class.isAssignableFrom(field.getType())) {
				throw new IllegalArgumentException("field " + field.getName() + " is not a Map");
			}
			
			Map<String,String> map = new HashMap<String,String>();
			
			Iterator iterator = element.elementIterator();
			while (iterator.hasNext()) {
				Element entry = (Element) iterator.next();
				String entryKey = entry.attributeValue("key");
				String entryValue = entry.attributeValue("value");
				
				map.put(entryKey, entryValue);
			}
			
			setValue(field, configuration, map);
		}
		
	}
	
	private static Field retrieveField(Element element, Object object) throws SecurityException, NoSuchFieldException {
		// retrieve the field
		String nameAttribute = element.attributeValue("name");
		Field field = object.getClass().getDeclaredField(nameAttribute);
		
		return field;
	}
	
	private static void setValue(Field field, Object object, Object value) throws IllegalAccessException {
		field.setAccessible(true);
		field.set(object, value);
	}
	
	@SuppressWarnings("unchecked")
	public static void setConfigurationFields(Element parentElement, Object configuration, 
			RoutingEngine routingEngine) throws SecurityException, IllegalArgumentException, 
			IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
		
		Iterator properties = parentElement.elementIterator();
		while (properties.hasNext()) {
			Element propertyElement = (Element) properties.next();
			setConfigurationField(propertyElement, configuration, routingEngine);
		}
	}
	
	/**
	 * Helper method to build {@link Action}s from an XML element.
	 * 
	 * @param routingEngine 
	 * @param pluginMechanism
	 * @param actionsElement
	 * @return a list of {@link Action} objects or an empty list.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static List<Action> buildActions(RoutingEngine routingEngine, 
			PluginMechanism pluginMechanism, Element actionsElement) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchFieldException, NoSuchMethodException {
		
		List<Action> actions = new ArrayList<Action>();
		
		if (actionsElement == null) {
			return actions;
		}
		
		Iterator iterator = actionsElement.elementIterator();
		while (iterator.hasNext()) {
			Element actionElement = (Element) iterator.next();
			
			// create action instance
			String className = actionElement.attributeValue("className");
			Class<? extends Action> actionClass = null;
			if (pluginMechanism != null) {
				actionClass = (Class<? extends Action>) pluginMechanism.loadClass(className);
			}
			
			if (actionClass == null) {
				actionClass = (Class<? extends Action>) Class.forName(className);
			}
			
			Action action = actionClass.newInstance();
			
			if (ExposableConfiguration.class.isInstance(action)) {
				ExposableConfiguration<?> exposableAction = (ExposableConfiguration<?>) action;
				
				XmlConfigurationUtils.setConfigurationFields(actionElement, exposableAction.getConfiguration(), routingEngine);
			}
			
			actions.add(action);
		}
		
		return actions;
	}
}
