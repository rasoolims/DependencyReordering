package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;
import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.*;

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
    int topK;
    HashMap<String,String> universalMap;

    public Reorderer(Classifier classifier, HashMap<String, Integer> posOrderFrequencyDic, HashMap<String,String> universalMap, int topK) {
        this.classifier = classifier;
        this.posOrderFrequencyDic = posOrderFrequencyDic;
        this.universalMap=universalMap;
        this.topK=topK;
    }

    public DependencyTree reorder(DependencyTree tree) throws Exception {
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
        
        DependencyTree currentTree=tree;
        for(ContextInstance instance:reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
        }
        
        return currentTree;
    }
    
    public void decode(String inputFile,String outputFile) throws  Exception {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        DependencyTree tree;
        int count=0;
        while ((tree = DependencyReader.readNextBitextDependency(reader, universalMap)) != null) {
            writer.write(reorder(tree).toConllOutput());
            count++;
            if(count%100==0)
                System.out.print(count + "...");
        }
        System.out.print(count+"\n");
        writer.flush();
        writer.close();
    }
}
