package org.mokai.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mokai.ExposableConfiguration;
import org.mokai.ui.annotation.ConnectorsList;
import org.mokai.ui.annotation.Label;
import org.mokai.ui.field.CheckBoxField;
import org.mokai.ui.field.InputField;
import org.mokai.ui.field.SelectConnectorsField;
import org.mokai.ui.field.SelectValuesField;
import org.mokai.ui.field.TextField;

public class InputFields {
	
	private Map<Class<?>,List<InputField>> inputFields = new HashMap<Class<?>,List<InputField>>();

	public List<InputField> createFields(Class<?> clazz) {
		List<InputField> fields = inputFields.get(clazz); 
		if (fields != null) {
			return fields;
		}
		
		fields = new ArrayList<InputField>();
		
		Field[] classFields = clazz.getDeclaredFields();
		for (Field classField : classFields) {
			String name = classField.getName();
			String label = name;
			
			Label labelAnnotation = classField.getAnnotation(Label.class);
			if (labelAnnotation != null) {
				label = labelAnnotation.value();
			}
			
			boolean fieldCreated = false;
			
			// SelectValuesField
			org.mokai.ui.annotation.List listAnnotation = classField.getAnnotation(org.mokai.ui.annotation.List.class);
			if (listAnnotation != null) {
				fieldCreated = true;
				
				SelectValuesField<String> svf = new SelectValuesField<String>(name, label);
				svf.setItems(listAnnotation.value());
				fields.add(svf);
			}
			
			// SelectConnectorsField
			ConnectorsList connectorsAnnotation = classField.getAnnotation(ConnectorsList.class);
			if (connectorsAnnotation != null) {
				fieldCreated = true;

				SelectConnectorsField scf = new SelectConnectorsField(name, label);
				fields.add(scf);
			}
			
			// TextField
			if (!fieldCreated) {
				if (classField.getType().equals(boolean.class) ||
						classField.getType().equals(Boolean.class)) {
					CheckBoxField checkBoxField = new CheckBoxField(name, label);
					fields.add(checkBoxField);
				} else {
					TextField inputField = new TextField(name, label);
					fields.add(inputField);
				}
			}
		}
		
		inputFields.put(clazz, fields);
		
		return fields;
	}
	
	@SuppressWarnings("unchecked")
	public static Class<?> getConfigurationClass(Class<? extends ExposableConfiguration> clazz) throws Exception {
		Method method = clazz.getMethod("getConfiguration");
		
		Class<?> returnClass = (Class<?>) method.getGenericReturnType();
		
		return returnClass;
	}
}
