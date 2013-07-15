package org.mokai.connector.ironmq;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.mokai.ui.annotation.Label;

/**
 *
 * @author German Escobar
 */
public class IronMqProcessorConfig {

	@Label("Project Id")
    private String projectId;

    @Label("Token")
    private String token;

    @Label("Queue Name")
    private String queueName;

	@Label("Field")
	private String field = "body";

	@ManagedAttribute(writable=false)
    public String getProjectId() {
        return projectId;
    }

    public void setProjectid(String projectId) {
        this.projectId = projectId;
    }

    @ManagedAttribute(writable=false)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @ManagedAttribute(writable=false)
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

	@ManagedAttribute
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
