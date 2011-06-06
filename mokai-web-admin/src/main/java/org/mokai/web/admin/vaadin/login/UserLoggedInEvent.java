package org.mokai.web.admin.vaadin.login;

import com.github.peholmst.mvp4vaadin.View;
import com.github.peholmst.mvp4vaadin.ViewEvent;

public class UserLoggedInEvent extends ViewEvent {
	
	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 8181324577914257235L;
	
	private final String username;

	public UserLoggedInEvent(View source, String username) {
		super(source);
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

}
