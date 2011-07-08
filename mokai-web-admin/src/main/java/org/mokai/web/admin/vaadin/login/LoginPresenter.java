package org.mokai.web.admin.vaadin.login;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.mokai.web.admin.AdminPasswordStore;
import org.mokai.web.admin.vaadin.WebAdminContext;

import com.github.peholmst.mvp4vaadin.Presenter;

public class LoginPresenter extends Presenter<LoginView> {

	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 747213053753352595L;

	public LoginPresenter(LoginView view) {
		super(view);
	}
	
	public void attemptLogin(String username, String password) {
		
		
		
		if ("admin".equals(username) && isPasswordValid(password)) {
			fireViewEvent(new UserLoggedInEvent(getView(), username));
		} else {
			getView().clearForm();
			getView().showLoginFailed();
		}
	}
	
	private boolean isPasswordValid(String password) {
		AdminPasswordStore passwordStore = WebAdminContext.getInstance().getAdminPasswordStore();
		String encryptedPassword = passwordStore.getPassword();
		
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

}
