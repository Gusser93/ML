import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.DoubleStream;

/**
 * Created by markus on 24.05.16.
 */
public class Test {
    public static void main(String... args) throws Exception {
        Instances data = ConverterUtils.DataSource.read("res/car.arff");
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        testPruning(data, 10, 0.01f, 0.5f, 100, 1, "Pruningx1");
        testPruning(data, 10, 0.01f, 0.5f, 100, 1, "Pruningx2");
        testPruning(data, 10, 0.01f, 0.5f, 100, 1, "Pruningx3");
        testPruning(data, 10, 0.01f, 0.5f, 100, 1, "Pruningx4");

        testPruning(data, 10, 0.01f, 0.5f, 100, 10, "Pruningx10");
        testPruning(data, 10, 0.01f, 0.5f, 100, 10, "Pruningx20");
        testPruning(data, 10, 0.01f, 0.5f, 100, 10, "Pruningx30");
        testPruning(data, 10, 0.01f, 0.5f, 100, 10, "Pruningx40");

        testPruning(data, 10, 0.01f, 0.5f, 100, 100, "Pruningx100");
        testPruning(data, 10, 0.01f, 0.5f, 100, 100, "Pruningx200");
        testPruning(data, 10, 0.01f, 0.5f, 100, 100, "Pruningx300");
        testPruning(data, 10, 0.01f, 0.5f, 100, 100, "Pruningx400");

        testPruning(data, 10, 0.01f, 0.5f, 100, 1000, "Pruningx1000");

        /*for (int i = 0; i <20; i++) {
            OptimalJ48 j48 = new OptimalJ48();
            j48.buildClassifier(data);
            System.out.println(Arrays.toString(j48.selection.getBestClassifierOptions()));
        }*/
    }

    public static void testPruning(Instances data, int numFolds, float
            lowerBorder, float upperBorder, int steps, int repeats,
            String title) throws Exception {
        double[][] accuracy = new double[steps][repeats];
        double[] x = new double[steps];
        float dx = (upperBorder - lowerBorder)/(steps - 1);

        for (int j = 0; j < repeats; j++) {
            for (int i = 0; i < steps; i++) {
                float c = lowerBorder + i * dx;
                J48 classifier = new J48();
                classifier.setConfidenceFactor(c);
                Evaluation eval = new Evaluation(data);
                eval.crossValidateModel(classifier, data, numFolds,
                        new Random());
                accuracy[i][j] = eval.pctCorrect() / 100.0;
                x[i] = c;
            }
        }

        double[] result = new double[steps];
        for (int i = 0; i < steps; i++) {
            double tmp = DoubleStream.of(accuracy[i]).parallel().sum();
            result[i] = tmp / repeats;
        }

        plotWithPython(title, "pruning confidence", "accuracy", x,
                result);

    }

    public static void plotWithPython(String title, String xLabel, String
            yLabel, double[] x, double[] y) throws IOException {
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        StringBuilder xList = new StringBuilder("[");
        StringBuilder yList = new StringBuilder("[");

        for(int i = 0; i < x.length; i++) {
            xList.append(nf.format(x[i])).append(",");
            yList.append(nf.format(y[i])).append(",");
        }
        xList.deleteCharAt(xList.length()-1).append("]");
        yList.deleteCharAt(yList.length()-1).append("]");

        List<String> options = new ArrayList<>();
        options.add("python3");
        options.add("plot.py");
        options.add(getArgument("xList", xList.toString()));
        options.add(getArgument("yList", yList.toString()));
        options.add(getStringArgument("title", title));
        options.add(getStringArgument("xLabel", xLabel));
        options.add(getStringArgument("yLabel", yLabel));

        ProcessBuilder pb = new ProcessBuilder(options);
        Process p = pb.start();

        Scanner err =new Scanner(p.getErrorStream());
        Scanner in = new Scanner(p.getInputStream());
        while(err.hasNextLine()) {
            System.err.println(err.nextLine());
        }
        while(in.hasNextLine()) {
            System.out.println(in.nextLine());
        }
    }

    private static String getArgument(String flag, String arg) {
        StringBuilder options = new StringBuilder("");
        options.append("--").append(flag).append("=").append(arg).append(" ");
        return options.toString();
    }

    private static String getStringArgument(String flag, String arg) {
        StringBuilder options = new StringBuilder("");
        options.append("--").append(flag).append("='").append(arg).append
                ("' ");
        return options.toString();
    }
}
