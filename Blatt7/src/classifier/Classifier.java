package classifier;
import dataset.*;

import java.util.HashMap;

public interface Classifier {
	void buildClassifier(Instances data);
	
	String classifyInstance(Instance instance);

	HashMap<String, Double> distributionForInstace(Instance instance);
	
	Capabilities getCapabilities();
}
