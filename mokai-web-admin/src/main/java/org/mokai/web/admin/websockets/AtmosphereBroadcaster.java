package org.mokai.web.admin.websockets;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

/**
 * An implementation of {@link WebSocketsBroadcaster} that uses 
 * <a href="https://github.com/Atmosphere/atmosphere">Atmosphere</a> to broadcast messages.
 * 
 * @author German Escobar
 */
public class AtmosphereBroadcaster implements WebSocketsBroadcaster {

	@Override
	public void broadcast(String data) {
		BroadcasterFactory broadcasterFactory = BroadcasterFactory.getDefault();
		if (broadcasterFactory == null) {
			return;
		}
		
		Broadcaster b = broadcasterFactory.lookup("changes", true);
		if (b != null) {
			b.broadcast(data);
		}
	}

}
