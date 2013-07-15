package org.mokai.impl.camel;

import org.mokai.ConnectorService;
import org.mokai.Message.Direction;

/**
 *
 *
 * @author German Escobar
 */
public interface ConnectorServiceChangeListener {

	/**
	 * Called when a connector service changes (ie. started/stoped, changed status and increased/decreased queued
	 * messages).
	 *
	 * @param connectorService the {@link ConnectorService} that changed.
	 * @param direction tells if the connector service is an application ({@link Direction#TO_APPLICATIONS}) or a connection.
	 */
	void changed(ConnectorService connectorService, Direction direction);

}
