package org.mokai.web.admin.websockets;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;

/**
 * Creates a <a href="https://github.com/Atmosphere/nettosphere">Nettosphere</a> server and provides methods to start/stop it.
 * 
 * @author German Escobar
 */
public class NettosphereServer {
	
	private Nettosphere server;
	
	private String host = "0.0.0.0";
	
	private int port = 8585;

	private AtmosphereHandler atmosphereHandler;
	
	/**
	 * Initializes the server and starts it.
	 */
	public void start() {
		
		server = new Nettosphere.Builder().config(
			new Config.Builder()
				.host(host)
				.resource("/*", atmosphereHandler)
				.port(port)
                .build()
            ).build();
		
		server.start();
		
	}
	
	/**
	 * Stops the server.
	 */
	public void stop() {
		server.stop();
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAtmosphereHandler(AtmosphereHandler atmosphereHandler) {
		this.atmosphereHandler = atmosphereHandler;
	}
	
}
