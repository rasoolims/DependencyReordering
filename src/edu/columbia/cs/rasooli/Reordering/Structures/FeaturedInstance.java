package edu.columbia.cs.rasooli.Reordering.Structures;

import java.util.ArrayList;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/25/15
 * Time: 8:18 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class FeaturedInstance {
    ContextInstance instance;
    ArrayList<Object>[] features;
    double score;

    public FeaturedInstance(ContextInstance instance, ArrayList<Object>[] features, double score) {
        this.instance = instance;
        this.features = features;
        this.score = score;
    }

    public ContextInstance getInstance() {
        return instance;
    }

    public ArrayList<Object>[] getFeatures() {
        return features;
    }

    public double getScore() {
        return score;
    }
}
