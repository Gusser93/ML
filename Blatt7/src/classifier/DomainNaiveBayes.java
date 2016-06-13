package classifier;
import java.io.Serializable;
import dataset.*;

public class DomainNaiveBayes implements Classifier, Cloneable, Serializable {

	public void buildClassifier(Instances data) {
		return;
	}
	
	public double classifyInstance(Instance instance){
		return Double.NaN;
	}
	
	public double[] distributionForInstace(Instance instance){
		return null;
	}
	
	public Capabilities getCapabilities(){
		return null;
	}
}
