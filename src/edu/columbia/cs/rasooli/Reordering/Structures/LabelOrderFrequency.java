package edu.columbia.cs.rasooli.Reordering.Structures;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/26/15
 * Time: 12:44 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class LabelOrderFrequency implements Comparable<LabelOrderFrequency>{
    int[] order;
    int frequency;
    String label;
    
    public LabelOrderFrequency(String label, int[] order, int frequency) {
        this.order = order;
        this.label=label;
        this.frequency = frequency;
    }

    //region compareTo
    @Override
    public int compareTo(LabelOrderFrequency labelOrderFrequency) {
        float diff = frequency - labelOrderFrequency.frequency;
        if (diff > 0)
            return 2;
        if (diff < 0)
            return -2;
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
    //endregion


    public int[] getOrder() {
        return order;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getLabel() {
        return label;
    }
}