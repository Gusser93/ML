package dataset;
import java.util.ArrayList;


public class Instance {
	//Value is index of value in Instances or numeric value 
	private class Value {
		Attribute attribute;
		double value;
		
		Value(Attribute attribute, double value) {
			this.attribute = attribute;
			this.value = value;
		}
	}
	
	private double weight;

	private ArrayList<Value> values;
	private int classIndex = -1;
	private Instances dataset;

	public Instance(Instance orig, String classLabel) throws Exception {
		this.values = new ArrayList<>();
		this.dataset = orig.dataset();
		Attribute classAttr = orig.classAttribute();
		for (int i = 0; i < orig.values.size(); i++) {
			if (i == orig.classIndex) {
				values.add(new Value(dataset.attribute(i), classAttr.getIndex
						(classLabel)));
			} else {
				values.add(new Value(dataset.attribute(i), orig.value(i)));
			}
		}
		this.weight = orig.weight;

		this.dataset = orig.dataset();
	}
	
	public Instance(Instances data, double weight, double[] attValues) {
		this.values = new ArrayList<Value>();
		for (int i = 0; i < attValues.length; i++) {
			values.add(new Value(data.attribute(i), attValues[i]));
		}
		this.weight = weight;
		
		this.dataset = data;
	}
	
	public Instance(double weight, double[] attValues) {
		this.values = new ArrayList<Value>();
		for (int i = 0; i < attValues.length; i++) {
			values.add(new Value(null, attValues[i]));
		}
		this.weight = weight;
		
		this.dataset = null;		
	}
	
	public Instance(int numAttributes) {
		this.values = new ArrayList<Value>();
		for (int i = 0; i < numAttributes; i++) {
			values.add(new Value(null, Double.NaN));
		}
		this.weight = 1;	
		
		this.dataset = null;
	}

	public Instances dataset() {
		return this.dataset;
	}

	public Attribute attribute(int index) {
		return this.values.get(index).attribute;
	}
	
	public Attribute classAttribute() throws Exception {
		if (this.classIsMissing()) {
			throw new Exception("Class index ist not set!");
		}
		return this.attribute(this.classIndex);
	}
	
	public boolean classIsMissing() {
		return classIndex < 0;
	}
	
	public double classValue() throws Exception {
		if (this.classIsMissing()) {
			throw new Exception("Class index ist not set!");
		}
		return value(classIndex);
	}

	public String classValueString() throws Exception {
		if (this.classIsMissing()) {
			throw new Exception("Class index ist not set!");
		}
		return this.stringValue(classIndex);
	}

	public double value(int attIndex) {
		return values.get(attIndex).value;
	}
	
	public String stringValue(int attIndex) {
		Attribute attribute = attribute(attIndex);
		double index = value(attIndex);
		if (attribute.isType(AttributeType.numeric)) {
			return Double.toString(index);
		}
		if (index < 0) {
			return "?";
		}
		return attribute.getString((int) index);
	}

	protected void setClassIndex(int index) {
		this.classIndex = index;
	}

	public String toString(String delimiter) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < this.values.size(); i++) {
			String value = this.stringValue(i);
			if (this.attribute(i).isType(AttributeType.string)) {
				result.append("\"");
				result.append(value);
				result.append("\"");
			} else {
				result.append(value);
			}
			result.append(delimiter);
		}
		return result.toString();
	}

	public String toString() {
		return this.toString(",");
	}
}
