package org.mokai.config.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.mokai.Processor;
import org.mokai.RoutingEngine;


public class XmlUtils {

	/**
	 * Converts a String value to the specified class.
	 * 
	 * @param clazz
	 * @param value
	 * @return
	 */
	public static <T> Object convert(Class<T> clazz, String value) {
		
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
					// TODO add null element					
				}
			}
		}
	}
	
	public static void setConfigurationField(Element element, Object configuration, RoutingEngine routingEngine) throws Exception {
		String name = element.attributeValue("name");
		
		Field field = configuration.getClass().getDeclaredField(name);
		
		Object propertyValue = null;
		if (element.isTextOnly()) {
			String strValue = element.getText();
			// TODO fix this when we have a null element
			if (strValue != null && !"".equals(strValue)) {
				propertyValue = XmlUtils.convert(field.getType(), strValue);
			}
		} else {
			Element elementValue = (Element) element.elementIterator().next();
			
			/*if (elementValue.getName().equals("connector")) {
				String connectorId = elementValue.attributeValue("id");
				propertyValue = routingContext.getConnector(connectorId);
			}*/
		}
			
		field.setAccessible(true);
		field.set(configuration, propertyValue);
	}
	
	@SuppressWarnings("unchecked")
	public static void setConfigurationFields(Element parentElement, Object configuration, 
			RoutingEngine routingEngine) throws Exception {
		
		Iterator properties = parentElement.elementIterator();
		while (properties.hasNext()) {
			Element propertyElement = (Element) properties.next();
			setConfigurationField(propertyElement, configuration, routingEngine);
		}
	}
}
