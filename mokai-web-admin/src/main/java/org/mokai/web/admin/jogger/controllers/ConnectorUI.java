package org.mokai.web.admin.jogger.controllers;

import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.ConnectorService;
import org.mokai.Processor;

/**
 * DTO used to pass connectors to the view.
 * 
 * @author German Escobar
 */
public class ConnectorUI {

	private String id;
	
	private String type;
	
	private String state;
	
	private String status;
	
	private int queuedMessages;
	
	private boolean processor;
	
	private int priority;
	
	public ConnectorUI() {
	}
	
	public ConnectorUI(ConnectorService connectorService) {
		this.id = connectorService.getId();
		this.type = Helper.getComponentName(connectorService.getConnector());
		this.state = connectorService.getState().name();
		this.status = connectorService.getStatus().name();
		this.queuedMessages = connectorService.getNumQueuedMessages();
		this.processor = Processor.class.isInstance(connectorService.getConnector());
		this.priority = connectorService.getPriority();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public ConnectorUI withId(String id) {
		setId(id);
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public ConnectorUI withType(String type) {
		setType(type);
		return this;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public ConnectorUI withState(String state) {
		setState(state);
		return this;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public ConnectorUI withStatus(String status) {
		setStatus(status);
		return this;
	}

	public int getQueuedMessages() {
		return queuedMessages;
	}

	public void setQueuedMessages(int queuedMessages) {
		this.queuedMessages = queuedMessages;
	}
	
	public ConnectorUI withQueuedMessages(int queuedMessages) {
		setQueuedMessages(queuedMessages);
		return this;
	}

	public boolean isProcessor() {
		return processor;
	}

	public void setProcessor(boolean processor) {
		this.processor = processor;
	}
	
	public ConnectorUI withProcessor(boolean processor) {
		setProcessor(processor);
		return this;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public JSONObject toJSON() throws JSONException {
		return new JSONObject()
			.put("id", id)
			.put("type", type)
			.put("state", state)
			.put("status", status)
			.put("queuedMessages", queuedMessages)
			.put("isProcessor", processor)
			.put("priority", priority);
	}
	
}
