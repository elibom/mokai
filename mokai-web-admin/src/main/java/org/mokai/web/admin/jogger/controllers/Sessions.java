package org.mokai.web.admin.jogger.controllers;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jogger.http.Request;
import org.jogger.http.Response;
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
		response.render("login.ftl");
	}
	
	public void create(Request request, Response response) throws JSONException {
	
		JSONObject input = null;
		try {
			input = new JSONObject(request.getBody().asString());
		} catch (JSONException e) {
			response.badRequest().print("{ \"message\": \"" + e.getMessage() + "\"}");
			return;
		}
		
		String username = getField(input, "username");
		String password = getField(input, "password");
		
		if ( !"admin".equals(username) || !isPasswordValid(password) ) {
			response.unauthorized();
		}
		
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

	public void setAdminPasswordStore(AdminPasswordStore adminPasswordStore) {
		this.adminPasswordStore = adminPasswordStore;
	}

}
