package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;
import edu.columbia.cs.rasooli.Reordering.Structures.IndexMaps;
import edu.columbia.cs.rasooli.Reordering.Structures.Pair;

import java.util.concurrent.Callable;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/27/15
 * Time: 12:44 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class BitextReorderingThread implements Callable<Pair<String,Integer>> {
    int treeNumber;
    BitextDependency bitextDependency;
    Reorderer reorderer;
    IndexMaps maps;

    public BitextReorderingThread(final int treeNumber, BitextDependency bitextDependency,final Reorderer reorderer, final  IndexMaps maps) {
        this.treeNumber = treeNumber;
        this.bitextDependency = bitextDependency;
        this.reorderer=reorderer;
        this.maps=maps;
    }

    @Override
    public Pair<String, Integer> call() throws Exception {
        return new Pair<String, Integer>(reorderer.reorderWithAlignmentGuide(bitextDependency).toConllOutput(maps),treeNumber);
    }
}
