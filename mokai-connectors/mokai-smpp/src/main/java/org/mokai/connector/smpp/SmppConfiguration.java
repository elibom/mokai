package org.mokai.connector.smpp;

import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

public class SmppConfiguration {
	
	public enum BindType {
		
		TRANSMITTER, TRANSCIEVER, RECEIVER;
		
		public static BindType convert(String strValue) {
			if (strValue == null) {
				throw new IllegalArgumentException("value not provided");
			}
			
			if (strValue.equals("t")) {
				return TRANSMITTER;
			} else if (strValue.equals("r")) {
				return RECEIVER;
			} else if (strValue.equals("tr")) {
				return TRANSCIEVER;
			}
			
			throw new IllegalArgumentException("value not supported");
		}
	}

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
	private BindType bindType = BindType.TRANSCIEVER;
	
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
	
	public final String getHost() {
		return host;
	}

	public final void setHost(String host) {
		this.host = host;
	}

	public final int getPort() {
		return port;
	}

	public final void setPort(int port) {
		this.port = port;
	}

	public final String getSystemId() {
		return systemId;
	}

	public final void setSystemId(String user) {
		this.systemId = user;
	}

	public final String getPassword() {
		return password;
	}

	public final void setPassword(String password) {
		this.password = password;
	}

	public final String getSystemType() {
		return systemType;
	}

	public final void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public final int getEnquireLinkTimer() {
		return enquireLinkTimer;
	}

	public final void setEnquireLinkTimer(int enquireLinkTimer) {
		this.enquireLinkTimer = enquireLinkTimer;
	}

	public final BindType getBindType() {
		return bindType;
	}

	public final void setBindType(BindType bindType) {
		this.bindType = bindType;
	}

	public final String getSourceNPI() {
		return sourceNPI;
	}

	public final void setSourceNPI(String sourceNPI) {
		this.sourceNPI = sourceNPI;
	}

	public final String getSourceTON() {
		return sourceTON;
	}

	public final void setSourceTON(String sourceTON) {
		this.sourceTON = sourceTON;
	}

	public final String getDestNPI() {
		return destNPI;
	}

	public final void setDestNPI(String destNPI) {
		this.destNPI = destNPI;
	}

	public final String getDestTON() {
		return destTON;
	}

	public final void setDestTON(String destTON) {
		this.destTON = destTON;
	}

	public final long getInitialReconnectDelay() {
		return initialReconnectDelay;
	}

	public final void setInitialReconnectDelay(long initialReconnectDelay) {
		this.initialReconnectDelay = initialReconnectDelay;
	}

	public final long getReconnectDelay() {
		return reconnectDelay;
	}

	public final void setReconnectDelay(long reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}
	
}
