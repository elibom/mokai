package org.mokai.connector.smpp;

import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

public class SmppConfiguration {

	@Required
	@Label("Host")
	private String host;
	
	@Required
	@Label("Port")
	private int port;
	
	@Required
	@Label("System ID")
	private String systemId;
	
	@Required
	@Label("Password")
	private String password;
	
	@Required
	@Label("System Type")
	private String systemType;
	
	@Label("Enquire Link Timer")
	private int enquireLinkTimer = 9000;
	
	@Label("Bind Type")
	private String bindType= "tr";
	
	@Label("Source NPI")
	private String sourceNPI;
	
	@Label("Source TON")
	private String sourceTON;
	
	@Label("Destination NPI")
	private String destNPI;
	
	@Label("Destination TON")
	private String destTON;
	
	@Label("Initial Reconnect Delay")
	private long initialReconnectDelay = 5000;
	
	@Label("Reconnect Delay")
    private long reconnectDelay = 5000;
	
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

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String user) {
		this.systemId = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public int getEnquireLinkTimer() {
		return enquireLinkTimer;
	}

	public void setEnquireLinkTimer(int enquireLinkTimer) {
		this.enquireLinkTimer = enquireLinkTimer;
	}

	public String getBindType() {
		return bindType;
	}

	public void setBindType(String bindType) {
		this.bindType = bindType;
	}

	public String getSourceNPI() {
		return sourceNPI;
	}

	public void setSourceNPI(String sourceNPI) {
		this.sourceNPI = sourceNPI;
	}

	public String getSourceTON() {
		return sourceTON;
	}

	public void setSourceTON(String sourceTON) {
		this.sourceTON = sourceTON;
	}

	public String getDestNPI() {
		return destNPI;
	}

	public void setDestNPI(String destNPI) {
		this.destNPI = destNPI;
	}

	public String getDestTON() {
		return destTON;
	}

	public void setDestTON(String destTON) {
		this.destTON = destTON;
	}

	public long getInitialReconnectDelay() {
		return initialReconnectDelay;
	}

	public void setInitialReconnectDelay(long initialReconnectDelay) {
		this.initialReconnectDelay = initialReconnectDelay;
	}

	public long getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(long reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}
	
}
