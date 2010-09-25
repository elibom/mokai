package org.mokai.connector.camel.jetty;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.mokai.Configurable;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Receiver;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author German Escobar
 */
@Name("Jetty")
@Description("Receives messages through HTTP")
public class JettyConnector implements Receiver, Configurable, 
		ExposableConfiguration<JettyConfiguration> {
	
	private Logger log = LoggerFactory.getLogger(JettyConnector.class);
	
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
	public final void configure() throws Exception {
		camelContext = new DefaultCamelContext();
		
		final String uri = "jetty:http://0.0.0.0:" + getConfiguration().getPort() 
			+ "/" + getConfiguration().getContext();
		

		camelContext.addRoutes(new RouteBuilder(){
	
			@Override
			public void configure() throws Exception {
				from(uri).process(new Processor() {

					@Override
					public void process(Exchange exchange) throws Exception {
						
						Message message = new Message();
						
						// retrieve the type of the message
						String type = (String) exchange.getIn().getHeader("type");
						if (type == null) {
							message.setType(Message.SMS_TYPE);
						} else {
							message.setType(type);
						}
						
						// retrieve the reference of the message
						String reference = (String) exchange.getIn().getHeader("reference");
						if (reference != null) {
							message.setReference(reference);
						}
						
						// retrieve the query part of the request
						String query = (String) exchange.getIn().getHeader("CamelHttpQuery");
						
						// if the query is not null or empty, parse
						if (query != null && !"".equals(query)) {
							
							// load the query parameters in a properties object
							query = query.replaceAll("&", "\n");
							ByteArrayInputStream inputStream = new ByteArrayInputStream(query.getBytes());
							Properties parameters = new Properties();
							parameters.load(inputStream);
							
							// iterate through the parameters and add them to the message properties
							for (Map.Entry<Object,Object> entry : parameters.entrySet()) {

								// by default set the key to the header value 
								String key = (String) entry.getKey();
									
								// check if there is a mapping for the key
								if (configuration.getMapper().containsKey(key)) {
									key = configuration.getMapper().get(key);
								}
									
								String value = (String) entry.getValue();
								
								// decode the value
								value = URLDecoder.decode(value, "UTF-8");
								
								message.setProperty(key, value);
							}
						}
						
						messageProducer.produce(message);
					}
						
				});
			}
				
		});
			
		camelContext.start();

	}

	@Override
	public final void destroy() throws Exception {
		try {
			camelContext.stop();
		} catch (Exception e) {
			log.warn("Exception while stopping the connector: " + e.getMessage(), e);
		}
	}

	@Override
	public final JettyConfiguration getConfiguration() {
		return configuration;
	}

}
