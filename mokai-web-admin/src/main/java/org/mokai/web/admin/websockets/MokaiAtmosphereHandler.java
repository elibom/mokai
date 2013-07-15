package org.mokai.web.admin.websockets;

import java.io.IOException;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;

/**
 * Used to handle the <a href="https://github.com/Atmosphere/nettosphere">Nettosphere</a> requests.
 *
 * @author German Escobar
 */
public class MokaiAtmosphereHandler extends AbstractReflectorAtmosphereHandler {

	@Override
	public void onRequest(AtmosphereResource resource) throws IOException {

		AtmosphereRequest req = resource.getRequest();

        // first, tell Atmosphere to allow bi-directional communication by suspending.
        if (req.getMethod().equalsIgnoreCase("GET")) {
            // we are using HTTP long-polling with an invite timeout
            resource.suspend();
        }

        // retrieve the channel to which this request is subscribing
        String channel = resource.getRequest().getPathInfo();
        if (channel.startsWith("/")) {
        	channel = channel.substring(1);
        }

        // create the broadcaster and do the Atmosphere wiring
        Broadcaster b = BroadcasterFactory.getDefault().lookup(channel, true);
        b.addAtmosphereResource(resource);
        resource.setBroadcaster(b);

	}

	@Override
	public void destroy() {

	}

}
