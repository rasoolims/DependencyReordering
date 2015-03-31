package edu.columbia.cs.rasooli.Reordering.Classifier;

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
public class AveragedPerceptron extends Classifier implements Serializable {
    HashMap<Object, CompactArray>[] weights;
    HashMap<Object, CompactArray>[] avgWeights;
    int featSize;
    int labelSize;

    public AveragedPerceptron(int labelSize, int featSize) {
        super();
        this.featSize = featSize;
        this.labelSize = labelSize;
        weights = new HashMap[featSize];
        avgWeights = new HashMap[featSize];
        for (int j = 0; j < featSize; j++) {
            weights[j] = new HashMap<Object, CompactArray>();
            avgWeights[j] = new HashMap<Object, CompactArray>();
        }
    }

    public void updateWeight(int label, int slot, Object feature, float change) {
        CompactArray values = weights[slot].get(feature);
        CompactArray aValues;
        if (values == null) {
            float[] val = new float[]{change};
            values = new CompactArray(label, val);
            weights[slot].put(feature, values);

            float[] aval = new float[]{change * iteration};
            aValues = new CompactArray(label, aval);
            avgWeights[slot].put(feature, aValues);
        } else {
            values.expandArray(label, change);
            (avgWeights[slot].get(feature)).expandArray(label, iteration * change);
        }
    }

    public float[] scores(ArrayList<Object>[] features, boolean decode) {
        float scores[] = new float[labelSize];
        HashMap<Object, CompactArray>[] map = decode ? avgWeights : weights;
        for (int i = 0; i < featSize; i++) {
            if (features[i] == null)
                continue;
            for (Object feat : features[i]) {
                CompactArray values = map[i].get(feat);
                if (values != null) {
                    int offset = values.getOffset();
                    float[] weightVector = values.getArray();

                    for (int d = offset; d < offset + weightVector.length; d++) {
                        scores[d] += weightVector[d - offset];
                    }
                }
            }
        }
        return scores;
    }

    public float[] decScores(ArrayList<Object>[] features) {
        float scores[] = new float[labelSize];
        for (int i = 0; i < featSize; i++) {
            if (features[i] == null)
                continue;
            for (Object feat : features[i]) {
                CompactArray values = weights[i].get(feat);
                CompactArray aValues = avgWeights[i].get(feat);
                if (values != null) {
                    int offset = values.getOffset();
                    float[] weightVector = values.getArray();
                    float[] aWeightVector = aValues.getArray();

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
}