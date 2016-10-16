package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.http.Cookie;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.web.admin.jogger.Session;
import org.mokai.web.admin.jogger.SessionsManager;
import org.mokai.web.admin.jogger.UsersManager;
import org.mokai.web.admin.jogger.helpers.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sessions controller.
 *
 * @author German Escobar
 * @author Alejandro <lariverosc@gmail.com>
 */
public class Sessions {

    private final Logger log = LoggerFactory.getLogger(Sessions.class);

    private SessionsManager sessionsManager;

    private UsersManager usersManager;

    public void index(Request request, Response response) {
        if (WebUtil.isAuthenticated(response)) {
            response.redirect("/");
            return;
        }
        response.contentType("text/html; charset=UTF-8").render("login.ftl");
    }

    public void create(Request request, Response response) throws JSONException {
        Session session = WebUtil.getSession(response);
        if (session.getUser() != null) {
            return;
        }
        String data = request.getBody().asString();
        JSONObject jsonData = new JSONObject(data);
        String username = jsonData.getString("username");
        String password = jsonData.getString("password");
        boolean keepLogged = jsonData.getBoolean("keepLogged");
        if (usersManager.isValid(username, password)) {
            session.setUser(username);
            session.setKeepLogged(keepLogged);
            sessionsManager.addSession(session);
            response.redirect("/");
        } else {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("errorCode", "invalid_credentials");
            response.unauthorized().write(jsonResponse.toString());
        }
    }

    public void destroy(Request request, Response response) {
        Session session = (Session) response.getAttributes().get("session");
        sessionsManager.deleteSession(session);
        response.redirect("/login");
    }

    public void setSessionsManager(SessionsManager sessionsManager) {
        this.sessionsManager = sessionsManager;
    }

    public void setUsersManager(UsersManager usersManager) {
        this.usersManager = usersManager;
    }
}
