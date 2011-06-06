package org.mokai.web.admin.vaadin.dashboard;

import org.mokai.ProcessorService;
import org.mokai.ReceiverService;

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
	 * If the processor service is started, it is stopped and viceversa.
	 * 
	 * @param processorService the ProcessorService to which we are going to change the state.
	 */
	public void changeProcessorServiceState(ProcessorService processorService) {
		if (processorService.getState().isStartable()) {
			processorService.start();
		} else {
			processorService.stop();
		}
	}
	
	/**
	 * If the receiver service is started, it is stopped and viceversa.
	 * 
	 * @param receiverService the ReceiverService to which we are going to change the state.
	 */
	public void changeReceiverServiceState(ReceiverService receiverService) {
		if (receiverService.getState().isStartable()) {
			receiverService.start();
		} else {
			receiverService.stop();
		}
	}

}
