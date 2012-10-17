package org.mokai.web.admin.websockets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.ConnectorService;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.Monitorable.Status;
import org.mokai.RoutingEngine;
import org.mokai.Service;
import org.mokai.impl.camel.ConnectorServiceChangeListener;
import org.mokai.persist.MessageCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class checks the values of connector services and some queues periodically and if there are changes, it will 
 * broadcast them to the web sockets clients.</p>
 * 
 * <p>It uses an algorithm to improve the response time. It will start checking every 500 millis but if there are no 
 * changes, next time it will double the waiting time until it reaches a max of 4000 millis. It will keep checking
 * with this interval until there are actually changes, in which case it returns back to the 500 millis interval an so 
 * on.</p>
 * 
 * @author German Escobar
 */
public class WebSocketsChangeMonitor implements Service {
	
	private Logger log = LoggerFactory.getLogger(WebSocketsChangeMonitor.class);
	
	private RoutingEngine routingEngine;
	
	/**
	 * Used to wait between monitor executions so we can wake up the thread to stop it. 
	 */
	private Object lock = new Object();
	
	/**
	 * Holds the state of the monitor
	 */
	private State state;
	
	/**
	 * Used to notify connector services' changes
	 */
	private ConnectorServiceChangeListener connectorServiceChangeListener;
	
	/**
	 * Used to notify changes different from connector services.
	 */
	private WebSocketsBroadcaster broadcaster;

	@Override
	public void start() {
		
		if (State.STARTED.equals(state)) {
			return;
		}
		
		log.info("starting web sockets change monitor ... ");
		
		state = State.STARTED;
		new MonitorThread().start();
		
	}
	
	@Override
	public void stop() {
		
		if (State.STOPPED.equals(state)) {
			return;
		}
		
		log.info("stopping web sockets change monitor ... ");
		
		state = State.STOPPED;
		synchronized (lock) {
			lock.notify();
		}
	}
	
	@Override
	public State getState() {
		return state;
	}

	public void setRoutingEngine(RoutingEngine routingEngine) {
		this.routingEngine = routingEngine;
	}

	public void setConnectorServiceChangeListener(ConnectorServiceChangeListener connectorServiceChangeListener) {
		this.connectorServiceChangeListener = connectorServiceChangeListener;
	}

	public void setBroadcaster(WebSocketsBroadcaster broadcaster) {
		this.broadcaster = broadcaster;
	}

	/**
	 * 
	 * @author German Escobar
	 */
	private class MonitorThread extends Thread {
		
		private static final long DEFAULT_INTERVAL = 500;
		private static final long MAX_INTERVAL = 4000;
		
		/**
		 * Holds the last state of the applications.
		 */
		private Map<String,ConnectorMonitor> applications = new HashMap<String,ConnectorMonitor>();
		
		/**
		 * Holds the last state of the connections
		 */
		private Map<String,ConnectorMonitor> connections = new HashMap<String,ConnectorMonitor>();
		
		/**
		 * The number of messages queued in the applications router.
		 */
		private int queuedInApplications;
		
		/**
		 * The number of messages queued in the connections router.
		 */
		private int queuedInConnections;
		
		/**
		 * The number of messages that failed.
		 */
		private int failed;
		
		/**
		 * The interval between executions.
		 */
		private long interval = DEFAULT_INTERVAL;
		
		@Override
		public void run() {
			
			while ( Service.State.STARTED.equals(state) ) {
				monitor();
			}
			
		}
		
		/**
		 * Helper method that actually checks if there are changes and notifies them.
		 */
		private void monitor() {
			
			boolean changed = monitorConnectors(routingEngine.getApplications(), applications, 
					Direction.TO_APPLICATIONS);
			changed = monitorConnectors(routingEngine.getConnections(), connections, 
					Direction.TO_CONNECTIONS) ? true : changed;
			
			changed = monitorQueuedInApplications(routingEngine.getNumQueuedInApplicationsRouter()) ? 
					true : changed;
			changed = monitorQueuedInConnections(routingEngine.getNumQueuedInConnectionsRouter()) ? true : changed;
			
			MessageCriteria criteria = new MessageCriteria()
				.addStatus(Message.STATUS_FAILED)
				.addStatus(Message.STATUS_RETRYING);
			int actualFailed = routingEngine.getMessageStore().list(criteria).size();
			if (failed != actualFailed) {
				failed = actualFailed;
				try { 
					JSONObject json = new JSONObject().put("eventType", "FAILED_CHANGED")
							.put( "data", new JSONObject().put("value", failed) );
					broadcaster.broadcast(json.toString());
				} catch (JSONException e) {
					log.error("JSONException while notifying failed: " + e.getMessage(), e);
				}
				changed = true;
			}
			
			if (changed) {
				interval = DEFAULT_INTERVAL;
			} else {
				// double the amount of time but always keep it below MAX_INTERVAL
				interval = interval < MAX_INTERVAL ? interval * 2 : interval;
			}
			
			try {
				synchronized (lock) {
					lock.wait(interval);
				}
			} catch (InterruptedException e) {}
			
		}
		
		private boolean monitorQueuedInApplications(int actuallyQueuedInApplications) {
			
			if (queuedInApplications != actuallyQueuedInApplications) {
				queuedInApplications = actuallyQueuedInApplications;
				
				try {
					JSONObject json = new JSONObject().put("eventType", "TO_APPLICATIONS_CHANGED")
							.put( "data", new JSONObject().put("value", queuedInApplications) );
					broadcaster.broadcast(json.toString());
				} catch (JSONException e) {
					log.error("JSONException while notifying queued messages in applications: " + e.getMessage(), e);
				}
				
				return true;
			}
			
			return false;
		}
		
		private boolean monitorQueuedInConnections(int actuallyQueuedInConnections) {
			
			if (queuedInConnections != actuallyQueuedInConnections) {
				queuedInConnections = actuallyQueuedInConnections;
				
				try {
					JSONObject json = new JSONObject().put("eventType", "TO_CONNECTIONS_CHANGED")
							.put( "data", new JSONObject().put("value", queuedInConnections) );
					broadcaster.broadcast(json.toString());
				} catch (JSONException e) {
					log.error("JSONException while notifying queued messages in connections: " + e.getMessage(), e);
				}
				
				return true;
			}
			
			return false;
			
		}
		
		private boolean monitorConnectors(List<ConnectorService> connectorServices, 
				Map<String,ConnectorMonitor> monitors, Direction direction) {
			
			boolean changed = false;
			
			for (ConnectorService cs : connectorServices) {
				
				ConnectorMonitor monitor = monitors.get(cs.getId());
				boolean notify = false; // tells if this connector has changed and we need to notify
				
				if (monitor == null) {
					monitors.put( cs.getId(), new ConnectorMonitor(cs.getStatus(), cs.getNumQueuedMessages()) );
					notify = true;
				} else {
					if (monitor.status != cs.getStatus() || monitor.queuedMessages != cs.getNumQueuedMessages()) {
						monitor.status = cs.getStatus();
						monitor.queuedMessages = cs.getNumQueuedMessages();
						notify = true;
					}
				}
				
				if (notify) {
					connectorServiceChangeListener.changed(cs, direction);
					changed = true;
				}
			}
			
			return changed;
			
		}
		
		/**
		 * 
		 * @author German Escobar
		 */
		private class ConnectorMonitor {
			
			public Status status;
			public int queuedMessages;
			
			public ConnectorMonitor(Status status, int queuedMessages) {
				this.status = status;
				this.queuedMessages = queuedMessages;
			}
		}
		
	}
	
}