package org.mokai.config.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.mokai.ExecutionException;
import org.mokai.Processor;

/**
 * Utility class to handle common tasks used by the {@link ConnectionsConfiguration}
 * and {@link ApplicationsConfiguration}.
 *
 * @author German Escobar
 */
public final class XmlConfigurationUtils {

	/**
	 * This class is not supposed to be instantiated.
	 */
	private XmlConfigurationUtils() {}

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
	public static <T> Object convert(Class<T> clazz, Object value) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException {
		if (value == null) {
			return null;
		}

		if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
			return Integer.valueOf(value.toString());
		} else if (Double.class.equals(clazz) || double.class.equals(clazz)) {
			return Double.valueOf(value.toString());
		} else if (Long.class.equals(clazz) || long.class.equals(clazz)) {
			return Long.valueOf(value.toString());
		} else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
			return Boolean.valueOf(value.toString());
		} else if (Byte.class.equals(clazz) || byte.class.equals(clazz)) {
			return Byte.valueOf(value.toString());
		} else if (clazz.isEnum()) {
			try {
				Method valueOfMethod = clazz.getMethod("convert", String.class);
				if (!Modifier.isStatic(valueOfMethod.getModifiers())) {
					throw new NoSuchMethodException("method convert must be declared static");
				}

				return valueOfMethod.invoke(null, value.toString());
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

	public static Field retrieveField(Element element, Object object) throws SecurityException, NoSuchFieldException {
		// retrieve the field
		String nameAttribute = element.attributeValue("name");
		Field field = object.getClass().getDeclaredField(nameAttribute);

		return field;
	}

	public static void setValue(Field field, Object object, Object value) throws IllegalAccessException {
		field.setAccessible(true);
		field.set(object, value);
	}

}
