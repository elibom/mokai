package org.mokai.connector.smpp;

import java.util.HashMap;
import java.util.Map;

public class SmppServerConfiguration {

	private int port = 4444;

	private Map<String,String> users = new HashMap<String,String>();

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Map<String, String> getUsers() {
		return users;
	}

	public void setUsers(Map<String, String> users) {
		this.users = users;
	}

	public SmppServerConfiguration addUser(String systemId, String password) {
		users.put(systemId, password);

		return this;
	}
}
