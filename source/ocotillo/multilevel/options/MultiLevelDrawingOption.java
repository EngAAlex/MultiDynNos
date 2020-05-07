package ocotillo.multilevel.options;

public class MultiLevelDrawingOption<V> {

	protected boolean applyOnLastLevelOnly = false;

	protected V value;
	/*public MultiLevelOption() {
		
	}*/
	
	public MultiLevelDrawingOption(V newValue) {
		this(newValue, false);
	}
	
	public MultiLevelDrawingOption(V newValue, boolean applyOnLastLevelOnly) {
		this.value = newValue;
		this.applyOnLastLevelOnly = applyOnLastLevelOnly;
	}
//
//	public String getName() {
//		return toString();
//	}
//
//	public String getDescription() {
//		return "";
//	}

	/*public V getDefaultValue() {
		return defaultValue;
	}*/

	public V getValue() {
		return value;
	}
	
	public boolean applyOnLastLevelOnly() {
		return applyOnLastLevelOnly;
	}
	
	
}
