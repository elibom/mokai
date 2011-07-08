package org.mokai.web.admin.vaadin.login;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class LoginViewImpl extends AbstractView<LoginView, LoginPresenter> implements VaadinView, LoginView {
	
	/**
	 * Generated Serial Version UID 
	 */
	private static final long serialVersionUID = -5123691925590033327L;
	
	private HorizontalLayout viewLayout;
	private TextField username;
	private PasswordField password;
	private Button loginButton;
	
	public LoginViewImpl() {
		super(true);
	}
	
	@Override
	public String getDisplayName() {
		return "Mokai";
	}
	
	@Override
	public String getDescription() {
		return "Web Admin Console";
	}

	@Override
	protected LoginPresenter createPresenter() {
		return new LoginPresenter(this);
	}
	
	@Override
	public ComponentContainer getViewComponent() {
		return viewLayout;
	}

	@Override
	protected void initView() {
		VerticalLayout loginPanel = new VerticalLayout();
		loginPanel.setSpacing(true);
		loginPanel.setStyleName(Reindeer.LAYOUT_WHITE);
		loginPanel.setWidth("300px");
		
		VerticalLayout header = getHeader();
		loginPanel.addComponent(header);
		
		VerticalLayout body = getBody();
		loginPanel.addComponent(body);
		
		viewLayout = new HorizontalLayout();
		viewLayout.addComponent(loginPanel);
		viewLayout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
		
		viewLayout.setSizeFull();
		
		username.focus();
	}
	
	public VerticalLayout getHeader() {
		VerticalLayout header = new VerticalLayout();
		header.addStyleName(Reindeer.LAYOUT_BLACK);
		header.addStyleName("header");
		
		Label headerLabel = new Label("Login");
		headerLabel.addStyleName(Reindeer.LABEL_H2);
		header.addComponent(headerLabel);
		
		return header;
	}
	
	public VerticalLayout getBody() {
		VerticalLayout body = new VerticalLayout();
		body.setMargin(true);
		body.setSpacing(true);
		
		username = new TextField("Username");
		username.setWidth("100%");
		body.addComponent(username);
		
		password = new PasswordField("Password");
		password.setWidth("100%");
		body.addComponent(password);
		
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		body.addComponent(buttons);
		body.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
		
		loginButton = new Button("Login");
		loginButton.setClickShortcut(KeyCode.ENTER);
		loginButton.addListener(createLoginButtonListener());
		loginButton.addStyleName(Reindeer.BUTTON_DEFAULT);
		buttons.addComponent(loginButton);
		
		return body;
	}
	
	@SuppressWarnings("serial")
	private Button.ClickListener createLoginButtonListener() {
		return new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				getPresenter().attemptLogin((String) username.getValue(),
						(String) password.getValue());
			}
		};
	}

	@Override
	public void showLoginFailed() {
		viewLayout.getWindow().showNotification("Login failed", "Please try again.", Notification.TYPE_ERROR_MESSAGE);
	}

	@Override
	public void clearForm() {
		username.setValue("");
		password.setValue("");
		username.focus();
	}

}
