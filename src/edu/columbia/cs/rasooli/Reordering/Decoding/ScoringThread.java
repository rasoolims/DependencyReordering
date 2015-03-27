package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Structures.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/25/15
 * Time: 8:17 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class ScoringThread implements Callable<Pair<Integer,Double>> {
    ArrayList<Object>[] features;
    AveragedPerceptron classifier;
    boolean isDecode;
    int labelIndex;

    public ScoringThread(int labelIndex,  ArrayList<Object>[] features,AveragedPerceptron classifier , boolean isDecode) {
        this.features = features;
        this.classifier=classifier;
        this.isDecode=isDecode;
        this.labelIndex=labelIndex;
    }

    @Override
    public Pair<Integer,Double> call() throws Exception {
        double score = classifier.score(labelIndex,features, isDecode);
        return new Pair<Integer, Double>(labelIndex,score);
    }
}
