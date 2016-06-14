package dataset;

import java.util.*;

public class Attribute {

	// Index of attribute
	Map<Integer, String> values;
	AttributeType type;
	int size;

	public Collection<String> getValues() {
		return this.values.values();
	}

	public AttributeType attributeType(){
		return this.type;
	}

	protected Attribute(AttributeType type, List<String> values) {
		size = 0;
		this.values = new HashMap<Integer, String>();
		this.type = type;
	}
	
	protected Attribute(AttributeType type, String value) {
		this(type, new ArrayList<String>());
		this.addValue(value);
	}
	
	protected void addValues(List<String> valueList) {
		for (String value: valueList) {
			this.addValue(value);
		}
	}
	
	protected void addValue(String value) {
		if (!this.values.containsValue(value)){
			this.values.put(size++, value);
		}
	}
	
	protected String getString(int index) {
		return values.get(index);
	}
	
	protected boolean isType(AttributeType type) {
		return this.type.equals(type);
	}
}
