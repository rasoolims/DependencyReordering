package edu.columbia.cs.rasooli.Reordering.Classifier;

import java.util.ArrayList;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/17/15
 * Time: 2:54 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public interface Classifier {
    public void incrementIteration() ;
    public int getIteration();
    public double[] scores(ArrayList<Object>[] features, boolean decode);

    public int argmax(ArrayList<Object>[] features, boolean decode);
    public double[] scores(ArrayList<Object>[] features);
    public void updateWeight(int label, int slot, Object feature, double change) throws Exception;
    public int featLen();
    
}