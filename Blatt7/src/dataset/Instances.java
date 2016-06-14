package dataset;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Instances {
	List<Instance> instances;
	
	List<Attribute> attributes;

	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	public List<Instance> getInstances() {
		return this.instances;
	}

	public Attribute attribute(int attIndex) {
		return this.attributes.get(attIndex);
	}
	
	public Instances(String csvPath, String delimiter) throws FileNotFoundException {
		Scanner input = new Scanner(new File(csvPath));
		this.instances = new ArrayList<Instance>();
		this.attributes = new ArrayList<Attribute>();
		if (input.hasNextLine()) {
			String line = input.nextLine();
			Scanner lineIn = new Scanner(line);
			lineIn.useDelimiter(delimiter);
			while(lineIn.hasNext()) {
				String value = lineIn.next();
				if (value.startsWith("\"") && value.endsWith("\"")) {
					this.attributes.add(new Attribute(AttributeType.string,
							value.substring(1, value.length()-2)));
				//} else if (false) {
					//Nach Test auf Zahl adde numeric usw.
				} else {
					this.attributes.add(new Attribute(AttributeType.nominal, value));
				}
			}
		}
			
		while (input.hasNextLine()) {
			String line = input.nextLine();
			Scanner lineIn = new Scanner(line);
			lineIn.useDelimiter(delimiter);
			for (int i = 0; lineIn.hasNext(); i++) {
				Attribute temp = attributes.get(i);
				temp.addValue(lineIn.next());
			}
			
		}
	}
}
