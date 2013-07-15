package org.mokai.ui.field;

public class TextField extends InputField {

	public static final String TYPE = "text";

	public TextField() {
		super();
	}

	public TextField(String name, String label) {
		super(name, label);
	}

	@Override
	public final String getType() {
		return TYPE;
	}

}
