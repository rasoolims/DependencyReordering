package edu.columbia.cs.rasooli.Reordering.Classifier;

import edu.columbia.cs.rasooli.Reordering.Enums.ClassifierType;
import edu.columbia.cs.rasooli.Reordering.Structures.CompactArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/18/15
 * Time: 11:15 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class AveragedPerceptron  implements Serializable,Classifier {
    HashMap<Object, CompactArray>[] weights;
    HashMap<Object, CompactArray>[] avgWeights;
    int featSize;
    int labelSize;
    int iteration;

    public AveragedPerceptron(int labelSize, int featSize) {
        this.iteration=1;
        this.featSize = featSize;
        this.labelSize = labelSize;
        weights = new HashMap[featSize];
        avgWeights = new HashMap[featSize];
        for (int j = 0; j < featSize; j++) {
            weights[j] = new HashMap<Object, CompactArray>();
            avgWeights[j] = new HashMap<Object, CompactArray>();
        }
    }

    public void updateWeight(int label, int slot, Object feature, double change) throws Exception {
        CompactArray values = weights[slot].get(feature);
        CompactArray aValues;
        if (values == null) {
            double[] val = new double[]{change};
            values = new CompactArray(label, val);
            weights[slot].put(feature, values);

            double[] aval = new double[]{change * iteration};
            aValues = new CompactArray(label, aval);
            avgWeights[slot].put(feature, aValues);
        } else {
            values.expandArray(label, change);
            (avgWeights[slot].get(feature)).expandArray(label, iteration * change);
        }
    }

    @Override
    public ClassifierType getType() {
        return ClassifierType.perceptron;
    }

    @Override
    public int featLen() {
        return featSize;
    }

    @Override
    public double[] scores(ArrayList<Object>[] features, boolean decode) {
        double scores[] = new double[labelSize];
        HashMap<Object, CompactArray>[] map = decode ? avgWeights : weights;
        for (int i = 0; i < featSize; i++) {
            if (features[i] == null)
                continue;
            for (Object feat : features[i]) {
                CompactArray values = map[i].get(feat);
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
    public double[] scores(ArrayList<Object>[] features) {
        double scores[] = new double[labelSize];
        for (int i = 0; i < featSize; i++) {
            if (features[i] == null)
                continue;
            for (Object feat : features[i]) {
                CompactArray values = weights[i].get(feat);
                CompactArray aValues = avgWeights[i].get(feat);
                if (values != null) {
                    int offset = values.getOffset();
                    double[] weightVector = values.getArray();
                    double[] aWeightVector = aValues.getArray();

                    for (int d = offset; d < offset + weightVector.length; d++) {
                        scores[d] += weightVector[d - offset] - (aWeightVector[d - offset] / iteration);
                    }
                }
            }
        }
        return scores;
    }

    public HashMap<Object, CompactArray>[] getWeights() {
        return weights;
    }

    public HashMap<Object, CompactArray>[] getAvgWeights() {
        return avgWeights;
    }

    public void setAvgWeights(HashMap<Object, CompactArray>[] avgWeights) {
        this.avgWeights = avgWeights;
    }

    @Override
    public void incrementIteration() {
         iteration+=1;
    }

    @Override
    public int getIteration() {
        return iteration;
    }
}