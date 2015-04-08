package edu.columbia.cs.rasooli.Reordering.Training;

import java.util.ArrayList;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 4/6/15
 * Time: 4:15 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class PivotTrainData {
    boolean isBefore;
    ArrayList<Object>[] features;

    public PivotTrainData(boolean isBefore, ArrayList<Object>[] features) {
        this.isBefore = isBefore;
        this.features = features;
    }

    public boolean getGoldLabel() {
        return isBefore;
    }

    public ArrayList<Object>[] getFeatures() {
        return features;
    }
}
