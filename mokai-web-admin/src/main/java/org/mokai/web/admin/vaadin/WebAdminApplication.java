package org.mokai.web.admin.vaadin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.mokai.ProcessorService;
import org.mokai.RoutingEngine;
import org.mokai.Monitorable.Status;
import org.mokai.Service.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.henrik.refresher.Refresher;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class WebAdminApplication extends Application {

	private static final long serialVersionUID = 1L;
	
	private Logger log = LoggerFactory.getLogger(WebAdminApplication.class);
	
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
	
	@SuppressWarnings("unused")
	private class PropertyValue implements Property {

		private static final long serialVersionUID = 1L;
		
		private Object object;
		private String property;
		private boolean readOnly;
		
		public PropertyValue(Object object, String property) {
			this(object, property, true);
		}
		
		public PropertyValue(Object object, String property, boolean readOnly) {
			Validate.notNull(object);
			Validate.notEmpty(property);
			
			this.object = object;
			this.property = property;
			this.readOnly = readOnly;
		}

		@Override
		public Class<?> getType() {
			return object.getClass();
		}

		@Override
		public Object getValue() {
			try {
				Field field = object.getClass().getField(property);
				field.setAccessible(true);
				
				return field.get(object);
			} catch (NoSuchFieldException e) {
				log.error("NoSuchFieldException for property '" + property + "': " 
						+ e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				log.error("IllegalArgumentException for property '" + property + "': " 
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				log.error("IllegalAccessException for property '" + property + "': "
						+ e.getMessage(), e);
			}
			
			return null;
		}

		@Override
		public boolean isReadOnly() {
			return readOnly;
		}

		@Override
		public void setReadOnly(boolean newStatus) {
			this.readOnly = newStatus;
		}

		@Override
		public void setValue(Object newValue) throws ReadOnlyException,
				ConversionException {
			this.object = newValue;
		}
		
	}
	
	@SuppressWarnings("unused")
	private class MethodValue implements Property {

		private static final long serialVersionUID = 1L;
		
		private Object object;
		private String methodName;
		private boolean readOnly;
		
		public MethodValue(Object object, String methodName) {
			this(object, methodName, true);
		}
		
		public MethodValue(Object object, String methodName, boolean readOnly) {
			Validate.notNull(object);
			Validate.notEmpty(methodName);
			
			this.object = object;
			this.methodName = methodName;
			this.readOnly = readOnly;
		}

		@Override
		public Class<?> getType() {
			return object.getClass();
		}

		@Override
		public Object getValue() {
			try {
				Method method = object.getClass().getMethod(methodName);
				
				return method.invoke(object);
			} catch (NoSuchMethodException e) {
				log.error("NoSuchMethodException for method '" + methodName + "': " 
						+ e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				log.error("IllegalArgumentException for method '" + methodName + "': " 
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				log.error("IllegalAccessException for method '" + methodName + "': "
						+ e.getMessage(), e);
			} catch (InvocationTargetException e) {
				log.error("InvocationTargetException for method '" + methodName + "': "
						+ e.getMessage(), e);
			}
			
			return null;
		}

		@Override
		public boolean isReadOnly() {
			return readOnly;
		}

		@Override
		public void setReadOnly(boolean newStatus) {
			this.readOnly = newStatus;
		}

		@Override
		public void setValue(Object newValue) throws ReadOnlyException,
				ConversionException {
			this.object = newValue;
		}
	}

}
