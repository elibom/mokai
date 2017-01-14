package org.mokai.web.admin.jogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class UsersManager {

    private final Map<String, String> userPassMap;

    public UsersManager() {
        this.userPassMap = initUsers();
    }

    private Map<String, String> initUsers() {
        Map<String, String> temp = new HashMap<String, String>();
        temp.put("admin", "kcwAE%745");
        return Collections.unmodifiableMap(temp);
    }

    public boolean isValid(String username, String password) {
        if (userPassMap.containsKey(username) && userPassMap.get(username).equals(password)) {
            return true;
        }
        return false;
    }
}
