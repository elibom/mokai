package org.mokai.type.impl.test;

import java.util.Set;

import junit.framework.Assert;

import org.mokai.type.AcceptorType;
import org.mokai.type.ActionType;
import org.mokai.type.ProcessorType;
import org.mokai.type.ReceiverType;
import org.mokai.type.impl.StandardTypeLoader;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockAction;
import org.mokai.types.mock.MockConnector;
import org.testng.annotations.Test;

public class StandardTypeLoaderTest {
	
	//private final String SERVICES_PATH = "src/test/resources/META-INF/services/"; 

	@Test
	public void testLoadAcceptorTypes() throws Exception {
		StandardTypeLoader typeLoader = new StandardTypeLoader();
		
		Set<AcceptorType> acceptorTypes = typeLoader.loadAcceptorTypes();
		Assert.assertTrue(acceptorTypes.size() > 0);
		
		AcceptorType test = new AcceptorType("", "", MockAcceptor.class);
		Assert.assertTrue(acceptorTypes.contains(test));
	}
	
	@Test
	public void testLoadActionTypes() throws Exception {
		StandardTypeLoader typeLoader = new StandardTypeLoader();
		
		Set<ActionType> actionTypes = typeLoader.loadActionTypes();
		Assert.assertTrue(actionTypes.size() > 0);
		
		ActionType test = new ActionType("", "", MockAction.class);
		Assert.assertTrue(actionTypes.contains(test));
	}
	
	@Test
	public void testLoadReceiverTypes() throws Exception {
		StandardTypeLoader typeLoader = new StandardTypeLoader();
		
		Set<ReceiverType> receiverTypes = typeLoader.loadReceiverTypes();
		Assert.assertTrue(receiverTypes.size() > 0);
		
		ReceiverType test = new ReceiverType("", "", MockConnector.class);
		Assert.assertTrue(receiverTypes.contains(test));
	}
	
	@Test
	public void testLoadProcessorTypes() throws Exception {
		StandardTypeLoader typeLoader = new StandardTypeLoader();
		
		Set<ProcessorType> processorTypes = typeLoader.loadProcessorTypes();
		Assert.assertTrue(processorTypes.size() > 0);
		
		ProcessorType test = new ProcessorType("", "", MockConnector.class);
		Assert.assertTrue(processorTypes.contains(test));
	}
	
	/*private void createServiceFile(String fileName, String content) throws Exception {
		
		BufferedWriter out = null;
		
		try {
		    out = new BufferedWriter(new FileWriter(fileName));
		    out.write(content);
		    
		} catch (IOException e) {
			
		} finally {
			if (out != null) {
				try { out.close(); } catch (Exception e) {}
			}
		}
	}*/
}
