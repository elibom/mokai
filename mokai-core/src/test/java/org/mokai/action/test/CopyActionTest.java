package org.mokai.action.test;

import org.mokai.Message;
import org.mokai.action.CopyAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CopyActionTest {

	@Test
	public void shouldCopyProperty() throws Exception {
		
		CopyAction action = new CopyAction();
		action.setFrom("prop-1");
		action.setTo("prop-2");
		
		Message message = new Message();
		message.setProperty("prop-1", "value");
		
		action.execute(message);
		
		Assert.assertEquals(message.getProperty("prop-1"), "value");
		Assert.assertEquals(message.getProperty("prop-2"), "value");
		
	}
	
	@Test
	public void shouldCopyPropertyAndDeleteFrom() throws Exception {
		
		CopyAction action = new CopyAction();
		action.setFrom("prop-1");
		action.setTo("prop-2");
		action.setDeleteFrom(true);
		
		Message message = new Message();
		message.setProperty("prop-1", "value");
		
		action.execute(message);
		
		Assert.assertNull(message.getProperty("prop-1"));
		Assert.assertEquals(message.getProperty("prop-2"), "value");
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldThrowExceptionIfFromNotSet() throws Exception {
		
		CopyAction action = new CopyAction();
		action.setFrom("prop-2");
		
		action.execute(new Message());
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldThrowExceptionIfToNotSet() throws Exception {
		
		CopyAction action = new CopyAction();
		action.setTo("prop-2");
		
		action.execute(new Message());
		
	}
}
