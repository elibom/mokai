package org.mokai.ui.field;


public class SelectValuesField<T> extends InputField {
	
	public static final String TYPE = "selectValues";

	private T[] items;

	public SelectValuesField() {
		
	}

	public SelectValuesField(String name, String label) {
		super(name, label);
	}

	public final T[] getItems() {
		return items;
	}

	public final void setItems(T[] items) {
		this.items = items.clone();
	}

	@Override
	public final String getType() {
		return TYPE;
	}
}
