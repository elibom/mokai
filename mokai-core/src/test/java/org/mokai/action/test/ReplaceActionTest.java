package org.mokai.action.test;

import java.util.HashMap;
import java.util.Map;

import org.mokai.Message;
import org.mokai.action.ReplaceAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReplaceActionTest {
	
	@Test
	public void shouldReplaceStrings() throws Exception {
		
		ReplaceAction action = new ReplaceAction();
		action.setField("message");
		
		Map<String,String> replace = new HashMap<String,String>();
		replace.put("‡", "a");
		replace.put("Ž", "e");
		replace.put("–", "nh");
		replace.put("—", "");
		replace.put("badword", "_");
		action.setReplace(replace);
		
		Message message = new Message();
		message.setProperty("message", "‡Ž—– badword");
		
		action.execute(message);
		
		Assert.assertEquals(message.getProperty("message", String.class), "aenh _");
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailIfFieldIsNotSet() throws Exception {
		
		ReplaceAction action = new ReplaceAction();
		
		action.execute(new Message());
	}

}
