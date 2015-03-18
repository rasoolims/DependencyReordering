package edu.columbia.cs.rasooli.Reordering.Structures;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/17/15
 * Time: 11:02 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class PosOrderFrequency implements Comparable<PosOrderFrequency>{
    int[] order;
    int frequency;

    public PosOrderFrequency(int[] order, int frequency) {
        this.order = order;
        this.frequency = frequency;
    }

    //region compareTo
    @Override
    public int compareTo(PosOrderFrequency posOrderFrequency) {
        float diff = frequency - posOrderFrequency.frequency;
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
}