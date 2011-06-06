package org.mokai.web.admin.vaadin.main;

import com.github.peholmst.mvp4vaadin.View;
import com.github.peholmst.mvp4vaadin.ViewEvent;

public class UserLoggedOutEvent extends ViewEvent {

	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 5671374247707090559L;

	public UserLoggedOutEvent(View view) {
		super(view);
	}

}
