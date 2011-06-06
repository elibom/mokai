package org.mokai.web.admin.vaadin;

import org.mokai.RoutingEngine;
import org.mokai.web.admin.vaadin.login.LoginViewImpl;
import org.mokai.web.admin.vaadin.login.UserLoggedInEvent;
import org.mokai.web.admin.vaadin.main.MainViewImpl;
import org.mokai.web.admin.vaadin.main.UserLoggedOutEvent;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.peholmst.mvp4vaadin.ViewEvent;
import com.github.peholmst.mvp4vaadin.ViewListener;
import com.vaadin.Application;
import com.vaadin.ui.Window;

public class WebAdminApplication extends Application implements ViewListener {

	private static final long serialVersionUID = 1L;
	
	private LoginViewImpl loginView;
	
	private MainViewImpl mainView;
	
	@Autowired
	private RoutingEngine routingEngine;

	@Override
	public final void init() {
		setTheme("mokai");
		initContext();
		createAndShowLoginWindow();
	}
	
	private void initContext() {
		WebAdminContext context = WebAdminContext.getInstance();
		context.setRoutingEngine(routingEngine);
	}
	
	private void createAndShowLoginWindow() {
		loginView = new LoginViewImpl();
		loginView.addListener(this);
		
		Window loginWindow = new Window(loginView.getDisplayName(), loginView.getViewComponent());
		setMainWindow(loginWindow);
	}
	
	private void createAndShowMainWindow() {
		
		loginView.removeListener(this);
		
		mainView = new MainViewImpl();
		mainView.addListener(this);
		
		Window mainWindow = new Window(mainView.getDisplayName(), mainView.getViewComponent());
		removeWindow(getMainWindow());
		setMainWindow(mainWindow);
	}

	@Override
	public void handleViewEvent(ViewEvent event) {
		
		if (event instanceof UserLoggedInEvent) {
			String username = ((UserLoggedInEvent) event).getUsername();
			setUser(username);
			createAndShowMainWindow();
		} else if (event instanceof UserLoggedOutEvent) {
			close();
		}
		
	}
	

}
