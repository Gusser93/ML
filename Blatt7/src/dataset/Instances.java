package dataset;

import com.sun.tools.javac.util.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Instances {
	List<Instance> instances;
	
	List<Attribute> attributes;


	public int classIndex = -1;

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
		for (Instance i : this.instances) {
			i.setClassIndex(index);
		}
	}
	public Instances(String csvPath, String delimiter) throws FileNotFoundException {
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
				if (value.startsWith("\"") && value.endsWith("\"")) {
					this.attributes.add(new Attribute(AttributeType.string,
							value.substring(1, value.length()-2)));
					indices.add(0.0);
				//} else if (false) {
					//Nach Test auf Zahl adde numeric usw.
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
}
