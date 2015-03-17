package edu.columbia.cs.rasooli.Reordering.Classifier;

import java.util.ArrayList;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/17/15
 * Time: 2:54 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public abstract class Classifier {
    protected  int iteration;
    public Classifier(){
        iteration=1;
    }
    public abstract void updateWeight(String feature, double change);
    public abstract  double score(ArrayList<String> features,boolean decode);
    public abstract void saveModel(String modelPath)  throws  Exception;
    public void incrementIteration() {
        iteration++;
    }
    public abstract int size();
    public abstract Classifier loadModel(String modelPath) throws  Exception;
}