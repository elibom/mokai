package org.mokai.web.admin.vaadin.pwd;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.mokai.web.admin.AdminPasswordStore;
import org.mokai.web.admin.vaadin.WebAdminContext;

import com.github.peholmst.mvp4vaadin.Presenter;
import com.vaadin.ui.Window;

/**
 * 
 * @author German Escobar
 */
public class PasswordPresenter extends Presenter<PasswordView> {
	
	private static final long serialVersionUID = 1L;

	public PasswordPresenter(PasswordView view) {
		super(view);
	}

	public void changePassword(Window window, String newPassword, String confirmPassword) {
		if (!newPassword.equals(confirmPassword)) {
			getView().passwordsDontMatch();
			return;
		}
		
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		AdminPasswordStore passwordStore = WebAdminContext.getInstance().getAdminPasswordStore();
		
		boolean saved = passwordStore.setPassword(passwordEncryptor.encryptPassword(newPassword));
		if (!saved) {
			getView().passwordNotChanged();
		}
		
		
		fireViewEvent(new PasswordChangedEvent(getView(), window));
	}
}
