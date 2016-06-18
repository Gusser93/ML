package evaluation;

import classifier.Classifier;
import dataset.Instances;

import java.util.Random;

/**
 * Created by markus on 17.06.16.
 */
public class Evaluation {

    private Instances data;
    private double acc = Double.NaN;

    public Evaluation(Instances data) {
        this.data = data;
    }

    public void holdoutEvaluation(int folds, double split,
                                  Class<? extends Classifier> classifierClass) {
        double result = 0.0;
        for (int i = 0; i < folds; i++) {
            result += holdoutEvaluationAcc(split,new Random(),
                    classifierClass) /
                    folds;
        }
        this.acc = result;
    }

    public void holdoutEvaluationDEBUG(double split,
                                  Class<? extends Classifier> classifierClass) {
        this.acc = holdoutEvaluationAcc(split,new Random(1),
                classifierClass);
    }

    private double holdoutEvaluationAcc(double split, Random random, Class<?
            extends Classifier> classifierClass) {

        Instances train = new Instances(this.data, split, random);
        Instances test = new Instances(this.data, train);

        try {
            Classifier classifier = classifierClass.newInstance();
            classifier.buildClassifier(train);
            int count = 0;
            int n = train.numInstances();

            for (int i = 0; i < n; i++) {
                String label = classifier.classifyInstance(test.getInstance
                        (i));
                String should = test.getInstance(i).classValueString();
                //System.out.println("Predicted: " + label + " should " +
                        //should);
                if (should.equals(label)) {
                    count++;
                }
            }
            return (double)count / n;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Double.NaN;

    }

    public double accuracy() {
        return this.acc;
    }


}
