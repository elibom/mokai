package org.mokai.web.admin.vaadin.pwd;

import com.github.peholmst.mvp4vaadin.View;

/**
 * 
 * @author German Escobar
 */
public interface PasswordView extends View {

	void passwordsDontMatch();
	
	void passwordNotChanged();
	
}
