package org.mokai;

import java.util.Map;

import org.mokai.Message.Direction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageTest {

	@Test
	public void shouldInitializeReferenceAndDirection() throws Exception {
		
		Message message = new Message();
		
		Assert.assertNull(message.getDestination());
		Assert.assertNull(message.getSource());
		Assert.assertNotNull(message.getReference());
		Assert.assertEquals(message.getDirection(), Direction.UNKNOWN);
		
		Assert.assertNull(message.getProperty(Message.DESTINATION_PROPERTY));
		Assert.assertNull(message.getProperty(Message.SOURCE_PROPERTY));
		Assert.assertNotNull(message.getProperty(Message.REFERENCE_PROPERTY));
		Assert.assertEquals(message.getProperty(Message.DIRECTION_PROPERTY, Direction.class), Direction.UNKNOWN);
		
	}
	
	@Test
	public void shouldReturnUserPropertiesOnly() throws Exception {
		
		Message message = new Message();
		
		Assert.assertNotNull(message.getProperties());
		Assert.assertEquals(message.getProperties().size(), 0);
		
		message.setProperty("test-1", true);
		message.setProperty("test-2", "true");
		
		Assert.assertEquals(message.getProperties().size(), 2);
	}
	
	@Test(expectedExceptions=UnsupportedOperationException.class)
	public void shouldThrowExceptionIfPropertiesAreModified() throws Exception {
		
		Message message = new Message();
		
		Map<String,Object> properties = message.getProperties();
		properties.remove("test");
		
	}
	
}
