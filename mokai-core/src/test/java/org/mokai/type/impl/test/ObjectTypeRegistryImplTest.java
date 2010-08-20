package org.mokai.type.impl.test;

import java.util.Collection;

import junit.framework.Assert;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mokai.spi.type.AcceptorType;
import org.mokai.spi.type.ProcessorType;
import org.mokai.spi.type.ReceiverType;
import org.mokai.type.ObjectTypeRegistry;
import org.mokai.type.impl.ObjectTypeRegistryImpl;
import org.mokai.types.mock.MockAcceptor;
import org.mokai.types.mock.MockConfigurableAcceptor;
import org.mokai.types.mock.MockConfigurableConnector;
import org.mokai.types.mock.MockConnector;
import org.testng.annotations.Test;

public class ObjectTypeRegistryImplTest {

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullReceiverType() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addReceiverType(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullReceiverTypeName() throws Exception {
		ReceiverType type = mockReceiverType(null, null, MockConnector.class);
		
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addReceiverType(type);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullReceiverTypeConnector() throws Exception {
		ReceiverType type = mockReceiverType("Mock", null, null);
		
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addReceiverType(type);
	}
	
	@Test
	public void testAddReceiverTypes() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		
		// add a receiver type
		ReceiverType type1 = mockReceiverType("Mock", "", MockConnector.class);
		registry.addReceiverType(type1);
		
		// check that we have only 1 receiver type
		Collection<ReceiverType> receiverTypes = registry.getReceiverTypes();
		Assert.assertEquals(1, receiverTypes.size());
		Assert.assertEquals(type1, receiverTypes.iterator().next());
		Assert.assertEquals(type1, registry.getReceiverType(MockConnector.class));
		
		// add another receiver type
		ReceiverType type2 = mockReceiverType("ConfigurableMock", "", MockConfigurableConnector.class);
		registry.addReceiverType(type2);
		
		// check that we have 2 receiver types
		receiverTypes = registry.getReceiverTypes();
		Assert.assertEquals(2, receiverTypes.size());
		Assert.assertEquals(type2, registry.getReceiverType(MockConfigurableConnector.class));
		
	}
	
	@Test
	public void testOverwriteReceiverType() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		
		// add a receiver type
		ReceiverType type1 = mockReceiverType("Mock", "", MockConnector.class);
		registry.addReceiverType(type1);
		
		// add another receiver type with the same class
		ReceiverType type2 = mockReceiverType("AnotherMock", "", MockConnector.class);
		registry.addReceiverType(type2);
		
		// check that we have only 1
		Collection<ReceiverType> receiverTypes = registry.getReceiverTypes();
		Assert.assertEquals(1, receiverTypes.size());
		Assert.assertEquals(type2, receiverTypes.iterator().next());
		Assert.assertEquals(type2, registry.getReceiverType(MockConnector.class));
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullAcceptorType() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addAcceptorType(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullAcceptorTypeName() throws Exception {
		AcceptorType type = mockAcceptorType(null, null, MockAcceptor.class);
		
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addAcceptorType(type);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullAcceptorTypeClass() throws Exception {
		AcceptorType type = mockAcceptorType("Mock", null, null);
		
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addAcceptorType(type);
	}
	
	@Test
	public void testAddAcceptorTypes() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		
		// add an acceptor type
		AcceptorType type1 = mockAcceptorType("Mock", "", MockAcceptor.class);
		registry.addAcceptorType(type1);
		
		// check that we have only 1 acceptor type
		Collection<AcceptorType> acceptorTypes = registry.getAcceptorsTypes();
		Assert.assertEquals(1, acceptorTypes.size());
		Assert.assertEquals(type1, acceptorTypes.iterator().next());
		Assert.assertEquals(type1, registry.getAcceptorType(MockAcceptor.class));
		
		// add another acceptor type
		AcceptorType type2 = mockAcceptorType("Mock", "", MockConfigurableAcceptor.class);
		registry.addAcceptorType(type2);
		
		// check that we have 2 acceptor types
		acceptorTypes = registry.getAcceptorsTypes();
		Assert.assertEquals(2, acceptorTypes.size());
		Assert.assertEquals(type2, registry.getAcceptorType(MockConfigurableAcceptor.class));
	}
	
	@Test
	public void testOverwriteAcceptorTypes() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		
		// add an acceptor type
		AcceptorType type1 = mockAcceptorType("Mock", "", MockAcceptor.class);
		registry.addAcceptorType(type1);
		
		// add another acceptor type with the same class
		AcceptorType type2 = mockAcceptorType("AnotherMock", "", MockAcceptor.class);
		registry.addAcceptorType(type2);
		
		// check that we have only the second one
		Collection<AcceptorType> acceptorTypes = registry.getAcceptorsTypes();
		Assert.assertEquals(1, acceptorTypes.size());
		Assert.assertEquals(type2, acceptorTypes.iterator().next());
		Assert.assertEquals(type2, registry.getAcceptorType(MockAcceptor.class));
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullProcessorType() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addProcessorType(null);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullProcessorTypeName() throws Exception {
		ProcessorType type = mockProcessorType(null, null, MockConnector.class);
		
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addProcessorType(type);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailNullProcessorTypeConnector() throws Exception {
		ProcessorType type = mockProcessorType("Mock", null, null);
		
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		registry.addProcessorType(type);
	}
	
	@Test
	public void testAddProcessorTypes() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		
		// add a processor type
		ProcessorType type1 = mockProcessorType("Mock", "", MockConnector.class);
		registry.addProcessorType(type1);
		
		// check that we have only 1 processor type
		Collection<ProcessorType> processorTypes = registry.getProcessorTypes();
		Assert.assertEquals(1, processorTypes.size());
		Assert.assertEquals(type1, processorTypes.iterator().next());
		Assert.assertEquals(type1, registry.getProcessorType(MockConnector.class));
		
		// add another processor type
		ProcessorType type2 = mockProcessorType("ConfigurableMock", "", MockConfigurableConnector.class);
		registry.addProcessorType(type2);
		
		// check that we have 2 processor types
		processorTypes = registry.getProcessorTypes();
		Assert.assertEquals(2, processorTypes.size());
		Assert.assertEquals(type2, registry.getProcessorType(MockConfigurableConnector.class));
		
	}
	
	@Test
	public void testOverwriteProcessorType() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		
		// add a processor type
		ProcessorType type1 = mockProcessorType("Mock", "", MockConnector.class);
		registry.addProcessorType(type1);
		
		// add another processor type with the same class
		ProcessorType type2 = mockProcessorType("AnotherMock", "", MockConnector.class);
		registry.addProcessorType(type2);
		
		// check that we have only 1
		Collection<ProcessorType> processorTypes = registry.getProcessorTypes();
		Assert.assertEquals(1, processorTypes.size());
		Assert.assertEquals(type2, processorTypes.iterator().next());
		Assert.assertEquals(type2, registry.getProcessorType(MockConnector.class));
		
	}
	
	@Test
	public void testRetrieveNonExistentTypes() throws Exception {
		ObjectTypeRegistry registry = new ObjectTypeRegistryImpl();
		
		Assert.assertEquals(0, registry.getReceiverTypes().size());
		Assert.assertEquals(0, registry.getAcceptorsTypes().size());
		Assert.assertEquals(0, registry.getProcessorTypes().size());
		
		Assert.assertNull(registry.getReceiverType(MockConnector.class));
		Assert.assertNull(registry.getAcceptorType(MockAcceptor.class));
		Assert.assertNull(registry.getProcessorType(MockConnector.class));
	}
	
	private ReceiverType mockReceiverType(final String name, final String description, final Class<?> connectorClass) {
		ReceiverType type = Mockito.mock(ReceiverType.class);
		Mockito.when(type.getName()).thenReturn(name);
		Mockito.when(type.getDescription()).thenReturn(description);
		Mockito.when(type.getConnectorClass()).thenAnswer(new Answer<Class<?>>() {

			@Override
			public Class<?> answer(InvocationOnMock invocation)	throws Throwable {
				return connectorClass;
			}
			
		});
		
		return type;
	}
	
	private AcceptorType mockAcceptorType(final String name, final String description, final Class<?> acceptorClass) {
		AcceptorType type = Mockito.mock(AcceptorType.class);
		Mockito.when(type.getName()).thenReturn(name);
		Mockito.when(type.getDescription()).thenReturn(description);
		Mockito.when(type.getAcceptorClass()).thenAnswer(new Answer<Class<?>>() {

			@Override
			public Class<?> answer(InvocationOnMock invocation) throws Throwable {
				return acceptorClass;
			}
			
		});
		
		return type;
	}
	
	private ProcessorType mockProcessorType(final String name, final String description, final Class<?> connectorClass) {
		ProcessorType type = Mockito.mock(ProcessorType.class);
		Mockito.when(type.getName()).thenReturn(name);
		Mockito.when(type.getDescription()).thenReturn(description);
		Mockito.when(type.getProcessorClass()).thenAnswer(new Answer<Class<?>>() {

			@Override
			public Class<?> answer(InvocationOnMock invocation)	throws Throwable {
				return connectorClass;
			}
			
		});
		
		return type;
	}
}
