package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;
import edu.columbia.cs.rasooli.Reordering.Structures.Pair;

import java.util.concurrent.Callable;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/27/15
 * Time: 12:39 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class TreeReorderingThread implements Callable<Pair<String,Integer>> {
    int treeNumber;
    DependencyTree origTree;
    Reorderer reorderer;

    public TreeReorderingThread(final int treeNumber, final DependencyTree origTree,final Reorderer reorderer) {
        this.treeNumber = treeNumber;
        this.origTree = origTree;
        this.reorderer=reorderer;
    }

    @Override
    public Pair<String, Integer> call() throws Exception {
        return new Pair<String, Integer>(reorderer.reorder(origTree).toConllOutput(),treeNumber);
    }
}
