package org.mokai.connector.mail;

import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

/**
 * Holds the information used to configure a {@link MailReceiver} instance.
 * 
 * @author German Escobar
 */
public class MailReceiverConfig {

	@Required
	@Label("Host")
	private String host = "localhost";
	
	@Required
	@Label("Use TLS")
	private boolean tls = false;
	
	/**
	 * Overrides the default port of the specified protocol.
	 */
	@Label("Port")
	private int port = -1;
	
	@Required
	@Label("Username")
	private String username;
	
	@Required
	@Label("Password")
	private String password;
	
	@Required
	@Label("Folder")
	private String folder = "Inbox";
	
	@Required
	@Label("ReconnectDelay")
	private int reconnectDelay = 5000;
	
	@Label("Process Unseen Messages")
	private boolean unseen = true;
	
	@Label("Delete Messages")
	private boolean delete = false;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isTls() {
		return tls;
	}

	public void setTls(boolean tls) {
		this.tls = tls;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public int getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(int reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}

	public boolean isUnseen() {
		return unseen;
	}

	public void setUnseen(boolean unseen) {
		this.unseen = unseen;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}
