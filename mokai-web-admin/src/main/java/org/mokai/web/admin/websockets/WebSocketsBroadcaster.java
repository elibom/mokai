package org.mokai.web.admin.websockets;

/**
 * Provides an abstraction for broadcasting web sockets data.
 *
 * @author German Escobar
 */
public interface WebSocketsBroadcaster {

	/**
	 * Broadcast the data to the connected clients.
	 *
	 * @param data the string to be broadcasted
	 */
	void broadcast(String data);

}
