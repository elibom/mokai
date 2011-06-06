package org.mokai.web.admin.vaadin.login;

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
		
		if ("admin".equals(username) && "admin".equals(password)) {
			fireViewEvent(new UserLoggedInEvent(getView(), username));
		} else {
			getView().clearForm();
			getView().showLoginFailed();
		}
	}

}
