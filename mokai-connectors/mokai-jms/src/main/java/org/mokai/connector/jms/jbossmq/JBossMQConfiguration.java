package org.mokai.connector.jms.jbossmq;

import java.util.HashMap;
import java.util.Map;

import org.mokai.ui.annotation.Label;

/**
 * 
 * @author German Escobar
 */
public class JBossMQConfiguration {
	
	private static final long DEFAULT_INITIAL_RECONNECT_DELAY = 5000;
	private static final long DEFAULT_RECONNECT_DELAY = 5000;

	@Label("Host")
	private String host = "localhost";
	
	@Label("Queue Name")
	private String queueName;
	
	@Label("Field Mapper")
	private Map<String,String> mapper = new HashMap<String,String>();
	
	@Label("Body Mapper")
	private String bodyMapper;
	
	@Label("Initial Reconnect Delay")
	private long initialReconnectDelay = DEFAULT_INITIAL_RECONNECT_DELAY;
	
	@Label("Reconnect Delay")
    private long reconnectDelay = DEFAULT_RECONNECT_DELAY;

	public final String getHost() {
		return host;
	}

	public final void setHost(String host) {
		this.host = host;
	}

	public final String getQueueName() {
		return queueName;
	}

	public final void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public final Map<String, String> getMapper() {
		return mapper;
	}

	public final void setMapper(Map<String, String> mapper) {
		this.mapper = mapper;
	}
	
	public final void addMapper(String key, String value) {
		mapper.put(key, value);
	}

	public final String getBodyMapper() {
		return bodyMapper;
	}

	public final void setBodyMapper(String bodyMapper) {
		this.bodyMapper = bodyMapper;
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
