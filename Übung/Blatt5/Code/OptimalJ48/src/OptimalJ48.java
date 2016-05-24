import weka.core.*;

import java.util.Random;

/**
 * Created by David on 24.05.16.
 */
public class OptimalJ48 {
    Instances data = null;

    /**
     * @param pieces number of pieces to split the data
     * @return Array containing number of pieces Instances
     */
    public Instances[] splitDataset(int pieces) {
        // size of dataset
        int size = data.numInstances();
        int piece_size = size/pieces;

        // randomize the data
        this.data.randomize(new Random());

        // save trainings, validation and test set
        Instances[] datasets = new Instances[pieces];

        // split the data
        for (int i = 0; i<pieces; i++) {
            int lowerBound =  (int)(Math.ceil(piece_size*i));
            int upperBound =  (int)(Math.ceil(piece_size*(i+1)));
            datasets[i] = new Instances(this.data, lowerBound, upperBound);
        }

        return datasets;
    }
}
