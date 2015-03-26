package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;

import java.util.*;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/12/15
 * Time: 12:22 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class TrainData {
    ContextInstance goldInstance;
    ContextInstance originalInstance;
    ArrayList<Object>[] goldFeatures;

    public TrainData(ContextInstance originalInstance, ContextInstance goldInstance) throws Exception {
        this.originalInstance = originalInstance;
        this.goldInstance = goldInstance;
    }

    public ContextInstance getGoldInstance() {
        return goldInstance;
    }

    public ContextInstance getOriginalInstance() {
        return originalInstance;
    }
}
