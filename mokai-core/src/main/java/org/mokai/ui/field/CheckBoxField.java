package org.mokai.ui.field;

public class CheckBoxField extends InputField {
	
	public static final String TYPE = "checkBox";

	public CheckBoxField() {
		super();
	}

	public CheckBoxField(String name, String label) {
		super(name, label);
	}

	@Override
	public final String getType() {
		return TYPE;
	}

}
