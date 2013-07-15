package org.mokai.action;

import org.mokai.Action;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

/**
 * Copies the value of a property into another property
 *
 * @author German Escobar
 */
public class CopyAction implements Action, ExposableConfiguration<CopyAction> {

	@Required
	@Label("From Field")
	private String from;

	@Required
	@Label("To Field")
	private String to;

	@Label("Delete From Field")
	private boolean deleteFrom = false;

	@Override
	public void execute(Message message) throws Exception {
		// validate input
		if (from == null) {
			throw new IllegalArgumentException("from property not provided");
		}
		if (to == null) {
			throw new IllegalArgumentException("to property not provided");
		}

		if (message.getProperty(from) != null) {
			message.setProperty(to, message.getProperty(from));

			if (deleteFrom) {
				message.removeProperty(from);
			}
		}
	}

	@Override
	public CopyAction getConfiguration() {
		return this;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public boolean isDeleteFrom() {
		return deleteFrom;
	}

	public void setDeleteFrom(boolean deleteFrom) {
		this.deleteFrom = deleteFrom;
	}

}
