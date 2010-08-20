package org.mokai.connector.camel.jetty;

public class JettyConfiguration {

	private String port = "9080";
	
	private String context = "test";
	
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}
