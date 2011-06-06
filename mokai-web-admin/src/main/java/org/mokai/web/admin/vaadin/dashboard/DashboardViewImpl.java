package org.mokai.web.admin.vaadin.dashboard;

import org.mokai.ProcessorService;
import org.mokai.ReceiverService;
import org.mokai.RoutingEngine;
import org.mokai.web.admin.vaadin.WebAdminContext;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.github.wolfie.refresher.Refresher;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * The dashboard view.
 * 
 * @author German Escobar
 */
public class DashboardViewImpl extends AbstractView<DashboardView, DashboardPresenter> implements
		DashboardView, VaadinView {

	private static final long serialVersionUID = 7684553035917369296L;
	
	private static final long REFRESHER_INTERVAL = 5000;

	private HorizontalLayout viewLayout;
	
	private VerticalLayout receiversLayout;
	
	private VerticalLayout processorsLayout;
	
	/**
	 * Constructor.
	 */
	public DashboardViewImpl() {
		super(true);
	}

	@Override
	public String getDisplayName() {
		return "Mokai";
	}
	
	@Override
	public String getDescription() {
		return "Web Admin Console";
	}

	@Override
	protected DashboardPresenter createPresenter() {
		return new DashboardPresenter(this);
	}

	/**
	 * This is the method that actually creates and build the layout. It is called from the parent class.
	 */
	@Override
	protected void initView() {
		
		// outer layout
		viewLayout = new HorizontalLayout();
		
		// create the receivers layout, fill it and add it
		receiversLayout = createReceiversLayout();
		viewLayout.addComponent(receiversLayout);
		
		// spacer
		Label spacer = new Label();
		spacer.setHeight("1em");
		viewLayout.addComponent(spacer);
		viewLayout.setExpandRatio(spacer, 1.0f);
		
		// create the processors layout, fill it and add it
		processorsLayout = createProcessorsLayout();
		viewLayout.addComponent(processorsLayout);
		
		// create and add the refresher
		Refresher refresher = createRefresher(REFRESHER_INTERVAL);
		viewLayout.addComponent(refresher);
		
	}
	
	/**
	 * Helper method. Creates the layout that will hold the receivers.
	 * 
	 * @return n initialized layout.
	 */
	private VerticalLayout createReceiversLayout() {
		
		// create the layout and set the style
		VerticalLayout receiversLayout = new VerticalLayout();
		receiversLayout.setMargin(true);
		receiversLayout.setSpacing(true);
		
		// fill the receivers and add it to the outer layout
		fillReceiversLayout(receiversLayout);
		
		return receiversLayout;
	}
	
	/**
	 * Helper method. Adds the receivers to the layout.
	 * 
	 * @param receiversLayout the layout to which we are adding the receivers.
	 */
	private void fillReceiversLayout(VerticalLayout receiversLayout) {
		
		RoutingEngine routingEngine = getRoutingEngine();
		
		// delete all components
		receiversLayout.removeAllComponents();
		
		// title
		receiversLayout.addComponent(new Label("<h2>Receivers</h2>", Label.CONTENT_XHTML));
		
		// receivers
		for (final ReceiverService receiverService : routingEngine.getReceivers()) {
			VerticalLayout receiverLayout = new ReceiverComponent(getPresenter(), receiverService);
			receiversLayout.addComponent(receiverLayout);
		}
		
	}
	
	/**
	 * Helper method. Creates the layout that will hold the processors.
	 * 
	 * @return an initialized layout.
	 */
	private VerticalLayout createProcessorsLayout() {
		
		// create the layout and set the style
		VerticalLayout processorsLayout = new VerticalLayout();
		processorsLayout.setMargin(true);
		processorsLayout.setSpacing(true);
		
		fillProcessorsLayout(processorsLayout);
		
		return processorsLayout;
	}
	
	/**
	 * Helper method. Adds the processors to the layout.
	 * 
	 * @param processorsLayout the layout to which we are adding the processors.
	 */
	private void fillProcessorsLayout(VerticalLayout processorsLayout) {
		
		RoutingEngine routingEngine = getRoutingEngine();
		
		// delete all components
		processorsLayout.removeAllComponents();
		
		// title
		processorsLayout.addComponent(new Label("<h2>Processors</h2>", Label.CONTENT_XHTML));
		
		// receivers
		for (final ProcessorService processorService : routingEngine.getProcessors()) {
			VerticalLayout processorLayot = new ProcessorComponent(getPresenter(), processorService);
			processorsLayout.addComponent(processorLayot);
		}
	}
	
	/**
	 * Helper method. Creates a refresher object with the specified interval.
	 * 
	 * @param interval the interval of the refresher
	 * @return an initialized Refresher object.
	 */
	private Refresher createRefresher(long interval) {
		
		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(5000);
		refresher.addListener(new Refresher.RefreshListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void refresh(Refresher source) {
				fillReceiversLayout(receiversLayout);
				fillProcessorsLayout(processorsLayout);
			}
			
		});
		
		return refresher;
	}
	
	private RoutingEngine getRoutingEngine() {
		WebAdminContext context = WebAdminContext.getInstance();
		return context.getRoutingEngine();
	}

	@Override
	public ComponentContainer getViewComponent() {
		return viewLayout;
	}

}
