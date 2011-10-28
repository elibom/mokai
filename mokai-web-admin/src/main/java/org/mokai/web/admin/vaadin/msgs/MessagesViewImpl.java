package org.mokai.web.admin.vaadin.msgs;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.Message.Status;
import org.mokai.RoutingEngine;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageCriteria.OrderType;
import org.mokai.persist.MessageStore;
import org.mokai.web.admin.vaadin.WebAdminContext;

import com.github.peholmst.mvp4vaadin.AbstractView;
import com.github.peholmst.mvp4vaadin.VaadinView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class MessagesViewImpl extends AbstractView<MessagesView, MessagesPresenter> implements VaadinView, MessagesView {

	private static final long serialVersionUID = 1L;
	
	private Map<Long,Message> messagesMap;
	
	private VerticalLayout viewLayout;
	
	private Table messagesTable;
	
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

	@SuppressWarnings("serial")
	@Override
	protected void initView() {
		
		// outer layout
		viewLayout = new VerticalLayout();
		viewLayout.setSpacing(true);
		viewLayout.setMargin(true);
		viewLayout.setSizeFull();
		
		Button refreshBtn = new Button("Refresh");
		refreshBtn.addStyleName(BaseTheme.BUTTON_LINK);
		refreshBtn.addListener(refreshButtonListener());
		viewLayout.addComponent(refreshBtn);
		viewLayout.setComponentAlignment(refreshBtn, Alignment.MIDDLE_RIGHT);
		
		messagesTable = new Table();
		messagesTable.setSizeFull();
		messagesTable.setPageLength(15);
		messagesTable.setSelectable(true);
		
		messagesTable.addContainerProperty("Id", Long.class, null);
		messagesTable.addContainerProperty("Date", String.class, null);
		messagesTable.addContainerProperty("Source", String.class,  null);
		messagesTable.addContainerProperty("Destination", String.class,  null);
		messagesTable.addContainerProperty("Status", String.class,  null);
		messagesTable.addContainerProperty("To", String.class,  null);
		messagesTable.addContainerProperty("From", String.class,  null);
		messagesTable.addContainerProperty("Sequence", Integer.class,  null);
		messagesTable.addContainerProperty("Msg Id", String.class,  null);
		messagesTable.addContainerProperty("Cmd Status", Integer.class,  null);
		messagesTable.addContainerProperty("Receipt", String.class,  null);
		messagesTable.addContainerProperty("Receipt Date", String.class,  null);
		messagesTable.addContainerProperty("Text", String.class,  null);
		
		messagesTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

			@Override
			public String getStyle(Object itemId, Object propertyId) {
				
				if (propertyId == null) {
					return null;
				}
				
				Message message = messagesMap.get((Long) itemId);
				
				if (propertyId.equals("Status")) {
					if (message.getStatus().equals(Status.FAILED)) {
						return "red";
					}
				}
				
				if (propertyId.equals("Cmd Status")) {
					int commandStatus = message.getProperty("commandStatus", Integer.class);
					if (commandStatus != 0) {
						return "red";
					}
				}
				
				if (propertyId.equals("Receipt")) {
					String receiptStatus = message.getProperty("receiptStatus", String.class);
					if ("DELIVRD".equals(receiptStatus)) {
						return "green";
					} else if (receiptStatus != null && !"".equals(receiptStatus)) {
						return "red";
					}
				}
				
				return null;
			}
			
		});
		
		fillTableData(messagesTable);
		
		viewLayout.addComponent(messagesTable);
		viewLayout.setExpandRatio(messagesTable, 1.0F);
	}
	
	private void fillTableData(Table messagesTable) {
		messagesTable.removeAllItems();
		
		RoutingEngine routingEngine = getRoutingEngine();
		MessageStore messageStore = routingEngine.getMessageStore();
		
		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.TO_CONNECTIONS)
			.orderBy("id")
			.orderType(OrderType.DOWNWARDS);
		Collection<Message> messages = messageStore.list(criteria);
	
		messagesMap = new HashMap<Long,Message>();
		for (Message message : messages) {
			messagesMap.put(message.getId(), message);
		}
		
		for (Message message : messages) {
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			
			Date receiptDate = message.getProperty("receiptTime", Date.class);
			String strReceiptDate = "";
			if (receiptDate != null) {
				strReceiptDate = sdf.format(receiptDate);
			}
			
			Object[] data = new Object[] {
				message.getId(),
				sdf.format(message.getCreationTime()),
				message.getSource(),
				message.getDestination(),
				message.getStatus().toString(),
				message.getProperty("to", String.class),
				message.getProperty("from", String.class),
				message.getProperty("sequenceNumber", Integer.class),
				message.getProperty("messageId", String.class),
				message.getProperty("commandStatus", Integer.class),
				message.getProperty("receiptStatus", String.class),
				strReceiptDate,
				message.getProperty("text", String.class)
			};
			messagesTable.addItem(data, message.getId());
		}
	}
	
	@SuppressWarnings("serial")
	private Button.ClickListener refreshButtonListener() {
		return new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				fillTableData(messagesTable);
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
