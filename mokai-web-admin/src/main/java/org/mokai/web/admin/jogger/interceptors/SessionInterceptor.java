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
        Cookie cookie = request.getCookie(SESSION_ID_COOKIE_KEY);
        Session sessionFromCookie = getSessionFromCookie(cookie);
        if (sessionFromCookie != null) {
            return sessionFromCookie;
        }
        return new Session();
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

}
