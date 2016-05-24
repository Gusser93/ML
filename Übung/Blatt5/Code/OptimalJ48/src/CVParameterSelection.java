import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import org.omg.CORBA.DynAnyPackage.InvalidValue;
import sun.jvm.hotspot.types.WrongTypeException;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by David on 24.05.16.
 */

// extend RandomizableSingleClassifierEnhancer to use the method setClassifier

public class CVParameterSelection {


    //--------------------------------------------------------------------------
    //-----------------------Internal CVParameter Class-------------------------
    //--------------------------------------------------------------------------

    // internal class that represents one parameter
    protected class CVParameter {

        protected String param;
        protected char paramChar;
        protected double lowerBound;
        protected double upperBound;
        protected double stepValue;
        // current value to test
        protected double value;

        /**
         *
         * @param param actual parameter with its values as string
         */
        public CVParameter(String param) throws Exception {
            this.param = param;

            // split parameter in its arguments
            // we need at least 4 and at most 5 values
            String[] args = this.param.split(" ");
            if (args.length == 4 || args.length == 5) {
                // save parameter character
                String paramString = args[0];
                if (paramString.length() != 1) {
                    throw new IllegalArgumentException("Invalid parameter char.");
                } else {
                    this.paramChar = paramString.charAt(0);
                }

                // save lower bound, upper bound and steps
                try {
                    this.lowerBound = Double.parseDouble(args[1]);
                    this.upperBound = Double.parseDouble(args[2]);
                    this.stepValue = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    throw new WrongTypeException("Invalid values for parameter " + paramString);
                }

                // check if values are valid
                if (this.lowerBound > this.upperBound) {
                    throw new InvalidValue("Lower bound must be lesser than or equal to upper bound");
                }

                // todo add -R

            } else {
                throw new WrongNumberArgsException("At least 4 values are required.");
            }
        }

        /**
         *
         * @return name of parameter
         */
        protected String getParameterString() {
            return this.param;
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
    private double bestPerformance = -1;
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
        this.params.add(new CVParameter(param));
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

    /**
     *
     * @param options
     * @throws Exception
     */
    public void setOptions(String[] options) throws Exception {
        // -X
        String numFoldStr = Utils.getOption('X', options);
        if (numFoldStr.length() > 0) {
            try {
                // try to convert the number of folds to an integer
                this.setNumberOfFolds(Integer.parseInt(numFoldStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value for argument X");
            }
        } else {
            // x was not set => use default Value
            this.setNumberOfFolds(DEFAULT_NUM_OF_FOLDS);
        }


        // -P
        // Input example ['-P', 'N 10 23 2' , 'U 5 2 1']
        String paramString = Utils.getOption('P', options);

        // as long as we find a parameter after '-P'
        // add a CVParameter
        while (paramString.length() != 0) {
            this.addCVParameter(paramString);
            paramString = Utils.getOption('P', options);
        }

        //Todo Implement Rest of options
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
            paramNames[i] = this.params.get(i).getParameterString();
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


    /**
     *
     * @return options which are set
     */
    public String[] getOptions() {
        //2 for x, 1 for P
        int size = 2+1+this.params.size();

        String[] options = new String[size];

        // add all options to the array
        int current = 0;
        options[current++] = "-P";
        for (int i = 0; i < this.params.size(); i++) {
            options[current++] = this.getCVParameter(i);
        }
        options[current++] = "-X";
        options[current++] = String.valueOf(this.getNumberOfFolds());

        return options;
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
        for(CVParameter param : this.params) { 
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
            if (this.bestPerformance < 0 || errorRate < this.bestPerformance) {
                this.bestPerformance = errorRate;
                this.bestOptions = options;
            }
        }
    }

    /**
     * Build classifier with bestOptions
     * @param instances
     * @throws Exception
     */
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
     * @param instance
     * @return
     * @throws Exception
     */
    public double[] distributionForInstance(Instance instance) throws Exception { 
        return this.getClassifier().distributionForInstance(instance); 
    }
}
