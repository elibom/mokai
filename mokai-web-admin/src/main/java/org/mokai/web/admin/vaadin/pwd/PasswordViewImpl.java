package org.mokai.web.admin.vaadin.pwd;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

/**
 * 
 * @author German Escobar
 */
public class PasswordViewImpl extends AbstractView<PasswordView, PasswordPresenter> implements VaadinView, PasswordView {

	private static final long serialVersionUID = 1L;
	
	private HorizontalLayout viewLayout;
	private PasswordField password;
	private PasswordField confirmPassword;
	private Button changeButton;
	
	public PasswordViewImpl() {
		super(true);
	}
	
	@Override
	public String getDisplayName() {
		return "Mokai";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public ComponentContainer getViewComponent() {
		return viewLayout;
	}
	
	protected PasswordPresenter createPresenter() {
		return new PasswordPresenter(this);
	}
	
	@Override
	protected void initView() {
		VerticalLayout passwordPanel = new VerticalLayout();
		passwordPanel.setSpacing(true);
		passwordPanel.setStyleName(Reindeer.LAYOUT_WHITE);
		passwordPanel.setWidth("300px");
		
		VerticalLayout body = getBody();
		passwordPanel.addComponent(body);
		
		viewLayout = new HorizontalLayout();
		viewLayout.addComponent(passwordPanel);
		viewLayout.setComponentAlignment(passwordPanel, Alignment.TOP_CENTER);
		
		viewLayout.setSizeFull();
		
		password.focus();
	}
	
	private VerticalLayout getBody() {
		VerticalLayout body = new VerticalLayout();
		body.setMargin(true);
		body.setSpacing(true);
		
		password = new PasswordField("New Password");
		password.setWidth("100%");
		body.addComponent(password);
		
		confirmPassword = new PasswordField("Confirm Password");
		confirmPassword.setWidth("100%");
		body.addComponent(confirmPassword);
		
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		body.addComponent(buttons);
		body.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
		
		changeButton = new Button("Change");
		changeButton.setClickShortcut(KeyCode.ENTER);
		changeButton.addListener(passwordButtonListener());
		changeButton.addStyleName(Reindeer.BUTTON_DEFAULT);
		buttons.addComponent(changeButton);
		
		return body;
	}
	
	@SuppressWarnings("serial")
	private Button.ClickListener passwordButtonListener() {
		return new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				getPresenter().changePassword(viewLayout.getWindow(), (String) password.getValue(), 
						(String) confirmPassword.getValue()); 
			}
			
		};
	}

	@Override
	public void passwordsDontMatch() {
		viewLayout.getWindow().showNotification("Passwords don't match.", "Please correct the errors and try again", 
				Notification.TYPE_ERROR_MESSAGE);
	}

	@Override
	public void passwordNotChanged() {
		viewLayout.getWindow().showNotification("There was a problem changing the password.", "Please try again.", 
				Notification.TYPE_ERROR_MESSAGE);
	}
	
	
}
