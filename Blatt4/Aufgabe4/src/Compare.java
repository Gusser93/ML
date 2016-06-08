import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by Markus on 19.05.2016.
 */
public class Compare {

    private static class Bootstrap {

        private Instances train;
        private Instances test;

        public Bootstrap(Instances train, Instances test) {
            this.test = test;
            this.train = train;
        }
    }

    public static void test(String path, int repeats) throws Exception {
        Instances data = ConverterUtils.DataSource.read(path);

        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() -1);
        }

        int[][] n = new int[0][0];
        double result = 0;

        for (int i = 0; i < repeats; i++) {
            Bootstrap bootstrap = getBootstrap(data, data.numInstances());

            RandomForest rf = new RandomForest();
            J48 decisionTree = new J48();

            rf.buildClassifier(bootstrap.train);
            decisionTree.buildClassifier(bootstrap.train);

            Instances labeledRf = classifyData(bootstrap.test, rf);
            Instances labeledDt = classifyData(bootstrap.test, decisionTree);

            n = mcNemar(bootstrap.test, labeledRf, labeledDt);
            result += mcNemaStatistic(n);
        }

        System.out.println("File " + path);
        System.out.println("0 = a = RandomForest");
        System.out.println("1 = b = J48 as DecisionTree");
        System.out.println("last McNema Table");
        for (int i = 0; i < n.length; i++) {
            System.out.println(Arrays.toString(n[i]));
        }
        System.out.println("avg. McNema Stat. over " + repeats);
        System.out.println(result/repeats);
        System.out.println();
    }

    public static Instances classifyData(Instances data, Classifier
            classifier) throws Exception {
        Instances labeled = new Instances(data);

        for (int i = 0; i < data.numInstances(); i++) {
            double clsLabel = classifier.classifyInstance(data.instance(i));
            labeled.instance(i).setClassValue(clsLabel);
        }

        return labeled;
    }

    public static Bootstrap getBootstrap(Instances data, int size) {
        List<Integer> trainIdx = getBootstrapList(data.numInstances(), size);
        List<Integer> testIdx = getTestsetList(data.numInstances(), trainIdx);

        Instances train = getSubInstances(data, trainIdx);
        Instances test = getSubInstances(data, testIdx);

        return new Bootstrap(train, test);
    }

    public static Instances getSubInstances(Instances data, List<Integer>
            indices) {
        Instances sub = new Instances(data, 0);

        for (Integer i : indices) {
            sub.add(data.instance(i));
        }

        return sub;
    }



    public static List<Integer> getBootstrapList(int dataSize, int size) {
        Random random = new Random();

        List<Integer> bootstrapList = new ArrayList<>();

        for (int i = 0; i < size ; i++) {
            bootstrapList.add(random.nextInt(dataSize));
        }

        return bootstrapList;
    }

    public static List<Integer> getTestsetList(int dataSize, List<Integer>
            train) {
        Set<Integer> trainSet = new HashSet<>(train);
        Set<Integer> dataSet = new HashSet<>();
        IntStream.range(0,dataSize).forEach(val -> dataSet.add(val));
        dataSet.removeAll(trainSet);

        return new ArrayList<>(dataSet);
    }

    public static int[][] mcNemar(Instances test, Instances labeledA,
                                  Instances labeledB)  {
        if (!(test.equalHeaders(labeledA) && test.equalHeaders(labeledB)
                && test.numInstances() == labeledA.numInstances()
                && test.numInstances() == labeledB.numInstances())) {
            return null; //TODO should do something
        }
        int[][] n = new int[2][2];

        for (int i = 0; i < test.numInstances(); i++) {
            Instance testInst = test.instance(i);
            Instance aInst = labeledA.instance(i);
            Instance bInst = labeledB.instance(i);

            boolean aCorrect = testInst.classValue() == aInst.classValue();
            boolean bCorrect = testInst.classValue() == bInst.classValue();

            if (aCorrect && bCorrect) {
                n[1][1]++;
            } else if (aCorrect) {
                n[1][0]++;
            } else if (bCorrect) {
                n[0][1]++;
            } else {
                n[0][0]++;
            }
        }

        return n;
    }

    public static double mcNemaStatistic(int[][] n) {
        double result =
                Math.pow( Math.abs(n[0][1] - n[1][0]) - 1, 2)
                / (double)(n[0][1] + n[1][0]);
        return result;
    }

    public static void main(String[] args) throws Exception {
        String prefix = "res/";
        String[] paths = {"letter.arff", "kr-vs-kp.arff", "splice" +
                ".arff"};
        for (String path : paths) {
            test(prefix + path, 10);
        }
    }
}
