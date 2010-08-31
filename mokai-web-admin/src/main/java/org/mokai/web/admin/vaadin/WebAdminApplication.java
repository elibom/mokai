package org.mokai.web.admin.vaadin;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.mokai.ProcessorService;
import org.mokai.RoutingEngine;
import org.mokai.Monitorable.Status;
import org.mokai.Service.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.henrik.refresher.Refresher;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class WebAdminApplication extends Application {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private RoutingEngine routingEngine;

	@Override
	public void init() {
		Window mainWindow = new Window();
		
		final Table table = new Table();
		table.addContainerProperty("Processor Id", String.class, null);
		table.addContainerProperty("Queued Msgs", Integer.class, 0);
		table.addContainerProperty("Status", String.class, null);
		table.addContainerProperty("State", String.class, null);
		table.addContainerProperty("", Button.class, null);
		
		loadData(table);
		
		mainWindow.addComponent(table);
		
		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(5000);
		refresher.addListener(new Refresher.RefreshListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void refresh(Refresher source) {
				loadData(table);
			}
			
		});
		
		mainWindow.addComponent(refresher);
		
		setTheme("mokai");
		
		setMainWindow(mainWindow);
	}
	
	private void loadData(final Table table) {
		Validate.notNull(table, "table was not provided");
		
		table.removeAllItems();
		
		List<ProcessorService> processors = routingEngine.getProcessors();
		for (final ProcessorService processor : processors) {
			String buttonCaption = "Start";
			if (processor.getState().isStoppable()) {
				buttonCaption = "Stop";
			}
			
			Button btnManage = new Button(buttonCaption);
			if (processor.getState().isStartable()) {
				btnManage.addListener(new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						processor.start();	
						loadData(table);
					}
				});
			} else {
				btnManage.addListener(new Button.ClickListener() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						processor.stop();
						loadData(table);
					}
				});
			}

			table.addItem(
					new Object[] { 
							processor.getId(), 
							processor.getNumQueuedMessages(), 
							processor.getStatus().toString(), 
							processor.getState().toString(),
							btnManage
					}, 
					processor.getId()
			);
			
		}
		
		table.setCellStyleGenerator(new Table.CellStyleGenerator() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public String getStyle(Object itemId, Object propertyId) {
				
				if (propertyId == null) {
					return null;
				}
				
				String processorId = itemId.toString();
				ProcessorService processor = routingEngine.getProcessor(processorId);
				
				if (propertyId.equals("Status")) {
					if (processor.getStatus().equals(Status.FAILED)) {
						return "red";
					} else if (processor.getStatus().equals(Status.OK)) {
						return "green";
					}
					
					return "gray";
				}
				
				if (propertyId.equals("State")) {
					if (processor.getState().equals(State.STARTED)) {
						return "green";
					}
					
					return "red";
				}
				
				return null;
				
			}
			
		});
	}

}
