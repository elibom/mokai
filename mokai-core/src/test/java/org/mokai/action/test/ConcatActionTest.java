package org.mokai.action.test;

import java.util.ArrayList;
import java.util.List;

import org.mokai.Message;
import org.mokai.action.ConcatAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConcatActionTest {

	@Test
	public void shouldConcat2Fields() throws Exception {
		
		List<String> fields = new ArrayList<String>();
		fields.add("field1");
		fields.add("field2");
		
		ConcatAction action = new ConcatAction();
		action.setFields(fields);
		action.setDestField("field3");
		action.setSeparator(",");
		
		Message message = new Message();
		message.setProperty("field1", "value1");
		message.setProperty("field2", "value2");
		
		action.execute(message);
		
		Assert.assertEquals(message.getProperty("field3", String.class), "value1,value2");
		
	}
}
