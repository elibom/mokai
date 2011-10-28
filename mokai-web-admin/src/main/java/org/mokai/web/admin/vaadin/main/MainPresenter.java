package org.mokai.web.admin.vaadin.main;

import com.github.peholmst.mvp4vaadin.Presenter;

public class MainPresenter extends Presenter<MainView> {

	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 6220300151070145243L;

	public MainPresenter(MainView view) {
		super(view);
	}
	
	public void createAndShowPasswordWindow() {
		getView().createAndShowPasswordWindow();
	}
	
	public void createAndShowDashboard() {
		getView().createAndShowDashboard();
	}
	
	public void createAndShowMessages() {
		getView().createAndShowMessages();
	}
	
	public void logout() {
		fireViewEvent(new UserLoggedOutEvent(getView()));
	}

}
