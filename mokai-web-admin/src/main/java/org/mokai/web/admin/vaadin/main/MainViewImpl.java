package org.mokai.web.admin.vaadin.main;

import org.mokai.web.admin.vaadin.dashboard.DashboardViewImpl;
import org.mokai.web.admin.vaadin.msgs.MessagesViewImpl;
import org.mokai.web.admin.vaadin.pwd.PasswordChangedEvent;
import org.mokai.web.admin.vaadin.pwd.PasswordViewImpl;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.github.peholmst.mvp4vaadin.ViewEvent;
import com.github.peholmst.mvp4vaadin.ViewListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class MainViewImpl extends AbstractView<MainView, MainPresenter> implements MainView, VaadinView, ViewListener {
	
	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;

	private VerticalLayout viewLayout;
	
	private WindowHeader windowHeader;
	
	private WindowMenu windowMenu;
	
	/**
	 * The component that we are showing in the body of the page.
	 */
	private ComponentContainer activeComponent;
	
	public MainViewImpl() {
		super(true);
	}

	@Override
	public String getDescription() {
		return "Web Admin Console";
	}

	@Override
	public String getDisplayName() {
		return "Mokai";
	}

	@Override
	protected MainPresenter createPresenter() {
		return new MainPresenter(this);
	}

	@Override
	protected void initView() {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		
		// header and menu wrapper
		VerticalLayout header = new VerticalLayout();
		header.addStyleName(Reindeer.LAYOUT_BLACK);
		
		windowHeader = new WindowHeader(getPresenter());
		header.addComponent(windowHeader);
		
		windowMenu = new WindowMenu(getPresenter());
		header.addComponent(windowMenu);
		
		viewLayout.addComponent(header);
		
		createAndShowDashboard();
		
	}

	@Override
	public ComponentContainer getViewComponent() {
		return viewLayout;
	}

	@Override
	public void createAndShowPasswordWindow() {
		Window passwordWindow = new Window("Change Password");
		passwordWindow.setModal(true);
		passwordWindow.setWidth("300px");
		passwordWindow.setHeight("200px");
		
		PasswordViewImpl view = new PasswordViewImpl();
		view.addListener(this);
		
		passwordWindow.setContent(view.getViewComponent());
		
		viewLayout.getWindow().addWindow(passwordWindow);
	}
	
	@Override
	public void createAndShowDashboard() {
		DashboardViewImpl dashboardView = new DashboardViewImpl();
		ComponentContainer dashboardComponent = dashboardView.getViewComponent();
		
		if (activeComponent != null) {
			viewLayout.replaceComponent(activeComponent, dashboardComponent);
		} else {
			viewLayout.addComponent(dashboardComponent);
		}
		
		viewLayout.setExpandRatio(dashboardComponent, 1.0F);
		
		activeComponent = dashboardComponent;
	}
	
	@Override
	public void createAndShowMessages() {
		MessagesViewImpl messagesView = new MessagesViewImpl();
		ComponentContainer messagesComponent = messagesView.getViewComponent();
		
		if (activeComponent != null) {
			viewLayout.replaceComponent(activeComponent, messagesComponent);
		} else {
			viewLayout.addComponent(messagesComponent);
		}
		
		viewLayout.setExpandRatio(messagesComponent, 1.0F);
		
		activeComponent = messagesComponent;
	}

	@Override
	public void handleViewEvent(ViewEvent event) {
		if (PasswordChangedEvent.class.isInstance(event)) {
			PasswordChangedEvent passwordChangedEvent = (PasswordChangedEvent) event;
			viewLayout.getWindow().removeWindow(passwordChangedEvent.getPasswordWindow());
			
			viewLayout.getWindow().showNotification("Password changed", Notification.TYPE_TRAY_NOTIFICATION);
		}
	}

}
