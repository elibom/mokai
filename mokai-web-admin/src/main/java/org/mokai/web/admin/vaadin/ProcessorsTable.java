package org.mokai.web.admin.vaadin;

import java.util.List;

import org.mokai.ProcessorService;
import org.mokai.RoutingEngine;
import org.mokai.Monitorable.Status;
import org.mokai.Service.State;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;

public class ProcessorsTable extends Table {

	private static final long serialVersionUID = 1L;
	
	private RoutingEngine routingEngine;

	public ProcessorsTable(RoutingEngine routingEngine) {
		super();
		
		this.routingEngine = routingEngine;
		
		addContainerProperty("Processor Id", String.class, null);
		addContainerProperty("Queued Msgs", Integer.class, 0);
		addContainerProperty("Status", String.class, null);
		addContainerProperty("State", String.class, null);
		addContainerProperty("", Button.class, null);
	}
	
	public void loadData() {
		removeAllItems();
		
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
						loadData();
					}
				});
			} else {
				btnManage.addListener(new Button.ClickListener() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						processor.stop();
						loadData();
					}
				});
			}

			addItem(
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
		
		setCellStyleGenerator(new Table.CellStyleGenerator() {
			
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
