package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;
import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;
import edu.columbia.cs.rasooli.Reordering.Structures.FeaturedInstance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/20/15
 * Time: 3:32 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class Reorderer {
    AveragedPerceptron classifier;
    HashMap<String,Integer> posOrderFrequencyDic;
    int topK;
    HashMap<String,String> universalMap;
    ExecutorService executor  ;
    CompletionService<FeaturedInstance> pool ;

    public Reorderer(AveragedPerceptron classifier, HashMap<String, Integer> posOrderFrequencyDic, HashMap<String,String> universalMap, int topK, int numOfThreads) {
        this.classifier = classifier;
        this.posOrderFrequencyDic = posOrderFrequencyDic;
        this.universalMap=universalMap;
        this.topK=topK;
       executor = Executors.newFixedThreadPool(numOfThreads);
        pool = new ExecutorCompletionService<FeaturedInstance>(executor);
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

            double bestScore = Double.NEGATIVE_INFINITY;
            ContextInstance bestCandidate = null;


            HashSet<ContextInstance> candidates= origContext.getPossibleContexts(posOrderFrequencyDic, topK);
            
            int s=0;
            for (ContextInstance candidate : candidates) {
                pool.submit(new ScoringThread(candidate,classifier,false));
                s++;
            }

            for(int x=0;x<s;x++){
                FeaturedInstance featuredInstance=pool.take().get();
                if(featuredInstance.getScore()>bestScore){
                    bestScore = featuredInstance.getScore();
                    bestCandidate = featuredInstance.getInstance();
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
