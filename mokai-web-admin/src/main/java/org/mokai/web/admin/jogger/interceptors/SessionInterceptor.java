package org.mokai.web.admin.jogger.interceptors;

import com.elibom.jogger.http.Cookie;
import com.elibom.jogger.http.Http;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import com.elibom.jogger.middleware.router.interceptor.Interceptor;
import com.elibom.jogger.middleware.router.interceptor.InterceptorExecution;
import javax.xml.bind.DatatypeConverter;
import org.mokai.web.admin.jogger.Session;
import org.mokai.web.admin.jogger.SessionsManager;
import org.mokai.web.admin.jogger.UsersManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class SessionInterceptor implements Interceptor {

    private final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

    public final static String SESSION_ID_COOKIE_KEY = "mokai-session";

    private SessionsManager sessionsManager;

    private UsersManager usersManager;

    @Override
    public void intercept(Request request, Response response, InterceptorExecution execution) throws Exception {
        Session session = getSession(request);

        if (request.getHeader(Http.Headers.ACCEPT).contains("html")) {
            addCookies(response, session);
        }
        response.setAttribute("session", session);
        if (session.getUser() != null && !session.getUser().isEmpty()) {
            response.setAttribute("user", session.getUser());
        }

        execution.proceed();
    }

    private Session getSession(Request request) {
        if (isBasicAuthentication(request)) {
            String basicAuthHeader = getBasicAuthHeader(request);
            return getSessionFromAuthHeader(basicAuthHeader);
        } else {
            Cookie cookie = request.getCookie(SESSION_ID_COOKIE_KEY);
            Session sessionFromCookie = getSessionFromCookie(cookie);
            if (sessionFromCookie != null) {
                return sessionFromCookie;
            }
            return new Session();
        }
    }

    private Session getSessionFromAuthHeader(String basicAuthHeader) {
        String[] userPass = getUserPass(basicAuthHeader);
        String username = userPass[0];
        String password = userPass[1];
        if (usersManager.isValid(username, password)) {
            Session session = new Session();
            session.setUser(username);
            return session;
        }
        return new Session();
    }

    private String[] getUserPass(String basicAuthHeader) {
        String[] basicAuth = basicAuthHeader.split(" ");
        if (basicAuth.length != 2) {
            log.debug("authentication header '" + basicAuthHeader + "' is invalid");
            return null;
        }

        String strUserPassword = basicAuth[1];
        strUserPassword = new String(DatatypeConverter.parseBase64Binary(strUserPassword));
        String[] userPassword = strUserPassword.split(":");
        if (userPassword.length != 2) {
            log.debug("authentication header '" + basicAuthHeader + "' is invalid");
            return null;
        }
        return userPassword;
    }

    private boolean isBasicAuthentication(Request request) {
        return getBasicAuthHeader(request) != null;
    }

    private String getBasicAuthHeader(Request request) {
        if (request.getHeader("Authorization") != null) {
            return request.getHeader("Authorization");
        }
        return request.getHeader("Authentication");
    }

    private Session getSessionFromCookie(Cookie cookie) {
        if (cookie != null) {
            String sessionId = cookie.getValue();
            return sessionsManager.getSession(sessionId);
        }
        return null;
    }

    private void addCookies(Response response, Session session) {
        Cookie sessionCookie = new Cookie(SESSION_ID_COOKIE_KEY, session.getId(), Integer.MAX_VALUE, "/");
        sessionCookie.setHttpOnly(true);
        response.setCookie(sessionCookie);
    }

    public void setSessionsManager(SessionsManager sessionsManager) {
        this.sessionsManager = sessionsManager;
    }

    public void setUsersManager(UsersManager usersManager) {
        this.usersManager = usersManager;
    }

}
