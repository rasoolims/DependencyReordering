package edu.columbia.cs.rasooli.Reordering.Structures;

import java.util.HashSet;
import java.util.SortedSet;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/11/15
 * Time: 11:49 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class BitextDependency {
    SortedSet<Integer>[] alignedWords;

    DependencyTree sourceTree;
    HashSet<Integer> trainableHeads;


    public BitextDependency(SortedSet<Integer>[] alignedWords, DependencyTree sourceTree) {
        this.alignedWords = alignedWords;
        this.sourceTree = sourceTree;
        trainableHeads = this.setTrainableHeads();
    }

    /**
     * For more information see Section 2.2 of
     * Lerner and Petrov, "Source-side Classifier Preordering for Maching Translation", EMNLP 2013.
     *
     * @return head for trainable contexts
     */
    private HashSet<Integer> setTrainableHeads() {
        HashSet<Integer> heads = new HashSet<Integer>();

        int initHead = 0;
        fillInTrainableHeads(heads, initHead);
        return heads;
    }

    // todo --should debug this part (this is really sensitive)
    private void fillInTrainableHeads(HashSet<Integer> heads, int initHead) {
        HashSet<Integer> deps = sourceTree.getDependents(initHead);
        if (deps.size() == 0)
            return;

        boolean canMake = true;

        if (alignedWords[initHead].size() == 0)
            canMake = false;

        // condition 1
        for (int d : deps) {
            if (alignedWords[d].size() == 0)
                canMake = false;
            fillInTrainableHeads(heads, d);
        }

        // condition 2
        if (canMake) {
            for (int d1 : deps) {
                for (int d2 : deps) {
                    if (d1 == d2)
                        continue;
                    HashSet<Integer> f1 = new HashSet<Integer>(alignedWords[d1]);
                    HashSet<Integer> f2 = new HashSet<Integer>(alignedWords[d2]);
                    f1.retainAll(f2);
                    if (f1.size() > 0) {
                        canMake = false;
                        break;
                    }
                }
                if (!canMake)
                    break;
            }
        }

        // condition 3
        if (canMake) {
            for (int d1 : deps) {
                if (alignedWords[d1].size() > 1) {
                    int first = alignedWords[d1].first();
                    int last = alignedWords[d1].last();

                    for (int d2 : deps) {
                        if (d1 == d2)
                            continue;

                        for (int a : alignedWords[d2]) {
                            if (a >= first && a <= last) {
                                canMake = false;
                                break;
                            }
                        }
                    }
                    if (!canMake)
                        break;
                }

                if (!canMake)
                    break;
            }
        }

        if (canMake && initHead != 0 && deps.size() > 0)
            heads.add(initHead);
    }

    public HashSet<Integer> getTrainableHeads() {
        return trainableHeads;
    }

    public SortedSet<Integer>[] getAlignedWords() {
        return alignedWords;
    }

    public DependencyTree getSourceTree() {
        return sourceTree;
    }


}
