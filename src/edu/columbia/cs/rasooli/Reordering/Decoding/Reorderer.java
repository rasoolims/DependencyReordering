package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;
import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/20/15
 * Time: 3:32 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class Reorderer {
    Classifier classifier;
    HashMap<String,Integer> posOrderFrequencyDic;

    public Reorderer(Classifier classifier, HashMap<String, Integer> posOrderFrequencyDic) {
        this.classifier = classifier;
        this.posOrderFrequencyDic = posOrderFrequencyDic;
    }

    public void reorder(DependencyTree tree, int topK){
        HashSet<Integer> heads=new HashSet<Integer>();
        for(int h=1;h<tree.size();h++)
            if(tree.hasDep(h))
                heads.add(h);
        
        ArrayList<ContextInstance> reorderingInstances=new ArrayList<ContextInstance>();
        
        for(int head:heads) {
            HashSet<Integer> deps = tree.getDependents(head);
            TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
            origOrderSet.add(head);
            for (int dep : deps)
                origOrderSet.add(dep);

            int[] origOrder = new int[1 + deps.size()];
            int i = 0;
            for (int dep : origOrderSet)
                origOrder[i++] = dep;

            ContextInstance origContext = new ContextInstance(head, origOrder, tree);

            float bestScore = Float.NEGATIVE_INFINITY;
            ContextInstance bestCandidate = null;

            for (ContextInstance candidate : origContext.getPossibleContexts(posOrderFrequencyDic,topK)) {
                ArrayList<String> features = candidate.extractMainFeatures();
                float score = classifier.score(features, true);
                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = candidate;
                }
            }
            reorderingInstances.add(bestCandidate);
        }
        
    }
}
