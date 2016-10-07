package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.web.admin.AdminPasswordStore;
import org.mokai.web.admin.jogger.annotations.Secured;

@Secured
public class Administration {

	private AdminPasswordStore adminPasswordStore;

	public void changePassword(Request request, Response response) throws JSONException {
		JSONObject jsonPassword = new JSONObject( request.getBody().asString() );

		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		String newPassword = passwordEncryptor.encryptPassword( jsonPassword.getString("password") );

		adminPasswordStore.setPassword( newPassword );
	}

	public void setAdminPasswordStore(AdminPasswordStore adminPasswordStore) {
		this.adminPasswordStore = adminPasswordStore;
	}

}
