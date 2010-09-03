package org.mokai.ui.field;


public class SelectConnectorsField extends InputField {
	
	public static final String TYPE = "selectConnectors";
	
	public SelectConnectorsField() {
		super();
	}

	public SelectConnectorsField(String name, String label) {
		super(name, label);
	}

	@Override
	public final String getType() {
		return TYPE;
	}
	
}
