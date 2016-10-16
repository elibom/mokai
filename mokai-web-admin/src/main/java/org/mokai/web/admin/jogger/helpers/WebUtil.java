package org.mokai.web.admin.jogger.helpers;

import com.elibom.jogger.http.Response;
import org.mokai.web.admin.jogger.Session;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class WebUtil {

    public static boolean isAuthenticated(Response response) {
        Session session = getSession(response);
        if (session != null && session.getUser() != null && !session.getUser().isEmpty()) {
            return true;
        }
        return false;
    }

    public static Session getSession(Response response) {
        Object session = response.getAttributes().get("session");
        if (session != null) {
            return (Session) session;
        }
        return null;
    }

}
