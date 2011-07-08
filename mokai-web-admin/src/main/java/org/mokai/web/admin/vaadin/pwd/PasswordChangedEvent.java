package org.mokai.web.admin.vaadin.pwd;

import com.github.peholmst.mvp4vaadin.View;
import com.github.peholmst.mvp4vaadin.ViewEvent;
import com.vaadin.ui.Window;

/**
 * This event is fired when the password is successfully changed.
 * 
 * @author German Escobar
 */
public class PasswordChangedEvent extends ViewEvent {

	private static final long serialVersionUID = 1L;
	
	private Window passwordWindow;

	public PasswordChangedEvent(View view, Window passwordWindow) {
		super(view);
		this.passwordWindow = passwordWindow;
	}

	public Window getPasswordWindow() {
		return passwordWindow;
	}
	
}
