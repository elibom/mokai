package org.mokai.ui.field;

/**
 *
 * @author German Escobar
 */
public abstract class InputField {

	private String name;

	private String label;

	public InputField() {

	}

	public InputField(String name, String label) {
		this.name = name;
		this.label = label;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getLabel() {
		return label;
	}

	public final void setLabel(String label) {
		this.label = label;
	}

	public abstract String getType();

}
