package dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class Instances {
	private List<Instance> instances;
	
	private List<Attribute> attributes;


	private int classIndex = -1;

	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	public List<Instance> getInstances() {
		return this.instances;
	}

	public Attribute attribute(int attIndex) {
		return this.attributes.get(attIndex);
	}

	public void setClassIndex(int index) {
		this.classIndex = index;
		for (Instance i : this.instances) {
			i.setClassIndex(index);
		}
	}

	public Instances(Instances orig) {
		this.instances = new ArrayList<>();
		this.attributes = orig.getAttributes();
		this.setClassIndex(orig.classIndex);
	}

	public Instances(String csvPath, String delimiter)
			throws FileNotFoundException {
		Scanner input = new Scanner(new File(csvPath));
		this.instances = new ArrayList<Instance>();
		this.attributes = new ArrayList<Attribute>();
		if (input.hasNextLine()) {
			String line = input.nextLine();
			Scanner lineIn = new Scanner(line);
			lineIn.useDelimiter(delimiter);
			List<Double> indices = new ArrayList<>();
			while(lineIn.hasNext()) {
				String value = lineIn.next();
				if (isString(value)) {
					this.attributes.add(new Attribute(AttributeType.string,
							value));
					indices.add(0.0);
					} else if (isNumeric(value)) {
						//add numeric usw.
				} else {
					this.attributes.add(new Attribute(AttributeType.nominal, value));
					indices.add(0.0);
				}
			}

			double[] tmp = new double[indices.size()];
			for (int i=0; i<tmp.length; i++) {
				tmp[i] = indices.get(i);
			}

			this.instances.add(new Instance(this, 1, tmp));
		}

		while (input.hasNextLine()) {
			String line = input.nextLine();
			Scanner lineIn = new Scanner(line);
			lineIn.useDelimiter(delimiter);
			double[] indices = new double[attributes.size()];
			for (int i = 0; lineIn.hasNext(); i++) {
				Attribute temp = attributes.get(i);
				String value = lineIn.next();
				temp.addValue(value);
				int index = temp.getIndex(value);
				indices[i] = (double)index;
			}
			this.instances.add(new Instance(this, 1, indices));

		}


	}

	public Instances(Instances original, String csvPath, String delimiter)
			throws FileNotFoundException {
		Scanner input = new Scanner(new File(csvPath));
		this.instances = new ArrayList<Instance>();
		this.attributes = original.getAttributes();

		while (input.hasNextLine()) {
			String line = input.nextLine();
			Scanner lineIn = new Scanner(line);
			lineIn.useDelimiter(delimiter);
			double[] indices = new double[attributes.size()];
			boolean wasInsertet = true;
			String value = null;
			for (int i = 0; i < this.attributes.size(); i++) {

				if (wasInsertet && lineIn.hasNext()) {
					value = lineIn.next();
					wasInsertet = this.addValue(i, value, indices);
				} else if (!wasInsertet){
					wasInsertet = this.addValue(i, value, indices);
				} else {
					indices[i] = -1.0;
				}
			}
			this.instances.add(new Instance(this, 1, indices));

		}
		this.setClassIndex(original.classIndex);
	}

	public Instances(Instances origin, double split, Random random) {
		this.instances = new ArrayList<>();
		this.attributes = origin.getAttributes();

		List<Instance> shuffle = new ArrayList<>(origin.getInstances());
		Collections.shuffle(shuffle, random);
		int border = (int)Math.ceil(shuffle.size() * split);

		for (int i = border - 1; i >= 0; i--) {
			shuffle.remove(i);
		}

		this.instances = shuffle;
		this.setClassIndex(origin.classIndex);
	}

	/**
	 * TODO test if header is equal
	 * @param original
	 * @param sub
     */
	public Instances(Instances original, Instances sub) {
		this.instances = new ArrayList<>(original.getInstances());
		this.attributes = original.getAttributes();

		this.instances.removeAll(sub.getInstances());
		this.setClassIndex(original.classIndex);
	}

	private boolean addValue(int i, String value, double[] indices) {
		Attribute temp = attributes.get(i);
		if ((temp.isType(AttributeType.string) && isString(value)
		) || (temp.isType(AttributeType.numeric) && isNumeric(value)
		) || temp.isType(AttributeType.nominal) && !(isNumeric(value) ||
				isString(value))) {
			temp.addValue(value);
			int index = temp.getIndex(value);
			indices[i] = (double) index;
			return true;
		} else {
			indices[i] = -1.0;
			return false;
		}
	}

	public void addInstance(Instance data) {
		this.instances.add(data);
	}
	public int numInstances() {
		return this.instances.size();
	}

	public Instance getInstance(int index) {
		return this.instances.get(index);
	}

	public static boolean isNumeric(String value) {
		return value.matches("-?\\d+([\\.,]\\d+)?");
	}

	public static boolean isString(String value) {
		return value.startsWith("\"") && value.endsWith("\"");
	}

	public String toString(String delimiter) {
		StringBuilder result = new StringBuilder();
		for (Instance instance : this.instances) {
			result.append(instance.toString(delimiter));
			result.append('\n');
		}
		if (result.length() > 0) {
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}

	public String toString() {
		return this.toString(",");
	}


}
