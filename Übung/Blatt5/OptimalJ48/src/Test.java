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

        Instances car = load("res/car.arff");
        Instances autos = load("res/autos.arff");
        Instances vehicle = load("res/vehicle.arff");


        testPruning(car, 10, 0.01f, 0.5f, 10, 10, "Pruning car x10");
        testPruning(autos, 10, 0.01f, 0.5f, 10, 10, "Pruning autos x10");
        testPruning(vehicle, 10, 0.01f, 0.5f, 10, 10, "Pruning vehicle x10");

        testCVPS(car, 10, 10, "car");
        testCVPS(autos, 10, 10, "autos");
        testCVPS(vehicle, 10, 10, "vehicle");

        String[] carOpt = getPrune(car);
        String[] autosOpt = getPrune(autos);
        String[] vehicleOpt = getPrune(vehicle);

        System.out.println("car");
        System.out.println(Arrays.toString(carOpt));
        System.out.println();
        System.out.println("autos");
        System.out.println(Arrays.toString(autosOpt));
        System.out.println();
        System.out.println("vehicle");
        System.out.println(Arrays.toString(vehicleOpt));
        System.out.println();

        /*for (int i = 0; i <20; i++) {
            OptimalJ48 j48 = new OptimalJ48();
            j48.buildClassifier(data);
            System.out.println(Arrays.toString(j48.selection.getBestClassifierOptions()));
        }*/
    }

    public static Instances load(String path) throws Exception {
        Instances data = ConverterUtils.DataSource.read(path);
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        return data;
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

    public static void testCVPS(Instances data, int folds, int repeats,
                                String name) throws Exception {
        double acc = 0.0;
        double prune = 0.0;
        for (int i = 0; i < repeats; i++) {
            OptimalJ48 oj48 = new OptimalJ48();
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(oj48, data, folds, new Random());
            acc += eval.pctCorrect() / 100.0 / repeats;
        }

        System.out.println("Name: " + name);
        System.out.println("Accuracy: " + acc);
        System.out.println();
    }

    public static String[] getPrune(Instances data) throws Exception {
        OptimalJ48 oj48 = new OptimalJ48();
        oj48.buildClassifier(data);
        return oj48.selection.getBestClassifierOptions();
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
