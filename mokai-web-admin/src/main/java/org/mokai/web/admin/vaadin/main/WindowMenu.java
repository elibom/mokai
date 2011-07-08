package org.mokai.web.admin.vaadin.main;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class WindowMenu extends HorizontalLayout {

	private static final long serialVersionUID = 1L;
	
	private MainPresenter mainPresenter;
	
	public WindowMenu(MainPresenter mainPresenter) {
		this.mainPresenter = mainPresenter;
		
		setSizeFull();
		setSpacing(true);
		
		HorizontalLayout left = new HorizontalLayout();
		
		addComponent(left);
		setExpandRatio(left, 1.0F);

		HorizontalLayout right = new HorizontalLayout();
		right.addStyleName("menu");
		
		Button changePassword = new Button("Change Password");
		changePassword.addStyleName(BaseTheme.BUTTON_LINK);
		changePassword.addStyleName("menu");
		changePassword.addListener(createPasswordButtonListener());
		right.addComponent(changePassword);
		
		addComponent(right);
	}
	
	@SuppressWarnings("serial")
	private Button.ClickListener createPasswordButtonListener() {
		return new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				mainPresenter.createAndShowPasswordWindow();
			}
		};
	}
	
}
