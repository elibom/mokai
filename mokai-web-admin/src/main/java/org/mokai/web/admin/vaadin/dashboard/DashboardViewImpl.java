package org.mokai.web.admin.vaadin.dashboard;

import org.mokai.ConnectorService;
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
	
	private VerticalLayout applicationsLayout;
	
	private VerticalLayout connectionsLayout;
	
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
		applicationsLayout = createReceiversLayout();
		viewLayout.addComponent(applicationsLayout);
		
		// spacer
		Label spacer = new Label();
		spacer.setHeight("1em");
		viewLayout.addComponent(spacer);
		viewLayout.setExpandRatio(spacer, 1.0f);
		
		// create the processors layout, fill it and add it
		connectionsLayout = createConnectionsLayout();
		viewLayout.addComponent(connectionsLayout);
		
		// create and add the refresher
		Refresher refresher = createRefresher(REFRESHER_INTERVAL);
		viewLayout.addComponent(refresher);
		
	}
	
	/**
	 * Helper method. Creates the layout that will hold the applications.
	 * 
	 * @return n initialized layout.
	 */
	private VerticalLayout createReceiversLayout() {
		
		// create the layout and set the style
		VerticalLayout receiversLayout = new VerticalLayout();
		receiversLayout.setMargin(true);
		receiversLayout.setSpacing(true);
		
		// fill the receivers and add it to the outer layout
		fillApplicationsLayout(receiversLayout);
		
		return receiversLayout;
	}
	
	/**
	 * Helper method. Adds the applications to the layout.
	 * 
	 * @param applicationsLayout the layout to which we are adding the applications.
	 */
	private void fillApplicationsLayout(VerticalLayout applicationsLayout) {
		
		RoutingEngine routingEngine = getRoutingEngine();
		
		// delete all components
		applicationsLayout.removeAllComponents();
		
		// title
		applicationsLayout.addComponent(new Label("<h2>Applications</h2>", Label.CONTENT_XHTML));
		
		// applications
		for (final ConnectorService application : routingEngine.getApplications()) {
			VerticalLayout receiverLayout = new ConnectorComponent(getPresenter(), application);
			applicationsLayout.addComponent(receiverLayout);
		}
		
	}
	
	/**
	 * Helper method. Creates the layout that will hold the processors.
	 * 
	 * @return an initialized layout.
	 */
	private VerticalLayout createConnectionsLayout() {
		
		// create the layout and set the style
		VerticalLayout connectorsLayout = new VerticalLayout();
		connectorsLayout.setMargin(true);
		connectorsLayout.setSpacing(true);
		
		fillConnectionsLayout(connectorsLayout);
		
		return connectorsLayout;
	}
	
	/**
	 * Helper method. Adds the connections to the layout.
	 * 
	 * @param connectionsLayout the layout to which we are adding the connections.
	 */
	private void fillConnectionsLayout(VerticalLayout connectionsLayout) {
		
		RoutingEngine routingEngine = getRoutingEngine();
		
		// delete all components
		connectionsLayout.removeAllComponents();
		
		// title
		connectionsLayout.addComponent(new Label("<h2>Connections</h2>", Label.CONTENT_XHTML));
		
		// connections
		for (final ConnectorService connection : routingEngine.getConnections()) {
			VerticalLayout connectorLayot = new ConnectorComponent(getPresenter(), connection);
			connectionsLayout.addComponent(connectorLayot);
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
				fillApplicationsLayout(applicationsLayout);
				fillConnectionsLayout(connectionsLayout);
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
