package org.mokai.impl.camel.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.mokai.Action;
import org.mokai.Message;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class CamelBaseTest {

	protected CamelContext camelContext;
	
	protected ProducerTemplate camelProducer;
	
	@BeforeMethod
	public void beforeTest() throws Exception {
		camelContext = new DefaultCamelContext();
		camelContext.addComponent("activemq", defaultJmsComponent());
		
		camelContext.start();
		
		camelProducer = camelContext.createProducerTemplate();
	}
	
	protected JmsComponent defaultJmsComponent() {
		// a simple activemq connection factory
		ActiveMQConnectionFactory connectionFactory = 
			new ActiveMQConnectionFactory("vm://broker1?broker.persistent=false");
		connectionFactory.setDispatchAsync(false);
		
		// create the default JmsComponent 
		JmsComponent jmsComponent = new JmsComponent();
		jmsComponent.setConnectionFactory(connectionFactory);
		
		return jmsComponent; 
	}
	
	@AfterMethod
	public void afterTest() throws Exception {
		camelContext.stop();
	}
	
	/**
	 * A simple class to test that actions are being called.
	 * 
	 * @author German Escobar
	 */
	protected class MockAction implements Action {
		public int changed;
	
		@Override
		public void execute(Message message) {
			changed++;	
		}
		
		public int getChanged() {
			return changed;
		}
	}
	
}
