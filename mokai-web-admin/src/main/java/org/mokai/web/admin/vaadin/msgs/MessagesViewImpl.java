package org.mokai.web.admin.vaadin.msgs;

import java.util.Collection;

import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.RoutingEngine;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.web.admin.vaadin.WebAdminContext;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author German Escobar
 */
public class MessagesViewImpl extends AbstractView<MessagesView, MessagesPresenter> implements VaadinView, MessagesView {

	private static final long serialVersionUID = 1L;
	
	private VerticalLayout viewLayout;
	
	private MessagesTable messagesTable;
	
	private TextField recipient;
	
	private CheckBox sentMessages;
	
	private CheckBox failedMessages;
	
	private CheckBox unroutedMessages;
	
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
		
		HorizontalLayout searchForm = new HorizontalLayout();
		searchForm.setWidth(420, HorizontalLayout.UNITS_PIXELS);
		searchForm.setSpacing(true);
		
		// mobile field
		recipient = new TextField();
		recipient.setInputPrompt("Recipient");
		searchForm.addComponent(recipient);
		
		sentMessages = new CheckBox("Sent", true);
		searchForm.addComponent(sentMessages);
		
		failedMessages = new CheckBox("Failed", true);
		searchForm.addComponent(failedMessages);
		
		unroutedMessages = new CheckBox("Unrouted", true);
		searchForm.addComponent(unroutedMessages);
		
		// refresh button
		Button refreshBtn = new Button("Refresh");
		refreshBtn.addListener(refreshButtonListener());
		searchForm.addComponent(refreshBtn);
		searchForm.setComponentAlignment(refreshBtn, Alignment.MIDDLE_RIGHT);
		
		viewLayout.addComponent(searchForm);
		
		// messages table
		messagesTable = new MessagesTable();
		messagesTable.populate(loadMessages());
		
		viewLayout.addComponent(messagesTable);
		viewLayout.setExpandRatio(messagesTable, 1.0F);
	}
	
	@SuppressWarnings("serial")
	private Button.ClickListener refreshButtonListener() {
		return new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				
				messagesTable.populate(loadMessages());
			}
		};
	}
	
	/**
	 * Helper method. Retrieves the messages from the database using the specified criteria.
	 * 
	 * @return a collection of Message objects or an empty collection if no record matches. 
	 */
	private Collection<Message> loadMessages() {
		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.TO_CONNECTIONS)
			.orderBy("id")
			.orderType(OrderType.DOWNWARDS)
			.numRecords(2000);
		
		// add recipient criteria
		if (recipient.getValue() != null && !"".equals(recipient.getValue())) {
			criteria.addProperty("smsc_to", recipient.getValue());
		}
		
		// add status criteria ONLY IF at least one of the checkboxes is not checked
		boolean addStatusCriteria = !sentMessages.booleanValue() || !failedMessages.booleanValue() || !unroutedMessages.booleanValue();
		if (addStatusCriteria) {
			addStatusCriteria(criteria);
		}
		
		return getRoutingEngine().getMessageStore().list(criteria);
	}
	
	/**
	 * Helper method. Add status criteria to the MessageCriteria object according to the selected status checkboxes.
	 * 
	 * @param criteria the MessageCriteria object to which we are adding the status criteria.
	 */
	private void addStatusCriteria(MessageCriteria criteria) {
		if (sentMessages.booleanValue()) {
			criteria.addStatus(Message.STATUS_PROCESSED);
		}
			
		if (failedMessages.booleanValue()) {
			criteria.addStatus(Message.STATUS_FAILED).addStatus(Message.STATUS_RETRYING);
		}
			
		if (unroutedMessages.booleanValue()) {
			criteria.addStatus(Message.STATUS_UNROUTABLE);
		}
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
