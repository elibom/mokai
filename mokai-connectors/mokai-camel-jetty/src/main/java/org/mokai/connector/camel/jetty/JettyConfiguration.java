package org.mokai.connector.camel.jetty;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author German Escobar
 */
public class JettyConfiguration {

	private String port = "9080";
	
	private String context = "test";
	
	private Map<String,String> mapper = new HashMap<String,String>();
	
	public final String getPort() {
		return port;
	}

	public final void setPort(String port) {
		this.port = port;
	}

	public final String getContext() {
		return context;
	}

	public final void setContext(String context) {
		this.context = context;
	}

	public final Map<String, String> getMapper() {
		return mapper;
	}

	public final void setMapper(Map<String, String> mapper) {
		this.mapper = mapper;
	}
	
	public final void addMapper(String key, String value) {
		mapper.put(key, value);
	}
}
