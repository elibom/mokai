package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.http.Cookie;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.web.admin.AdminPasswordStore;

/**
 * Sessions controller.
 *
 * @author German Escobar
 */
public class Sessions {

	private AdminPasswordStore adminPasswordStore;

	public void newForm(Request request, Response response) {
		boolean authenticated = (Boolean) response.getAttributes().get("authenticated");
		if (authenticated) {
			response.redirect("/");
			return;
		}

		response.render("login.ftl");
	}

	public void create(Request request, Response response) throws JSONException {
		JSONObject input = null;
		try {
			input = new JSONObject(request.getBody().asString());
		} catch (JSONException e) {
			response.badRequest().write("{ \"message\": \"" + e.getMessage() + "\"}");
			return;
		}

		String username = getField(input, "username");
		String password = getField(input, "password");

		if ( !"admin".equals(username) || !isPasswordValid(password) ) {
			response.unauthorized();
		}

		Cookie cookie = new Cookie("access_token", "SO8ERHEHFSKJFHI7S3G3WODY7WFG64");
		cookie.setMaxAge( 3600 * 24 * 14 );
		cookie.setHttpOnly(true);
		response.setCookie( cookie );
	}

	private boolean isPasswordValid(String password) {
		String encryptedPassword = adminPasswordStore.getPassword();

		if (encryptedPassword != null) {
			StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
			return encryptor.checkPassword(password, encryptedPassword);
		} else {
			if ("admin".equals(password)) {
				return true;
			}
		}

		return false;
	}

	private String getField(JSONObject input, String field) throws JSONException {
		if ( !input.has(field) ) {
			return null;
		}

		return input.getString(field);
	}

	public void delete(Request request, Response response) {
		Cookie cookie = request.getCookie("access_token");

		if (cookie != null) {
			response.removeCookie(cookie);
		}
	}

	public void setAdminPasswordStore(AdminPasswordStore adminPasswordStore) {
		this.adminPasswordStore = adminPasswordStore;
	}

}
