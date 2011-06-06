package org.mokai.web.admin.vaadin.main;

import org.mokai.web.admin.vaadin.dashboard.DashboardViewImpl;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;

public class MainViewImpl extends AbstractView<MainView, MainPresenter> implements MainView, VaadinView {
	
	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = -8611844346722276070L;

	private VerticalLayout viewLayout;
	
	private WindowHeader windowHeader;
	
	public MainViewImpl() {
		super(true);
	}

	@Override
	public String getDescription() {
		return "Web Admin Console";
	}

	@Override
	public String getDisplayName() {
		return "Mokai";
	}

	@Override
	protected MainPresenter createPresenter() {
		return new MainPresenter(this);
	}

	@Override
	protected void initView() {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		
		windowHeader = createWindowHeader();
		viewLayout.addComponent(windowHeader);
		
		DashboardViewImpl dashboardView = new DashboardViewImpl();
		ComponentContainer dashboardComponent = dashboardView.getViewComponent();
		viewLayout.addComponent(dashboardComponent);
		viewLayout.setExpandRatio(dashboardComponent, 1.0F);
		
	}

	@Override
	public ComponentContainer getViewComponent() {
		return viewLayout;
	}

	private WindowHeader createWindowHeader() {
		return new WindowHeader(getPresenter());
	}

}
