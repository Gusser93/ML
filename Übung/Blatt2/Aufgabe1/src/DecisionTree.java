import org.kramerlab.teaching.ml.datasets.*;

import java.io.File;
import java.util.*;

//TODO exceptions
/**
 * Created by David Klopp, Christian Stricker, Markus Vieth on 21.04.2016.
 */
public class DecisionTree {

    /**
     * Inner Node class
     */
    private class Node {
        Node parent;
        Attribute attribute = null;
        List<Edge> edges = new ArrayList<>();
        List<Integer> indices;
        List<Attribute> notVisited;
        boolean isSingleNode = false;
        Value value;

        /**
         * Constructor
         * @param indices
         */
        public Node(List<Integer> indices, Node parent) {
            this.indices = indices;
            this.parent = parent;
            if (parent == null) {
                notVisited = dataset.getAttributes();
                notVisited.remove(classAttribute);
            } else {
                notVisited = parent.notVisited;
            }
        }

        /**
         * adds edge
         * @param edge
         */
        public void addEdge(Edge edge) {
            this.edges.add(edge);
        }

        /**
         * get value if single node else null
         * @return
         */
        public Value getValue() {
            if (isSingleNode)
                return value;
            return null;
        }

        /**
         * prints tree recursive (still needs some work)
         * @param prefix
         */
        public void print(String prefix) {
            if (isSingleNode) {
                System.out.println(prefix + " " + value);
            } else {
                System.out.println(prefix + " " + attribute);
            }
            for(Edge edge : edges) {
                edge.end.print(prefix+'-');
            }
        }

        /**
         * sets attribute and removes from not visited
         * @param attribute
         */
        public void setAttribute(Attribute attribute) {
            this.attribute = attribute;
            this.notVisited.remove(attribute);
        }
    }

    /**
     * Edge class
     */
    private class Edge {
        Value value = null;
        Node start;
        Node end;

        /**
         * Constructor
         * @param value
         */
        public Edge(Value value, Node start) {
            this.value = value;
            this.start = start;
            this.start.addEdge(this);
        }
    }

    private Node root = null;

    private Instance[] data;
    private Dataset dataset;
    private Attribute classAttribute;

    /**
     * default constructor
     */
    public DecisionTree() {
    }

    //--------------------------------------------------------------------------
    //-------------------------Train tree---------------------------------------
    //--------------------------------------------------------------------------

    /**
     * chooses the attribute with the best information gain
     * @param node
     * @return
     */
    public Attribute selectAttribute(Node node) {
        Attribute select = null;
        double maxGain = Double.NEGATIVE_INFINITY;

        // looks at all relevant attributes
        for (Attribute attribute : node.notVisited) {

            // does not look at classAttribute
            /*if (attribute.equals(classAttribute)) {
                continue;
            }*/
            double gain = this.informationGain(attribute, node.indices);
            if (gain > maxGain) {
                select = attribute;
                maxGain = gain;
            }
        }
        return select;
    }

    /**
     *
     */
    public void train(List<Integer> trainset) {
        // create root Node
        this.root = new Node(trainset, null);
        this.train_recursive(this.root);
    }

    /**
     * Internal method
     * @param n
     */
    public void train_recursive(Node n) {

        // exit function
        if (this.isSingleNode(n)) {
            return;
        }
        //select attribute with biggest informationGain
        n.setAttribute(this.selectAttribute(n));

        // create edges for each value of the attribute
        NominalAttribute attr = (NominalAttribute)n.attribute;
        for (int i = 0; i < attr.getNumberOfValues(); i++) {
            Value v = attr.getValue(i);
            Edge edge = new Edge(v, n);
            edge.end = new Node(new ArrayList<>(), n);
        }



        for (Integer idx : n.indices) {
            Instance i = this.data[idx];
            Value v = i.getValue(n.attribute);

            // add index to right edge
            for (Edge edge : n.edges) {
                if (edge.value.equals(v)) {
                    edge.end.indices.add(idx);
                    break;
                }
            }
        }
        // create tree
        for (Edge e : n.edges) {
            this.train_recursive(e.end);
        }
    }

    /**
     * Tests if node has only one class left and sets node.isSingleNode
     * @param node
     * @return
     */
    private boolean isSingleNode(Node node) {

        // if node is empty we set his value to the most common
        // value of his parent
        if (node.indices.size() == 0) {
            node.value = mostCommonValue(node.parent, this.classAttribute);
            node.isSingleNode = true;
            return true;
        }

        // should not be possible anymore
        // if last node we set value to most common value
        /*if ( node.notVisited.size() == 1
                && node.notVisited.contains(classAttribute)) {
            node.value = mostCommonValue(node, this.classAttribute);
            node.isSingleNode = true;
            return true;
        }*/

        /*// should not be possible, else like the one over this*/
        // if last node we set value to most common value
        if ( node.notVisited.size() == 0 ) {
            node.value = mostCommonValue(node, this.classAttribute);
            node.isSingleNode = true;
            return true;
        }

        Value value = data[node.indices.get(0)].getValue(classAttribute);

        // if one instance has an other value as the rest return false
        for (int i = 1; i < node.indices.size(); i++) {
            Instance instance = data[node.indices.get(i)];
            if (! instance.getValue(classAttribute).equals(value)) {
                return false;
            }
        }

        // else set as single node and return true
        node.isSingleNode = true;
        node.value = value;
        return true;
    }


    //--------------------------------------------------------------------------
    //-------------------------classify tree------------------------------------
    //--------------------------------------------------------------------------


    /**
     * classifies given data set on decision Tree
     * @param data
     * @return
     */
    public double classify(List<Integer> data) {
        //TODO test if tree is build
        int correctlyClassified = 0;
        // repeat for each instance
        for (Integer i : data) {
            Instance instance = this.data[i];

            // iterate over tre
            Node currentNode = this.root;
            while (!currentNode.isSingleNode) {
                Attribute attr = currentNode.attribute;
                Value value = instance.getValue(attr);

                // find right edge
                for (Edge edge : currentNode.edges) {
                    if (edge.value.equals(value)) {
                        currentNode = edge.end;
                        break;
                    }
                }
            }

            // check if class attr is correct
            if ( currentNode.getValue().equals(instance.getValue(classAttribute)) ) {
                correctlyClassified ++;
            }

        }

        return (double)correctlyClassified/(double)data.size();
    }




    //--------------------------------------------------------------------------
    //-------------------------Constructor--------------------------------------
    //--------------------------------------------------------------------------

    /**
     * returns most common value from node in attribute
     * @param node
     * @param attribute
     * @return
     */
    private Value mostCommonValue(Node node, Attribute attribute) {
        // because attribute dose not know his values
        Map<Value, Integer> numValues = new HashMap<Value, Integer>();
        // temp
        Value max = null;
        int maxInt = -1;

        for (Integer i : node.indices) {
            Instance instance = data[i];
            Value value = instance.getValue(attribute);
            Integer integer = 1;
            if(numValues.containsKey(value)) {
                integer = numValues.get(value);
                integer++;
                numValues.replace(value, integer);
            } else {
                numValues.put(value, integer);
            }

            if (integer > maxInt) {
                max = value;
            }
        }

        return max;
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
        this.classAttribute = this.dataset.getAttributes().get(this.dataset
                .getClassIndex());
    }


    // Implement a method informationGain that takes two arguments: attribute A
    // and a list of indices i 1 , i 2 , i m .
    /**
     * calculates information gain for given attribute and instances
     * @param attribute given attribute
     * @param indices given indices of instances
     * @return information gain
     */
    public double informationGain(Attribute attribute, List<Integer> indices) {
        Attribute classAttr = classAttribute;

        //TODO throw Exception
        // Check if nominal
        if (!attribute.isNominal()) {
            System.err.println(attribute.getName() + "is not nominal");
            return Double.NaN;
        } else if (!classAttr.isNominal()) {
            System.err.println(classAttr.getName() + "is not nominal");
            return Double.NaN;
        }

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
            double entropy = 0.0;
            if (subIndices.size() != 0) {
                entropy = calculateEntropy((NominalAttribute)classAttr,
                        subIndices);
            }

            // see formula
            gain -= entropy * ((double)subIndices.size())
                    /((double)indices.size());
        }

        return gain;
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
            NominalValue classValue = classAttr.getValue(v);

            // counts number of value v in trainset
            for(int i : indices) {
                Instance instance = data[i];
                NominalValue value = (NominalValue)instance.getValue(classAttr);
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



    //--------------------------------------------------------------------------
    //-------------------------Train and Testset--------------------------------
    //--------------------------------------------------------------------------


    /**
     * @return 2/3 trainset
     */
    public List<Integer> getTrainset() {
        return getTrainset(2.0/3);
    }

    /**
     * splits data set in train set
     * @param split size og train set as percentage
     * @return trainset
     */
    public List<Integer> getTrainset(double split) {
        // get List with all indices
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < this.data.length; i++) {
            indices.add(i);
        }

        //TODO check split, throw Exception
        // get random indices
        int size = (int)Math.ceil(indices.size() * split);
        while (indices.size() >= size) {
            Random random = new Random();
            Integer randomIdx = random.nextInt(indices.size());
            indices.remove(randomIdx);
        }


        return indices;
    }


    /**
     * returns inverse data set
     * @param originalSet
     * @return
     */
    public List<Integer> getInverseSet(List<Integer> originalSet) {
        List<Integer> inverseSet = new ArrayList<>();
        for (int i=0; i<this.data.length; i++) {
            if (!originalSet.contains(i))
                inverseSet.add(i);
        }
        return inverseSet;
    }





    //--------------------------------------------------------------------------
    //-------------------------test---------------------------------------------
    //--------------------------------------------------------------------------



    /**
     * prints some test data
     */
    private void testPrint() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            indices.add(i);
        }

        /*for (Attribute attr : this.dataset.getAttributes()) {
            System.out.print("Attribute " + attr.getName());
            System.out.print(" has an InformationGain of " + informationGain
                    (attr, indices));
            System.out.println();
        }*/

        List<Integer> trainset = this.getTrainset();
        this.train(trainset);
        System.out.println(this.classify(this.getInverseSet(trainset)));
    }

    private void printTree() {
        System.out.println("Tree");
        root.print("");
    }

    /**
     * a test
     * @param args none
     */
    public static void main(String[] args) {
        DecisionTree dt = new DecisionTree("res/car.arff");
        dt.testPrint();
        dt.printTree();
    }
}
