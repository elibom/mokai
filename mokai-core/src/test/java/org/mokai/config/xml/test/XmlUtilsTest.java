package org.mokai.config.xml.test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import junit.framework.Assert;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mokai.config.xml.XmlConfigurationUtils;
import org.mokai.types.mock.MockAcceptorWithEnum.MockEnum;
import org.testng.annotations.Test;

public class XmlUtilsTest {

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailInvalidEnum() throws Exception {
		XmlConfigurationUtils.convert(MockEnum.class, "third");
	}
	
	private Map<String,String> goodMap;
	
	@Test
	public void testMap() throws Exception {
		if (goodMap != null) {
			goodMap.clear();
		}
	
		String xml = "<mapProperty name='goodMap'><entry key='test1' value='test2' /></mapProperty>";
		
		// check root element
		Element mapElement = createElement(xml);
		
		XmlConfigurationUtils.setConfigurationField(mapElement, this, null);
		
		Assert.assertFalse(goodMap.isEmpty());
		Assert.assertEquals("test2", goodMap.get("test1"));
	}
	
	@Test(expectedExceptions=NoSuchFieldException.class)
	public void shouldFailNonExistentProperty() throws Exception {
		String xml = "<property name='nonExistent' value='test' />";
		
		Element propertyElement = createElement(xml);
		
		XmlConfigurationUtils.setConfigurationField(propertyElement, this, null);
	}
	
	public enum FailEnum {
		OPTION_1, OPTION_2;
		
		public FailEnum convert(String option) {
			return OPTION_1;
		}
	}
	
	@SuppressWarnings("unused")
	private FailEnum failEnum;
	
	@Test(expectedExceptions=NoSuchMethodException.class)
	public void shouldFailEnumWithoutConverter() throws Exception {
		String xml = "<property name='failEnum' value='option1' />";
		
		Element propertyElement = createElement(xml);
		
		XmlConfigurationUtils.setConfigurationField(propertyElement, this, null);
	}
	
	public enum GoodEnum {
		OPTION_1, OPTION_2;
		
		public static GoodEnum convert(String option) {
			if (option.equals("option1")) {
				return OPTION_1;
			} else if (option.equals("option2")) {
				return OPTION_2;
			}
			
			throw new IllegalArgumentException("enum value " + option + " not found");
		}
	}
	
	private GoodEnum goodEnum;
	
	@Test
	public void testGoodEnum() throws Exception {
		String xml = "<property name='goodEnum' value='option1' />";
		
		Element propertyElement = createElement(xml);
		
		XmlConfigurationUtils.setConfigurationField(propertyElement, this, null);
		
		Assert.assertEquals(GoodEnum.OPTION_1, goodEnum);
	}
	
	private Element createElement(String xml) throws DocumentException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		
		return document.getRootElement();
	}
	
}
