package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;
import edu.columbia.cs.rasooli.Reordering.Structures.FeaturedInstance;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/25/15
 * Time: 8:17 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class ScoringThread implements Callable<FeaturedInstance> {
    ContextInstance candidate;
    AveragedPerceptron classifier;
    boolean isDecode;

    public ScoringThread(ContextInstance candidate,AveragedPerceptron classifier , boolean isDecode) {
        this.candidate = candidate;
        this.classifier=classifier;
        this.isDecode=isDecode;
    }

    @Override
    public FeaturedInstance call() throws Exception {
        ArrayList<Object>[] features = candidate.extractMainFeatures();
        float score = classifier.score(features, isDecode);
        return new FeaturedInstance(candidate,features,score);
    }
}
