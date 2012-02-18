package org.mokai.connector.mail;

import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

/**
 * Holds the information used to configure a {@link MailProcessor} instance.
 * 
 * @author German Escobar
 */
public class MailProcessorConfig {
	
	@Required
	@Label("Host")
	private String host = "localhost";
	
	@Required
	@Label("Port")
	private int port = 25;
	
	@Label("Needs Authentication")
	private boolean auth = true;
	
	@Label("Username")
	private String username;
	
	@Label("Password")
	private String password;
	
	@Label("Use TLS")
	private boolean tls = false;
	
	@Label("From")
	private String from = "mokai@localhost.com";
	
	@Label("Subject")
	private String subject = "Mokai Message";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isTls() {
		return tls;
	}

	public void setTls(boolean tls) {
		this.tls = tls;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
