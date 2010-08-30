package org.mokai.boot;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// start spring context
		String[] configLocations = new String[] { 
				"conf/core-context.xml", "conf/admin-console-context.xml" 
			};
		
		final ConfigurableApplicationContext springContext = new FileSystemXmlApplicationContext(configLocations);
		
		// add a shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(){
		    public void run() {
		    	springContext.close();
		    }
		});
		
		// create jetty receiver
		/*JettyConfiguration jettyConfiguration = new JettyConfiguration();
		jettyConfiguration.setPort("9080");
		jettyConfiguration.setContext("test");
		JettyConnector jettyConnector = new JettyConnector(jettyConfiguration);
		
		// create smpp processor
		SmppConfiguration smppConfiguration = new SmppConfiguration();
		smppConfiguration.setHost("localhost");
		smppConfiguration.setPort(8321);
		smppConfiguration.setSystemId("test");
		smppConfiguration.setPassword("test");
		SmppConnector smppConnector = new SmppConnector(smppConfiguration);
		
		// create the routing engine
		final CamelRoutingEngine routingEngine = new CamelRoutingEngine();
		routingEngine.start();
		
		// add the receiver
		routingEngine.createReceiver("jetty", jettyConnector);
		
		// add the processor
		ProcessorService comcelProcessor = 
			routingEngine.createProcessor("comcel", 0, smppConnector);
		
		comcelProcessor.addAcceptor(new Acceptor() {

			@Override
			public boolean accepts(Message message) {
				return true;
			}
			
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
		    public void run() {
		    	routingEngine.stop();
		    }
		});*/		
	}

}
