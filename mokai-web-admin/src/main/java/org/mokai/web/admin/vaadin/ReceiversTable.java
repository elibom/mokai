package org.mokai.web.admin.vaadin;

import java.util.Collection;

import org.mokai.ReceiverService;
import org.mokai.RoutingEngine;
import org.mokai.Monitorable.Status;
import org.mokai.Service.State;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;

public class ReceiversTable extends Table {

	private static final long serialVersionUID = 1L;
	
	private RoutingEngine routingEngine;
	
	public ReceiversTable(RoutingEngine routingEngine) {
		super();
		
		this.routingEngine = routingEngine;
		
		addContainerProperty("Receiver Id", String.class, null);
		addContainerProperty("Status", String.class, null);
		addContainerProperty("State", String.class, null);
		addContainerProperty("", Button.class, null);
	}
	
	public void loadData() {
		removeAllItems();
		
		Collection<ReceiverService> receivers = routingEngine.getReceivers();
		for (final ReceiverService receiver : receivers) {
			String buttonCaption = "Start";
			if (receiver.getState().isStoppable()) {
				buttonCaption = "Stop";
			}
			
			Button btnManage = new Button(buttonCaption);
			if (receiver.getState().isStartable()) {
				btnManage.addListener(new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						receiver.start();	
						loadData();
					}
				});
			} else {
				btnManage.addListener(new Button.ClickListener() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						receiver.stop();
						loadData();
					}
				});
			}

			addItem(
					new Object[] { 
							receiver.getId(), 
							receiver.getStatus().toString(), 
							receiver.getState().toString(),
							btnManage
					}, 
					receiver.getId()
			);
		}
		
		setCellStyleGenerator(new Table.CellStyleGenerator() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public String getStyle(Object itemId, Object propertyId) {
				
				if (propertyId == null) {
					return null;
				}
				
				String receiverId = itemId.toString();
				ReceiverService receiver = routingEngine.getReceiver(receiverId);
				
				if (propertyId.equals("Status")) {
					if (receiver.getStatus().equals(Status.FAILED)) {
						return "red";
					} else if (receiver.getStatus().equals(Status.OK)) {
						return "green";
					}
					
					return "gray";
				}
				
				if (propertyId.equals("State")) {
					if (receiver.getState().equals(State.STARTED)) {
						return "green";
					}
					
					return "red";
				}
				
				return null;
				
			}
			
		});
	}
}
