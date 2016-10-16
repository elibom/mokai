package org.mokai.web.admin.jogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class SessionsManager {

    private final Map<String, Session> sessions;

    public SessionsManager() {
        sessions = new HashMap<String, Session>();
        new Thread(new SessionMonitor()).start();
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void addSession(Session session) {
        sessions.put(session.getId(), session);
    }

    public void deleteSession(Session session) {
        sessions.remove(session.getId());
    }

    private class SessionMonitor implements Runnable {

        private final long SESSION_TIME_OUT_MILLIS = 60 * 1000;

        @Override
        public void run() {
            while (true) {
                Set<Session> sessionsToDelete = new HashSet<Session>();
                for (Session session : sessions.values()) {
                    if (!session.isKeepLogged()) {
                        if (System.currentTimeMillis() - session.getCreationTime() > SESSION_TIME_OUT_MILLIS) {
                            sessionsToDelete.add(session);
                        }
                    }
                }
                for (Session session : sessionsToDelete) {
                    sessions.remove(session.getId());
                }
                try {
                    Thread.sleep(SESSION_TIME_OUT_MILLIS);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
