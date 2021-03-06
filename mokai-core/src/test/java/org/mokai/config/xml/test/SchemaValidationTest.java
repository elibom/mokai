package org.mokai.config.xml.test;

import java.io.File;

import org.dom4j.io.SAXReader;
import org.mokai.config.xml.SchemaEntityResolver;
import org.testng.annotations.Test;

public class SchemaValidationTest {

	@Test
	public void shouldLoadValidConnectorsDocument() throws Exception {
		String path = "src/test/resources/schema-test/good-connectors.xml";

		// create the document
		SAXReader reader = new SAXReader();
		reader.setEntityResolver(new SchemaEntityResolver());
		reader.setValidation(true);

		reader.setFeature("http://xml.org/sax/features/validation", true);
		reader.setFeature("http://apache.org/xml/features/validation/schema", true );
		reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

		reader.read(new File(path));
	}

}
