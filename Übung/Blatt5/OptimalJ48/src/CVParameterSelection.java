import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import org.omg.CORBA.DynAnyPackage.InvalidValue;
import sun.jvm.hotspot.types.WrongTypeException;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by David on 24.05.16.
 */

public class CVParameterSelection extends Classifier {


    //--------------------------------------------------------------------------
    //-----------------------Internal CVParameter Class-------------------------
    //--------------------------------------------------------------------------

    // internal class that represents one parameter
    protected class CVParameter {

        protected char paramChar;
        protected double lowerBound;
        protected double upperBound;
        protected double stepValue;
        // current value to test
        protected double value;


        /**
         *
         * @param paramChar
         * @param lowerBound
         * @param upperBound
         * @param steps
         * @throws Exception
         */
        public CVParameter(char paramChar, double lowerBound, double upperBound, double steps) throws Exception {
            this.paramChar = paramChar;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.stepValue = steps;

            // check if values are valid
            if (this.lowerBound > this.upperBound) {
                throw new InvalidValue("Lower bound must be lesser than or equal to upper bound");
            }
        }

        /**
         * Input string given by the user
         * @return name of parameter
         */
        protected String getParameterString() {
            String p = String.valueOf(this.paramChar);
            String u = String.valueOf(this.upperBound);
            String l = String.valueOf(this.lowerBound);
            String s = String.valueOf(this.stepValue);

            return (p + " " + l + " " + u + " " + s);
        }
    }



    //--------------------------------------------------------------------------
    //-------------------------Attributes---------------------------------------
    //--------------------------------------------------------------------------


    private final int DEFAULT_NUM_OF_FOLDS = 10;

    // available options
    protected String[] classifierOptions = null;
    // parameters which are set
    private ArrayList<CVParameter> params = new ArrayList<>();
    // use 10 number of folds as default
    private int numFolds = DEFAULT_NUM_OF_FOLDS;
    // classifier for selection
    private Classifier classifier;
    // error rate of best performance
    private double lowestError = -1;
    // best options for this classifier
    private String[] bestOptions = null;
    // initial classifierOptions
    private String[] initClassifierOptions = null;


    //--------------------------------------------------------------------------
    //-------------------------Setter-------------------------------------------
    //--------------------------------------------------------------------------

    /**
     * @param classifier
     */
    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
        this.initClassifierOptions = this.getClassifier().getOptions();
    }


    /**
     * add a single parameter
     * @param param name of the parameter
     * @return true on success otherwise false
     */
    public void addCVParameter(String param) throws Exception {
        // we expect a string in format: 'N lower_bound upper_bound steps'

        // split parameter in its arguments
        // we need exactly 4 values
        char paramChar;
        double lowerBound, upperBound, steps;
        String[] args = param.split(" ");

        // check input
        if (args.length == 4) {
            // get parameter character
            String paramString = args[0];
            if (paramString.length() != 1) {
                throw new IllegalArgumentException("Invalid parameter char.");
            } else {
                paramChar = paramString.charAt(0);
            }

            // save lower bound, upper bound and steps
            try {
                lowerBound = Double.parseDouble(args[1]);
                upperBound = Double.parseDouble(args[2]);
                steps = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                throw new WrongTypeException("Invalid values for parameter " + paramString);
            }

            // add CVParameter
            CVParameter tmp = new CVParameter(paramChar, lowerBound, upperBound, steps);
            this.params.add(tmp);


        } else {
            throw new WrongNumberArgsException("At least 4 values are required.");
        }
    }


    /**
     *
     * @param params parameters to add
     */
    public void setCVParameters(String[] params) throws Exception {
        for (String param : params) {
           this.addCVParameter(param);
        }
    }

    /**
     * @param numFolds number of folds for crossvalidation
     */
    public void setNumberOfFolds(int numFolds) {
        if (numFolds < 0) {
            throw new IllegalArgumentException("Number of folds must be positiv");
        } else {
            this.numFolds = numFolds;
        }
    }



    //--------------------------------------------------------------------------
    //------------------------------Getter--------------------------------------
    //--------------------------------------------------------------------------


    /**
     *
     * @return classifier
     */
    public Classifier getClassifier() {
        return this.classifier;
    }

    /**
     *
     * @param index of parameter
     * @return name of parameter
     */
    public String getCVParameter(int index) throws IndexOutOfBoundsException{
        if (index > 0 && index < this.params.size()) {
            return this.params.get(index).getParameterString();
        } else {
            throw new IndexOutOfBoundsException("Parameter index out of bounds");
        }
    }

    /**
     *
     * @return names of all parameters
     */
    public String[] getCVParameters() {
        String[] paramNames = new String[this.params.size()];
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = this.getCVParameter(i);
        }
        return paramNames;
    }

    /**
     *
     * @return number of folds
     */
    public int getNumberOfFolds() {
        return this.numFolds;
    }



    //--------------------------------------------------------------------------
    //----------------------------classifier Operations-------------------------
    //--------------------------------------------------------------------------


    /**
     * Combine cross validation options of this class with the classifier options
     */
    private String[] getCombinedOptions() throws  Exception {
        // remove all options from our classifier which are already set by this class
        // wekas Util.getOption method does this for us
        // Note: this changes the classifierOptions array
        String[] classifierOptions = this.classifier.getOptions();
        for (CVParameter param : this.params) {
            Utils.getOption(param.paramChar, classifierOptions);
        }


        // create new array
        int leng = classifierOptions.length;
        String[] options = new String[leng + 2*this.params.size()];

        // add classifier options
        System.arraycopy(classifierOptions, 0, options, 0, leng);

        // add crossvalidation options of this class
        for (int i = leng; i<leng+this.params.size()*2; i++) {
            CVParameter param = this.params.get(i);
            options[i] = String.valueOf(param.paramChar);
            options[i+1] = String.valueOf(param.value);
        }

        return options;
    }

    /**
     *
     * @param depth
     * @param trainData
     * @throws Exception
     */
    private void calculateBestClassifierOptions(int depth, Instances trainData) throws Exception {
        if (depth < this.params.size()) {
            // repeat for each parameter
            CVParameter param = this.params.get(depth);

            // calculate our increment value
            double u, l, s;
            u = param.upperBound;
            l = param.lowerBound;
            s = param.stepValue;

            // Todo is this increment right
            double inc = (u-l)/s;
            for (param.value = l; param.value <= u; param.value+=inc) {
                // calculate param.value => best options
                calculateBestClassifierOptions(depth+1, trainData);
            }

        } else {
            Evaluation eval = new Evaluation(trainData);

            // get combined options
            String[] options = this.getCombinedOptions();

            // set the option for our classifier
            this.classifier.setOptions(options);

            // for each fold
            int nFolds = this.getNumberOfFolds();
            for (int i = 0; i<nFolds; i++) {
                // get a trainset and a testset
                // randomize the data the same way each time
                // i-te fold
                Instances train = trainData.trainCV(nFolds, i, new Random(1));
                // is not important to randomize the same way with our test set
                Instances test = trainData.testCV(nFolds, i);
                // build our classifier
                this.classifier.buildClassifier(train);
                //eval.setPriors(train); //warum?
                // evaluate our model for each fold
                eval.evaluateModel(this.classifier, test);
            }

            // get our error rate and save the options if the error rate is
            // better than the last one
            double errorRate = eval.errorRate();
            if (this.lowestError == -1 || errorRate < this.lowestError) {
                this.lowestError = errorRate;
                this.bestOptions = options;
            }
        }
    }




    //--------------------------------------------------------------------------
    //--------------------------------Overrides---------------------------------
    //--------------------------------------------------------------------------



    /**
     * Build classifier with bestOptions
     * @param instances
     * @throws Exception
     */
    @Override
    public void buildClassifier(Instances instances) throws Exception {
        Instances trainData = new Instances(instances);

        // shuffle our trainData
        trainData.randomize(new Random());

        // if the user has not set any options then just build the classifier
        if (this.params.isEmpty()) {
            this.classifier.buildClassifier(trainData);
            // set default params as best params
            this.bestOptions = this.initClassifierOptions;
        } else {
            // calculate bestOptions
            this.calculateBestClassifierOptions(0, trainData);

            // set bestOptions for our classifier and build it
            this.classifier.setOptions(this.bestOptions);
            this.buildClassifier(trainData);
        }

    }


    /**
     *
     * @return
     */
    @Override
    public Capabilities getCapabilities() {
        Capabilities cap = super.getCapabilities();
        cap.setMinimumNumberInstances(this.getNumberOfFolds());
        return cap;
    }


    /**
     *
     * @param instance
     * @return
     * @throws Exception
     */
    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        return this.getClassifier().distributionForInstance(instance);
    }
}
