package org.mokai.connector.jetty;

import java.util.HashMap;
import java.util.Map;

import org.mokai.ui.annotation.Label;

public class JettyConfiguration {

	@Label("Port")
	private int port = 9080;
	
	private boolean useBasicAuth = false;
	
	private Map<String,String> users = new HashMap<String,String>();
	
	@Label("Mapper")
	private Map<String,String> mapper = new HashMap<String,String>();

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isUseBasicAuth() {
		return useBasicAuth;
	}

	public void setUseBasicAuth(boolean useBasicAuth) {
		this.useBasicAuth = useBasicAuth;
	}

	public Map<String, String> getUsers() {
		return users;
	}

	public void setUsers(Map<String, String> users) {
		this.users = users;
	}
	
	public void addUser(String username, String password) {
		this.users.put(username, password);
	}

	public Map<String, String> getMapper() {
		return mapper;
	}

	public void setMapper(Map<String, String> mapper) {
		this.mapper = mapper;
	}
	
}
