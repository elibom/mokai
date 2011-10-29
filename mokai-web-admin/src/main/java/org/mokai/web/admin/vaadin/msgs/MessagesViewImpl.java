package org.mokai.web.admin.vaadin.msgs;

import org.mokai.RoutingEngine;
import org.mokai.web.admin.vaadin.WebAdminContext;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * @author German Escobar
 */
public class MessagesViewImpl extends AbstractView<MessagesView, MessagesPresenter> implements VaadinView, MessagesView {

	private static final long serialVersionUID = 1L;
	
	private VerticalLayout viewLayout;
	
	private MessagesTable messagesTable;
	
	/**
	 * Constructor
	 */
	public MessagesViewImpl() {
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
	protected MessagesPresenter createPresenter() {
		return new MessagesPresenter(this);
	}

	@Override
	protected void initView() {
		
		// outer layout
		viewLayout = new VerticalLayout();
		viewLayout.setSpacing(true);
		viewLayout.setMargin(true);
		viewLayout.setSizeFull();
		
		// refresh button
		Button refreshBtn = new Button("Refresh");
		refreshBtn.addStyleName(BaseTheme.BUTTON_LINK);
		refreshBtn.addListener(refreshButtonListener());
		viewLayout.addComponent(refreshBtn);
		viewLayout.setComponentAlignment(refreshBtn, Alignment.MIDDLE_RIGHT);
		
		// messages table
		messagesTable = new MessagesTable(getRoutingEngine().getMessageStore());
		messagesTable.loadData();
		
		viewLayout.addComponent(messagesTable);
		viewLayout.setExpandRatio(messagesTable, 1.0F);
	}
	
	@SuppressWarnings("serial")
	private Button.ClickListener refreshButtonListener() {
		return new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				messagesTable.loadData();
			}
		};
	}

	@Override
	public ComponentContainer getViewComponent() {
		return viewLayout;
	}
	
	private RoutingEngine getRoutingEngine() {
		WebAdminContext context = WebAdminContext.getInstance();
		return context.getRoutingEngine();
	}

}
