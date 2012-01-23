package org.mokai.web.admin.vaadin.msgs;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mokai.Message;

import com.vaadin.ui.Table;

/**
 * This is the table that shows the messages. Notice that the table is initialized with no data; you 
 * have to explicitly call {@link #loadData()}.
 * 
 * @author German Escobar
 */
public class MessagesTable extends Table {

	private static final long serialVersionUID = 1L;
	
	private Map<Long,Message> messagesMap;
	
	/**
	 * Constructor. Initializes the table.
	 * 
	 * @param messageStore from which we are retriving the messages.
	 */
	public MessagesTable() {
		super();
		
		init();
	}
	
	/**
	 * Initialize the table style and headers.
	 */
	private void init() {
		
		// set style
		setSizeFull();
		setPageLength(15);
		setSelectable(true);
		
		// add table headers
		addTableHeaders();
		
		// use a custom cell style generator to show delivered messages (green), failed messages (red) or unroutable messages (blue)
		setCellStyleGenerator(new CustomCellStyleGenerator());
	}
	
	/**
	 * Helper method to add table headers.
	 */
	private void addTableHeaders() {
		
		addContainerProperty("Id", Long.class, null);
		addContainerProperty("Date", String.class, null);
		addContainerProperty("Source", String.class,  null);
		addContainerProperty("Destination", String.class,  null);
		addContainerProperty("Status", String.class,  null);
		addContainerProperty("To", String.class,  null);
		addContainerProperty("From", String.class,  null);
		addContainerProperty("Sequence", Integer.class,  null);
		addContainerProperty("Msg Id", String.class,  null);
		addContainerProperty("Cmd Status", Integer.class,  null);
		addContainerProperty("Receipt", String.class,  null);
		addContainerProperty("Receipt Date", String.class,  null);
		addContainerProperty("Text", String.class,  null);
		
	}
	
	/**
	 * Cleans the table and reloads the data.
	 */
	public void populate(Collection<Message> messages) {
		removeAllItems();
	
		messagesMap = new HashMap<Long,Message>();
		for (Message message : messages) {
			messagesMap.put(message.getId(), message);
		}
		
		for (Message message : messages) {
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			
			Timestamp receiptDate = message.getProperty("receiptTime", Timestamp.class);
			String strReceiptDate = "";
			if (receiptDate != null) {
				strReceiptDate = sdf.format(receiptDate);
			}
			
			Object[] data = new Object[] {
				message.getId(),
				sdf.format(message.getCreationTime()),
				message.getSource(),
				message.getDestination(),
				getStringStatus(message.getStatus()),
				message.getProperty("to", String.class),
				message.getProperty("from", String.class),
				message.getProperty("sequenceNumber", Integer.class),
				message.getProperty("messageId", String.class),
				message.getProperty("commandStatus", Integer.class),
				message.getProperty("receiptStatus", String.class),
				strReceiptDate,
				message.getProperty("text", String.class)
			};
			addItem(data, message.getId());
		}
	}
	
	private String getStringStatus(byte status) {
		if (status == Message.STATUS_CREATED) {
			return "CREATED";
		}
		
		if (status == Message.STATUS_PROCESSED) {
			return "SENT";
		}
		
		if (status == Message.STATUS_FAILED) {
			return "FAILED";
		}
		
		if (status == Message.STATUS_RETRYING) {
			return "RETRYING";
		}
		
		if (status == Message.STATUS_UNROUTABLE) {
			return "UNROUTABLE";
		}
		
		return "UNKNOWN - " + status;
	}
	
	/**
	 * This custom CellStyleGenerator is used to show green, red or blue cells for delivered, failed
	 * or unroutable messages. Notice that only the cell is styled, not the whole row.
	 * 
	 * @author German Escobar
	 */
	private class CustomCellStyleGenerator implements Table.CellStyleGenerator {

		private static final long serialVersionUID = 1L;

		@Override
		public String getStyle(Object itemId, Object propertyId) {
			if (propertyId == null) {
				return null;
			}
			
			Message message = messagesMap.get((Long) itemId);
			
			if (propertyId.equals("Status")) {
				if (message.getStatus() == Message.STATUS_FAILED) {
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
		
	}
}
