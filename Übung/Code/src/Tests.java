import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.util.List;

/**
 * Created by markus on 10.05.16.
 */
public class Tests {

    /*
    private static double evalWeka(Classifier classifier, Instances
            testset) throws Exception {
        double cor = 0;

        for (int i = 0; i < testset.numInstances(); i++) {
            double clsLabel = classifier.classifyInstance(testset.instance
                    (i));
            if (clsLabel == testset.instance(i).classValue()) {
                cor++;
            }
        }

        return cor/testset.numInstances();
    }*/

    /*
    private static Instances classifyWeka(Classifier classifier, Instances
            testset) throws Exception {
        Instances labeled = new Instances(testset);

        for (int i = 0; i < testset.numInstances(); i++) {
            double clsLabel = classifier.classifyInstance(testset.instance
                    (i));
            labeled.instance(i).setClassValue(clsLabel);
        }

        return labeled;
    }

    private static double evalWeka(Instances testset, Instances labeled) {
        double cor = 0;

        for (int i = 0; i < testset.numInstances(); i++) {
            if (testset.instance(i).classValue() == labeled.instance(i)
                    .classValue()) {
                cor++;
            }
        }
        return cor / testset.numInstances();
    }*/


    private static Instances getSplitSet(Instances orig, List<Integer>
            integers) {

        Instances trainset = new Instances(orig, 0);
        for (Integer i : integers) {
            trainset.add(orig.instance(i));
        }
        return trainset;
    }

    private static double evalWeka(Classifier classifier, Instances
            testset, Instances trainset) throws Exception {
        Evaluation eval = new Evaluation(trainset);
        eval.evaluateModel(classifier, testset);

        double cor = eval.correct();

        return cor/testset.numInstances();
    }

    private static RandomForest trainRandomForest (Instances trainset,
                                                   int numberOfTrees)
            throws Exception {

        RandomForest randomForest = new RandomForest();
        randomForest.setNumTrees(numberOfTrees);
        randomForest.buildClassifier(trainset);

        return randomForest;

    }

    private static void dtVsRf(int n, File data, Instances instances)
            throws Exception {

        double result = 0;
        double rfs = 0;
        double dts = 0;

        for (int i = 0; i < n; i++) {
            DecisionTree decisionTree = new DecisionTree(data);
            List<Integer> trainsetIdx = decisionTree.getTrainset();
            List<Integer> testsetIdx = decisionTree.getInverseSet(trainsetIdx);

            Instances trainset = getSplitSet(instances, trainsetIdx);
            Instances testset = getSplitSet(instances, testsetIdx);

            RandomForest randomForest = trainRandomForest(trainset, 100);

            decisionTree.train(trainsetIdx);

            double dt = decisionTree.classify(testsetIdx);
            double rf = evalWeka(randomForest, testset, trainset);
            double diff = rf - dt;


            //System.out.println("DT has " + dt + " and RF has " + rf);
            if (dt > rf) {
                //System.out.println("DT was better. Diff :" + Math.abs(diff));
            } else {
                //System.out.println("RF was better. Diff :" + Math.abs(diff));
            }
            //System.out.println();
            result += diff;
            rfs += rf;
            dts += dt;
        }

        System.out.println("\n\nAvarage in " + n + " cases\n");
        if (0 > result) {
            System.out.println("DT was better.\nDiff : " + Math.abs(result / n)
                    + "\nFactor : " + dts/rfs);
        } else {
            System.out.println("RF was better.\nDiff : " + Math.abs(result / n)
                    + "\nFactor : " + rfs/dts);
        }
    }

    private static void testRandomForest(int n, File data, Instances
            instances) throws Exception {

        DecisionTree dT = new DecisionTree(data);
        List<Integer> trainsetIndex = dT.getTrainset();
        List<Integer> testsetIndex = dT.getInverseSet(trainsetIndex);

        Instances trainS = getSplitSet(instances, trainsetIndex);
        Instances testS = getSplitSet(instances, testsetIndex);

        for(int i = 0; i< n; i++) {

            RandomForest randomForest = trainRandomForest(trainS, i);

            double performanceWekaRandomForest
                    = evalWeka(randomForest, testS, trainS)/i;
            System.out.println("For " + i + " Trees is the performance: "
                    + performanceWekaRandomForest);
        }
    }

    public static void main(String... args) throws Exception {
System.out.println("MUH");
        File data = new File("res/car.arff");
        ArffLoader source = new ArffLoader();
        source.setFile(data);
        Instances instances =  source.getDataSet();

        if (instances.classIndex() == -1) {
            instances.setClassIndex(instances.numAttributes() - 1);
        }

        testRandomForest(100, data, instances);

        System.out.println();

        dtVsRf(100, data, instances);
    }
}