package org.mokai.connector.camel.jetty;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.mokai.spi.Configurable;
import org.mokai.spi.ExposableConfiguration;
import org.mokai.spi.MessageProducer;
import org.mokai.spi.annotation.Resource;
import org.mokai.spi.message.SmsMessage;

/**
 * 
 * @author German Escobar
 */
public class JettyConnector implements Configurable, ExposableConfiguration<JettyConfiguration> {
	
	@Resource
	private MessageProducer messageProducer;
	
	private JettyConfiguration configuration;
	
	private CamelContext camelContext;

	public JettyConnector() {
		this(new JettyConfiguration());
	}
	
	public JettyConnector(JettyConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void configure() throws Exception {
		camelContext = new DefaultCamelContext();
		
		final String uri = "jetty:http://0.0.0.0:" + getConfiguration().getPort() 
			+ "/" + getConfiguration().getContext();
		

		camelContext.addRoutes(new RouteBuilder(){
	
			@Override
			public void configure() throws Exception {
				from(uri).process(new Processor() {

					@Override
					public void process(Exchange exchange) throws Exception {
						String to = (String) exchange.getIn().getHeader("to");
						String from = (String) exchange.getIn().getHeader("from");
						String message = (String) exchange.getIn().getHeader("message");
						String accountId = (String) exchange.getIn().getHeader("account");
						String password = (String) exchange.getIn().getHeader("password");
							
						SmsMessage sms = new SmsMessage();
						sms.setTo(to);
						sms.setFrom(from);
						sms.setText(message);
							
						if (accountId != null) {
							sms.setAccountId(accountId);
							sms.setPassword(password);
						}
							
						messageProducer.produce(sms);
					}
						
				});
			}
				
		});
			
		camelContext.start();

	}



	@Override
	public void destroy() throws Exception {
		try {
			camelContext.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public JettyConfiguration getConfiguration() {
		return configuration;
	}

}
