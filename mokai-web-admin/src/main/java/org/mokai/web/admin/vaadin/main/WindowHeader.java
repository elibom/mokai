package org.mokai.web.admin.vaadin.main;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class WindowHeader extends HorizontalLayout {
	
	/**
	 * Generated Serial Version UID. 
	 */
	private static final long serialVersionUID = 8577813175926779512L;
	
	private MainPresenter mainPresenter;

	public WindowHeader(MainPresenter mainPresenter) {
		this.mainPresenter = mainPresenter;
		
		setWidth("100%");
		setMargin(true);
		setSpacing(true);
		addStyleName(Reindeer.LAYOUT_BLACK);
		
		VerticalLayout title = createTitle();
		addComponent(title);
		setComponentAlignment(title, Alignment.MIDDLE_LEFT);
		setExpandRatio(title, 1.0F);
		
		Button logoutButton = createLogoutButton();
		addComponent(logoutButton);
		setComponentAlignment(logoutButton, Alignment.MIDDLE_RIGHT);
	}
	
	private VerticalLayout createTitle() {
		VerticalLayout layout = new VerticalLayout();

		Label appTitle = new Label("Mokai");
		appTitle.addStyleName(Reindeer.LABEL_H1);
		layout.addComponent(appTitle);
		
		Label appDescription = new Label("Web Admin Console");
		layout.addComponent(appDescription);
		
		return layout;
	}
	
	@SuppressWarnings("serial")
	private Button createLogoutButton() {
		Button button = new Button("Logout");
		button.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				mainPresenter.logout();
			}
		});
		button.addStyleName(Reindeer.BUTTON_SMALL);
		return button;
	}
	
}
