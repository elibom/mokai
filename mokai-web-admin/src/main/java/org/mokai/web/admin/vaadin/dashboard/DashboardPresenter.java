package org.mokai.web.admin.vaadin.dashboard;

import org.mokai.ConnectorService;

import com.github.peholmst.mvp4vaadin.Presenter;

/**
 * Presenter of the dashboard view.
 * 
 * @author German Escobar
 */
public class DashboardPresenter extends Presenter<DashboardView> {

	private static final long serialVersionUID = 7574607911489534347L;

	public DashboardPresenter(DashboardView view) {
		super(view);
	}
	
	/**
	 * If the connection service is started, it is stopped and viceversa.
	 * 
	 * @param connectorService the ConnectorService to which we are going to change the state.
	 */
	public void changeConnectionServiceState(ConnectorService connectorService) {
		if (connectorService.getState().isStartable()) {
			connectorService.start();
		} else {
			connectorService.stop();
		}
	}
	
	/**
	 * If the application service is started, it is stopped and viceversa.
	 * 
	 * @param connectorService the ReceiverService to which we are going to change the state.
	 */
	public void changeApplicationServiceState(ConnectorService connectorService) {
		if (connectorService.getState().isStartable()) {
			connectorService.start();
		} else {
			connectorService.stop();
		}
	}

}
