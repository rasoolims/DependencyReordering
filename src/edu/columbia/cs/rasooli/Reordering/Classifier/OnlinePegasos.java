package edu.columbia.cs.rasooli.Reordering.Classifier;

import edu.columbia.cs.rasooli.Reordering.Enums.ClassifierType;
import edu.columbia.cs.rasooli.Reordering.Structures.CompactArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 4/1/15
 * Time: 1:32 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class OnlinePegasos implements Classifier {
    /**
     * I tried to implement the online Pegasos algorithm desribed in  *
     * Wang, Zhuang, Koby Crammer, and Slobodan Vucetic. "Multi-class pegasos on a budget." In Proceedings of the 27th International Conference on Machine Learning (ICML-10), pp. 1143-1150. 2010.* 
     */
    
    double lambda ;
    HashMap<Object, CompactArray>[] weights;
    int featSize;
    int labelSize;
    int iteration;

    public OnlinePegasos(int labelSize, int featSize, double lambda) {
        this.iteration=1;
        this.lambda=lambda;
        this.featSize = featSize;
        this.labelSize = labelSize;
        weights = new HashMap[featSize];
        for (int j = 0; j < featSize; j++) {
            weights[j] = new HashMap<Object, CompactArray>();
        }
    }

    public OnlinePegasos(int labelSize, int featSize) {
       this(labelSize,featSize,0.0001f) ;
    }

    
   public void updateWeights(int goldLabel, int predictedLabel, ArrayList<Object>[] features) throws Exception {
       double eta=1.f/(lambda*iteration);
       for(int i=0;i<features.length;i++) {
           for (Object feat : features[i]) {
               boolean hasKey = true;
               CompactArray currArray = weights[i].get(feat);
               if (currArray == null) {
                   hasKey = false;
               }

               double[] currentWeights;
               if (hasKey)
                   currentWeights = currArray.getArray();
               else
                   currentWeights = new double[labelSize];

               try {
                   for (int l = 0; l < labelSize; l++) {
                       if (l == goldLabel) {
                           currentWeights[l] -= (eta * (lambda * currentWeights[l] - 1));
                       } else if (l == predictedLabel) {
                           currentWeights[l] -= (eta * (lambda * currentWeights[l] + 1));
                       } else {
                           currentWeights[l] -= eta * lambda * currentWeights[l];
                       }
                   }
               }catch (Exception ex){
                   System.out.print("HERE!");
               }

               weights[i].put(feat, new CompactArray(0, currentWeights));
           }
       }
   }

    @Override
    public double[] scores(ArrayList<Object>[] features) {
        double scores[] = new double[labelSize];
        for (int i = 0; i < featSize; i++) {
            if (features[i] == null)
                continue;
            for (Object feat : features[i]) {
                CompactArray values = weights[i].get(feat);
                if (values != null) {
                    int offset = values.getOffset();
                    double[] weightVector = values.getArray();

                    for (int d = offset; d < offset + weightVector.length; d++) {
                        scores[d] += weightVector[d - offset];
                    }
                }
            }
        }
        return scores;
    }

    @Override
    public void updateWeight(int label, int slot, Object feature, double change) {

    }

    public HashMap<Object, CompactArray>[] getWeights() {
        return weights;
    }

    public void setWeights(HashMap<Object,  CompactArray>[] weights) {
        this.weights = weights;
    }

    @Override
    public void incrementIteration() {
        iteration+=1;
    }

    @Override
    public int getIteration() {
        return iteration;
    }

    @Override
    public double[] scores(ArrayList<Object>[] features, boolean decode) {
        return scores(features);
    }

    @Override
    public ClassifierType getType() {
        return ClassifierType.pegasos;
    }

    @Override
    public int featLen() {
        return featSize;
    }
}
