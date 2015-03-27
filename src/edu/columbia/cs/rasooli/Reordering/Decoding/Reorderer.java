package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;
import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;
import edu.columbia.cs.rasooli.Reordering.Structures.FeaturedInstance;
import edu.columbia.cs.rasooli.Reordering.Structures.IndexMaps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
    AveragedPerceptron[] classifier;
    HashMap<String,int[]>[]  mostCommonPermutations;
    int topK;
    HashMap<String,String> universalMap;
    ExecutorService executor  ;
    CompletionService<FeaturedInstance> pool ;
    IndexMaps maps;
    int numOfThreads;
    
    
    public Reorderer(AveragedPerceptron[] classifier, HashMap<String,int[]>[]  mostCommonPermutations, HashMap<String,String> universalMap, int topK, int numOfThreads, IndexMaps maps) {
        this.classifier = classifier;
        this.mostCommonPermutations = mostCommonPermutations;
        this.universalMap=universalMap;
        this.topK=topK;
        this.numOfThreads=numOfThreads; 
        this.maps=maps;
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
            ArrayList<Object>[]  features=  origContext.extractMainFeatures();
            double bestScore = Double.NEGATIVE_INFINITY;
            int[] bestOrder=null;
            
            int index= deps.size()-1;
            if (index<mostCommonPermutations.length) {
                int l = 0;
                for (String label : mostCommonPermutations[index].keySet()) {
                    double score = classifier[index].score(l, features, true);
                    if (score > bestScore) {
                        bestScore = score;
                        bestOrder = mostCommonPermutations[index].get(label);
                    }
                    l++;
                }
            }

            
                int[] newOrder = new int[origOrder.length];
            if(bestOrder!=null)
                for (int o = 0; o < newOrder.length; o++)
                    newOrder[o] = origOrder[bestOrder[o]];
            else
                newOrder=origOrder;
            ContextInstance bestCandidate=new ContextInstance(head,newOrder,tree);

            reorderingInstances.add(bestCandidate);
        }
        
        DependencyTree currentTree=tree;
        for(ContextInstance instance:reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
        }
        
        return currentTree;
    }
    
    
    public void decode(String inputFile,String outputFile) throws  Exception {
        executor = Executors.newFixedThreadPool(numOfThreads);
        pool = new ExecutorCompletionService<FeaturedInstance>(executor);
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

         long start=System.currentTimeMillis();

        DependencyTree tree;
        int count=0;
        while ((tree = DependencyReader.readNextDependencyTree(reader, universalMap, maps)) != null) {
            writer.write(reorder(tree).toConllOutput());
            count++;
            if(count%100==0)
                System.err.print(count + "...");
        }
        System.out.print(count+"\n");
        long end=System.currentTimeMillis();
        float elapsed=(float)(end-start)/count;
        System.err.println("time for decoding "+elapsed + " ms per sentence");
        writer.flush();
        writer.close();
        executor.shutdown();
    }
}
