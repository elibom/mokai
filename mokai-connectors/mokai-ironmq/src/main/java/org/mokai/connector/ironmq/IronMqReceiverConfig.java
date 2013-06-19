package org.mokai.connector.ironmq;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.mokai.ui.annotation.Label;

/**
 * 
 * @author German Escobar
 */
public class IronMqReceiverConfig {
	
	@Label("Project Id")
    private String projectId;

    @Label("Token")
    private String token;

    @Label("Queue Name")
    private String queueName;

	@Label("Num Consumer Threads")
    private int numConsumerThreads = 5;

    @Label("Fetch Interval")
    private long fetchInterval = 1000;
    
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
    
    @ManagedAttribute(writable=false)
    public int getNumConsumerThreads() {
        return numConsumerThreads;
    }

    public void setNumConsumerThreads(int numConsumerThreads) {
        this.numConsumerThreads = numConsumerThreads;
    }

    @ManagedAttribute(writable=false)
    public long getFetchInterval() {
        return fetchInterval;
    }

    public void setFetchInterval(long fetchInterval) {
        this.fetchInterval = fetchInterval;
    }
}
