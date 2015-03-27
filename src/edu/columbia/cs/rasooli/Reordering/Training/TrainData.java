package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;

import java.util.*;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/12/15
 * Time: 12:22 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class TrainData {
    int index;
    String goldLabel;
    ArrayList<Object>[] features;

    public TrainData(int index, String goldLabel, ArrayList<Object>[] features) {
        this.index = index;
        this.goldLabel = goldLabel;
        this.features = features;
    }

    public int getIndex() {
        return index;
    }

    public String getGoldLabel() {
        return goldLabel;
    }

    public ArrayList<Object>[] getFeatures() {
        return features;
    }
}
