package org.mokai.connector.ironmq;

import net.gescobar.jmx.annotation.ManagedAttribute;
import org.mokai.ui.annotation.Label;

/**
 *
 * @author Alejandro Riveros Cruz <lariverosc@gmail.com>
 */
public class IronMqConfiguration {

    @Label("IronMqProjectId")
    private String ironMqProjectId;

    @Label("IronMqToken")
    private String ironMqToken;

    @Label("IronMqQueueName")
    private String ironMqQueueName;

    @Label("numThreads")
    private int numThreads = 5;

    @Label("fetchInterval")
    private long fetchInterval = 1000;

    @ManagedAttribute(writable = false)
    public String getIronMqProjectId() {
        return ironMqProjectId;
    }

    public void setIronMqProjectId(String ironMqProjectId) {
        this.ironMqProjectId = ironMqProjectId;
    }

    @ManagedAttribute(writable = false)
    public String getIronMqToken() {
        return ironMqToken;
    }

    public void setIronMqToken(String ironMqToken) {
        this.ironMqToken = ironMqToken;
    }

    @ManagedAttribute(writable = false)
    public String getIronMqQueueName() {
        return ironMqQueueName;
    }

    public void setIronMqQueueName(String ironMqQueueName) {
        this.ironMqQueueName = ironMqQueueName;
    }

    @ManagedAttribute(writable = false)
    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    @ManagedAttribute(writable = false)
    public long getFetchInterval() {
        return fetchInterval;
    }

    public void setFetchInterval(long fetchInterval) {
        this.fetchInterval = fetchInterval;
    }
}
