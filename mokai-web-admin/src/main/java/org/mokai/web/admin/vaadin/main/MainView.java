package org.mokai.web.admin.vaadin.main;

import com.github.peholmst.mvp4vaadin.View;

/**
 * 
 * @author German Escobar
 */
public interface MainView extends View {

	void createAndShowPasswordWindow();
	
	void createAndShowDashboard();
	
	void createAndShowMessages();
}
