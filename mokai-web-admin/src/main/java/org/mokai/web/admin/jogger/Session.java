package org.mokai.web.admin.jogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class Session {

    private final String id;

    private String user;

    private final Map<String, String> parameters;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private final long creationTime;

    private boolean keepLogged = false;

    public Session() {
        this.id = generateId();
        this.parameters = new HashMap<String, String>();
        this.creationTime = System.currentTimeMillis();
    }

    private String generateId() {
        return UUID.randomUUID().toString() + "-" + dateFormat.format(new Date());
    }

    public String getId() {
        return id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public long getCreationTime() {
        return creationTime;
    }

    public boolean isKeepLogged() {
        return keepLogged;
    }

    public void setKeepLogged(boolean keepLogged) {
        this.keepLogged = keepLogged;
    }
}
