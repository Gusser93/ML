package dataset;

import java.util.*;

public class Attribute {

	// Index of attribute
	private Map<Integer, String> values;
	private AttributeType type;
	private int size;

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
			if(Instances.isString(value)) {
				value = stripString(value);
			}
			this.values.put(size++, value);
		}
	}

	protected int getIndex(String value) {
		for (Map.Entry entry : this.values.entrySet()) {
			if(Instances.isString(value)) {
				value = stripString(value);
			}
			if (entry.getValue().equals(value)) {
				return (Integer)entry.getKey();
			}
		}
		return -1;
	}
	
	protected String getString(int index) {
		return values.get(index);
	}
	
	protected boolean isType(AttributeType type) {
		return this.type.equals(type);
	}

	private static String stripString(String value) {
		return value.substring(1, value.length()-2);
	}
}
