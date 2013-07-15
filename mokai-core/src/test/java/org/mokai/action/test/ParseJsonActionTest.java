package org.mokai.action.test;

import java.util.List;
import java.util.Map;

import org.mokai.Message;
import org.mokai.action.JsonParserAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParseJsonActionTest {

	@Test
	public void shouldParseSimpleJson() throws Exception {
		JsonParserAction action = new JsonParserAction();
		action.setField("body");

		Message message = new Message();
		message.setProperty("body", "{ \"number\": 2, \"boolean\": false, \"string\": \"this is a test\", \"double\": 2.23 }");

		action.execute(message);

		Assert.assertEquals(message.getProperty("number", int.class).intValue(), 2);
		Assert.assertEquals(message.getProperty("boolean", Boolean.class).booleanValue(), false);
		Assert.assertEquals(message.getProperty("string", String.class), "this is a test");
		Assert.assertEquals(message.getProperty("double", Double.class).doubleValue(), 2.23);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void shouldParseComplexJson() throws Exception {
		JsonParserAction action = new JsonParserAction();
		action.setField("body");

		Message message = new Message();
		message.setProperty("body", "[ 2, 3, 4 ]");

		action.execute(message);

		message.setProperty("body", "{ \"numbers\": { \"one\": [1, 2, 3], \"two\": [2, 3, 4], \"three\": [3, 4, 5] } }");

		action.execute(message);

		Assert.assertNotNull(message.getProperty("numbers"));
		Map<String,Object> numbers = message.getProperty("numbers", Map.class);
		List list = (List) numbers.get("one");
		Assert.assertNotNull(list);
		Assert.assertEquals(list.get(0), 1);
		Assert.assertEquals(list.get(1), 2);
		Assert.assertEquals(list.get(2), 3);

	}

	@Test
	public void shouldNotFailWithInvalidJson() throws Exception {
		JsonParserAction action = new JsonParserAction();
		action.setField("body");

		Message message = new Message();
		message.setProperty("body", "hola");
	}
}
