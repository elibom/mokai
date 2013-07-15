package org.mokai.web.admin.websockets;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a <a href="https://github.com/Atmosphere/nettosphere">Nettosphere</a> server and provides methods to start/stop it.
 *
 * @author German Escobar
 */
public class NettosphereServer {

	private Logger log = LoggerFactory.getLogger(NettosphereServer.class);

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
		log.info("stopping nettosphere server ...");
		server.stop();
		log.info("nettosphere stopped");
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
