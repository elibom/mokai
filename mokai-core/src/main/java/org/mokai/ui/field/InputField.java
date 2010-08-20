package org.mokai.ui.field;

public abstract class InputField {

	private String name;
	
	private String label;
	
	public InputField() {
		
	}
	
	public InputField(String name, String label) {
		this.name = name;
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public abstract String getType();

}
