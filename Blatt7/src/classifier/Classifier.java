package classifier;
import dataset.*;

public interface Classifier {
	void buildClassifier(Instances data);
	
	double classifyInstance(Instance instance);
	
	double[] distributionForInstace(Instance instance);
	
	Capabilities getCapabilities();
}
