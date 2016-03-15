package org.mokai.connector.rabbitmq;

import net.gescobar.jmx.annotation.ManagedAttribute;
import org.mokai.ui.annotation.Label;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
public class RabbitMqConfiguration {

	@Label("host")
	private String host;

	@Label("port")
	private int port = 5672;

	@Label("virtualHost")
	private String virtualHost;

	@Label("username")
	private String username;

	@Label("password")
	private String password;

	@Label("exchange")
	private String exchange;

	@Label("queueName")
	private String queueName;

	@Label("routingKey")
	private String routingKey;

	@Label("fetchInterval")
	private long reconnectDelay = 1000;

	@Label("heartBeat")
	private int heartBeat = 10;

	@ManagedAttribute(writable = false)
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@ManagedAttribute(writable = false)
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@ManagedAttribute(writable = false)
	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}

	@ManagedAttribute(writable = false)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@ManagedAttribute(writable = false)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@ManagedAttribute(writable = false)
	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	@ManagedAttribute(writable = false)
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	@ManagedAttribute(writable = false)
	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	@ManagedAttribute(writable = false)
	public int getHeartBeat() {
		return heartBeat;
	}

	public void setHeartBeat(int heartBeat) {
		this.heartBeat = heartBeat;
	}

	@ManagedAttribute(writable = false)
	public long getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(long reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}

	@Override
	public String toString() {
		return "RabbitMqConfiguration{" + "host=" + host + ", port=" + port + ", virtualHost=" + virtualHost + ", username=" + username + ", password=" + password + ", exchange=" + exchange + ", queueName=" + queueName + ", routingKey=" + routingKey + ", heartBeat=" + heartBeat + '}';
	}
}
