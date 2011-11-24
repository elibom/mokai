package org.mokai.action.test;

import org.mokai.Message;
import org.mokai.action.AddSuffixAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AddSuffixActionTest {

	@Test
	public void shouldAddSuffixIfNotPresent() throws Exception {
		
		AddSuffixAction action = new AddSuffixAction();
		action.setField("to");
		action.setSuffix("suf");
		
		Message message = new Message();
		message.setProperty("to", "test");
		
		action.execute(message);
		
		Assert.assertEquals(message.getProperty("to", String.class), "testsuf");
	}
	
	@Test
	public void shouldAddSuffixIfFieldDoesntExists() throws Exception {
		
		AddSuffixAction action = new AddSuffixAction();
		action.setField("to");
		action.setSuffix("suf");
		
		Message message = new Message();
		
		action.execute(message);
		
		Assert.assertEquals(message.getProperty("to", String.class), "suf");
		
	}
	
	@Test
	public void shouldNotAddSuffixIfPresent() throws Exception {
		
		AddSuffixAction action = new AddSuffixAction();
		action.setField("to");
		action.setSuffix("suf");
		
		Message message = new Message();
		message.setProperty("to", "testsuf");
		
		action.execute(message);
		
		Assert.assertEquals(message.getProperty("to", String.class), "testsuf");
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailIfSuffixNotSet() throws Exception {
		
		AddSuffixAction action = new AddSuffixAction();
		action.setField("to");
		
		action.execute(new Message());
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailIfFieldNotSet() throws Exception {
		
		AddSuffixAction action = new AddSuffixAction();
		action.setSuffix("57");
		
		action.execute(new Message());
	}
	
}
