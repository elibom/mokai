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
	@Label("Protocol")
	private String protocol = "imap";
	
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
	@Label("Interval")
	private int interval = 300;
	
	@Label("Process Unseen Messages")
	private boolean unseen = true;
	
	@Label("Delete Messages")
	private boolean delete = false;
	
	@Label("Email To SMS")
	private boolean emailToSms = false;
	
	@Label("SMS From")
	private String smsFrom = "12345";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
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

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
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

	public boolean isEmailToSms() {
		return emailToSms;
	}

	public void setEmailToSms(boolean emailToSms) {
		this.emailToSms = emailToSms;
	}

	public String getSmsFrom() {
		return smsFrom;
	}

	public void setSmsFrom(String smsFrom) {
		this.smsFrom = smsFrom;
	}

}
