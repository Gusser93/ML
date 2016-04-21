import org.kramerlab.teaching.ml.datasets.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TODO exceptions
//TODO better dataset, attribute and co
/**
 * Created by Markus Vieth on 21.04.2016.
 */
public class DecisionTree {

    private Instance[] data;
    private Dataset dataset;

    /**
     * default constructor
     */
    public DecisionTree() {
    }

    /**
     * loads arff
     * @param path path to arff
     */
    public DecisionTree(String path) {
        try {
            this.loadArff(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * loads arff
     * @param file arff file
     */
    public DecisionTree(File file) {
        try {
            this.loadArff(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * loads arff
     * @param path path to arff
     * @throws Exception see kramerlabs dataset
     */
    public void loadArff(String path) throws Exception {
        File file = new File(path);
        this.loadArff(file);
    }

    /**
     * loads arff
     * @param file arff file
     * @throws Exception see kramerlabs dataset
     */
    public void loadArff(File file) throws Exception {
        this.dataset = new Dataset();
        dataset.load(file);
        this.data = new Instance[dataset.getNumberOfInstances()];

        for (int i = 0; i < data.length; i++) {
            this.data[i] = dataset.getInstance(i);
        }
    }

    /**
     * calculates information gain for given attribute and instances
     * @param attribute given attribute
     * @param indices given indices of instances
     * @return information gain
     */
    public double informationGain(Attribute attribute, int... indices) {
        List<Attribute> attributes = dataset.getAttributes();
        // I assume that the last attribute is the class attribute.
        // Alternative there could be a member variable in data set
        Attribute classAttr = attributes.get(attributes.size() - 1);
        //TODO test if nominal
        NominalAttribute attr = (NominalAttribute) attribute;
        // Init gain
        double gain = calculateEntropy((NominalAttribute)classAttr,
                indices);

        // sum over all values in attribute
        for (int v = 0; v < attr.getNumberOfValues(); v++) {
            List<Integer> subIndices = new ArrayList<>();
            // Alternative we could copy data in a list and remove already
            // picked instances to improve runtime
            // creates subset with indices of instances with value v
            for (int i : indices) {
                Instance instance = data[i];
                NominalValue value = (NominalValue)instance.getValue(attr);
                if (attr.getValue(v).equals(value)) {
                    subIndices.add(i);
                }
            }

            // calculates entropy
            double entropy = calculateEntropy((NominalAttribute)classAttr,
                    subIndices);

            // see formula
            gain -= entropy * ((double)subIndices.size())/((double)indices.length);
        }

        return gain;
    }

    /**
     * calculates entropy
     * @param classAttr given class attribute
     * @param indices indices of instances
     * @return entropy
     */
    private double calculateEntropy(NominalAttribute classAttr, int[] indices
    ) {
        int[] values = new int[classAttr.getNumberOfValues()];
        // calculates number of instances with value v in class attribute
        for (int v = 0; v < classAttr.getNumberOfValues(); v++) {
            values[v] = 0;
            for(int i : indices) {
                Instance instance = data[i];
                NominalValue value = (NominalValue)instance.getValue(classAttr);
                NominalValue classValue = classAttr.getValue(v);
                if (classValue.equals(value)) {
                    values[v]++;
                }
            }
        }
        return calculateEntropy(values);
    }

    /**
     * calculates entropy
     * @param classAttr given class attribute
     * @param indices indices of instances
     * @return entropy
     */
    private double calculateEntropy(NominalAttribute classAttr, List<Integer>
            indices
    ) {
        int[] values = new int[classAttr.getNumberOfValues()];
        // calculates number of instances with value v in class attribute
        for (int v = 0; v < classAttr.getNumberOfValues(); v++) {
            values[v] = 0;
            for(int i : indices) {
                Instance instance = data[i];
                NominalValue value = (NominalValue)instance.getValue(classAttr);
                NominalValue classValue = classAttr.getValue(v);
                if (classValue.equals(value)) {
                    values[v]++;
                }
            }
        }
        return calculateEntropy(values);
    }

    /**
     * calculates entropy
     * @param values given values
     * @return entropy
     */
    private double calculateEntropy(int[] values) {
        double sum = 0;
        for (int i : values) {
            sum += i;
        }
        double entropy = 0.0;

        for (int value : values) {
            double p = value/sum;
            entropy -= p * log2(p);
        }

        return entropy;
    }

    /**
     * calculates log to base 2
     * @param a given parameter
     * @return log2(a) or 0 if a == 0
     */
    private double log2(double a) {
        if ( Double.compare(0.0, Math.abs(a)) == 0 )
            return 0;
        return Math.log(a) / Math.log(2);
    }

    /**
     * prints some test data
     */
    private void testPrint() {
        int[] indices = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            indices[i] = i;
        }
        for (Attribute attr : this.dataset.getAttributes()) {
            System.out.print("Attribute " + attr.getName());
            System.out.print(" has an InformationGain of " + informationGain
                    (attr, indices));
            System.out.println();
        }
    }

    /**
     * a test
     * @param args none
     */
    public static void main(String[] args) {
        DecisionTree dt = new DecisionTree("res/weather.nominal.arff");
        dt.testPrint();
    }
}
