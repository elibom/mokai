package org.mokai.impl.camel.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.mokai.Action;
import org.mokai.Message;
import org.mokai.annotation.Resource;
import org.mokai.impl.camel.ResourceRegistry;
import org.mokai.persist.MessageStore;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class CamelBaseTest {

	protected ResourceRegistry resourceRegistry;
	
	protected ProducerTemplate camelProducer;
	
	@BeforeMethod
	public void beforeTest() throws Exception {
		CamelContext camelContext = new DefaultCamelContext();
		camelContext.addComponent("activemq", defaultJmsComponent());
		
		// workaround to start the activemq broker - sucks
		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("activemq:not").to("direct:guash");
			}			
		});
		
		camelContext.start();
		
		resourceRegistry = new ResourceRegistry();
		resourceRegistry.putResource(CamelContext.class, camelContext);
		
		camelProducer = camelContext.createProducerTemplate();
	}
	
	protected JmsComponent defaultJmsComponent() throws Exception{
		// a simple activemq connection factory
		ActiveMQConnectionFactory connectionFactory = 
			new ActiveMQConnectionFactory("vm://broker1?broker.persistent=false");
                connectionFactory.setTrustAllPackages(true);
		connectionFactory.setDispatchAsync(false);
		
		// create the default JmsComponent 
		JmsComponent jmsComponent = new JmsComponent();
		jmsComponent.setConnectionFactory(connectionFactory);
		
		return jmsComponent; 
	}
	
	@AfterMethod
	public void afterTest() throws Exception {
		CamelContext camelContext = resourceRegistry.getResource(CamelContext.class);
		if (camelContext != null) {
			camelContext.stop();
		}
	}
	
	/**
	 * A simple class to test that actions are being called.
	 * 
	 * @author German Escobar
	 */
	protected class MockAction implements Action {
		public int changed;
		
		/**
		 * This field is here to test inject resources
		 */
		@Resource
		private MessageStore messageStore;
	
		@Override
		public void execute(Message message) {
			changed++;	
		}
		
		public int getChanged() {
			return changed;
		}

		public MessageStore getMessageStore() {
			return messageStore;
		}
	}
	
}
