package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Structures.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/27/15
 * Time: 1:09 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class DevScoringThread implements Callable<Pair<Integer,Double>> {
    ArrayList<Object>[] features;
    AveragedPerceptron classifier;
    int labelIndex;

    public DevScoringThread(int labelIndex,  ArrayList<Object>[] features,AveragedPerceptron classifier ) {
        this.features = features;
        this.classifier=classifier;
        this.labelIndex=labelIndex;
    }

    @Override
    public Pair<Integer,Double> call() throws Exception {
        double score = classifier.devScore(labelIndex,features);
        return new Pair<Integer, Double>(labelIndex,score);
    }
}