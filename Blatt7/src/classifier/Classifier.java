package classifier;
import dataset.*;

public interface Classifier {
	void buildClassifier(Instances data);
	
	String classifyInstance(Instance instance);
	
	double[] distributionForInstace(Instance instance);
	
	Capabilities getCapabilities();
}
