package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.IO.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.*;

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
                   // for(int b=0;b<mostCommonPermutations[index].get(label).length;b++)
                   //     System.out.print(mostCommonPermutations[index].get(label)[b]+" ");
                   // System.out.print("\n");
                    l++;
                }
            }

            
                int[] newOrder = new int[origOrder.length];
            if(bestOrder!=null)
                for (int o = 0; o < newOrder.length; o++)
                    newOrder[o] = origOrder[bestOrder[o]];
            else
                newOrder=origOrder;
            
           // for(int b=0;b<bestOrder.length;b++)
             //   System.out.print(bestOrder[b]+" ");
            //System.out.print("\n---b----\n");
            ContextInstance bestCandidate=new ContextInstance(head,newOrder,tree);

            reorderingInstances.add(bestCandidate);
        }
        
        DependencyTree currentTree=tree;
        for(ContextInstance instance:reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
        }
        
        return currentTree;
    }

    public DependencyTree reorderWithAlignmentGuide(BitextDependency bitextDependency) throws Exception {
     DependencyTree tree=bitextDependency.getSourceTree();
        
        HashSet<Integer> heads=new HashSet<Integer>();
        for(int h=1;h<tree.size();h++)
            if(tree.hasDep(h))
                heads.add(h);

        ArrayList<ContextInstance> reorderingInstances=new ArrayList<ContextInstance>();

        for(int head:heads) {
            if(bitextDependency.getTrainableHeads().contains(head)){
                HashSet<Integer> deps = bitextDependency.getSourceTree().getDependents(head);
                TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
                origOrderSet.add(head);
                for (int dep : deps)
                    origOrderSet.add(dep);
                
                int index = 0;
                int[] ordering = new int[origOrderSet.size()];
                HashMap<Integer, Integer> revOrdering = new HashMap<Integer, Integer>();
                revOrdering.put(head, index);
                ordering[index++] = head;
                for (int d : deps) {
                    revOrdering.put(d, index);
                    ordering[index++] = d;
                }
                try {
                    TreeMap<Integer, Integer> changedOrder = new TreeMap<Integer, Integer>();

                    SortedSet<Integer>[] alignedSet = bitextDependency.getAlignedWords();
                    changedOrder.put(alignedSet[head].first(), head);
                    for (int dep : origOrderSet)
                        changedOrder.put(alignedSet[dep].first(), dep);

                    int[] goldOrder = new int[1 + deps.size()];
                    int i = 0;
                    for (int dep : changedOrder.keySet()) {
                        goldOrder[i++] = revOrdering.get(changedOrder.get(dep));
                    }

                    int[] newOrder = new int[ordering.length];
                    if (goldOrder != null)
                        for (int o = 0; o < newOrder.length; o++)
                            newOrder[o] = ordering[goldOrder[o]];
                    else
                        newOrder = ordering;

                    
                    
                    ContextInstance bestCandidate = new ContextInstance(head, newOrder, tree);
                    reorderingInstances.add(bestCandidate);
                }catch (Exception ex){
                    System.out.print("err!...");
                }
            }   else {
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
                ArrayList<Object>[] features = origContext.extractMainFeatures();
                double bestScore = Double.NEGATIVE_INFINITY;
                int[] bestOrder = null;

                int index = deps.size() - 1;
                if (index < mostCommonPermutations.length) {
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
                if (bestOrder != null)
                    for (int o = 0; o < newOrder.length; o++)
                        newOrder[o] = origOrder[bestOrder[o]];
                else
                    newOrder = origOrder;
                ContextInstance bestCandidate = new ContextInstance(head, newOrder, tree);

                reorderingInstances.add(bestCandidate);
            }
        }

        DependencyTree currentTree=tree;
        for(ContextInstance instance:reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
        }

        return currentTree;
    }

    public void decode(String inputFile,String outputFile) throws  Exception {
        ExecutorService executor =Executors.newFixedThreadPool(numOfThreads);
        CompletionService<Pair<String,Integer>> pool =new ExecutorCompletionService<Pair<String,Integer>>(executor);

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

         long start=System.currentTimeMillis();

        DependencyTree tree;
        int count=0;
        int c=0;
        String[] output=new String[1000];
        while ((tree = DependencyReader.readNextDependencyTree(reader, universalMap, maps)) != null) {
            pool.submit(new TreeReorderingThread(c, tree, this));
            count++;
            c++;

            if(c==1000){
                for(int i=0;i<c;i++){
                    Pair<String,Integer> outputPair=pool.take().get();
                    output[outputPair.second]=outputPair.first;
                }
                for(int i=0;i<c;i++){
                    writer.write(output[i]);
                }
                output=new String[1000];
                c=0;
            }
            if(count%1000==0) 
                System.err.print(count + "...");
        }
        if(c>0){
            for(int i=0;i<c;i++){
                Pair<String,Integer> outputPair=pool.take().get();
                output[outputPair.second]=outputPair.first;
            }
            for(int i=0;i<c;i++){
                writer.write(output[i]);
            }
            c=0;
        }
        System.err.print(count+"\n");
        long end=System.currentTimeMillis();
        float elapsed=(float)(end-start)/count;
        System.err.println("time for decoding "+elapsed + " ms per sentence");
        writer.flush();
        writer.close();
        executor.shutdown();
    }

    public void decodeWithAlignmentGuide(String inputFile,String intersectionFile, String outputFile) throws  Exception {
        ExecutorService executor =Executors.newFixedThreadPool(numOfThreads);
        CompletionService<Pair<String,Integer>> pool =new ExecutorCompletionService<Pair<String,Integer>>(executor);

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedReader inersectionReader = new BufferedReader(new FileReader(intersectionFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        long start=System.currentTimeMillis();

        BitextDependency bitextDependency;
        
        int count=0;
        String[] output=new String[1000];
        int c=0;
        while ((bitextDependency = BitextDependencyReader.readNextBitextDependency(reader, inersectionReader, universalMap, maps)) != null) {
          pool.submit(new BitextReorderingThread(c,bitextDependency,this));
            count++;
            c++;

            if(c==1000){
                for(int i=0;i<c;i++){
                    Pair<String,Integer> outputPair=pool.take().get();
                    output[outputPair.second]=outputPair.first;
                }
                for(int i=0;i<c;i++){
                    writer.write(output[i]);
                }
                c=0;
                if(count%1000==0)
                    System.err.print(count + "...");
            }
        }
        if(c>0){
            for(int i=0;i<c;i++){
                Pair<String,Integer> outputPair=pool.take().get();
                output[outputPair.second]=outputPair.first;
            }
            for(int i=0;i<c;i++){
                writer.write(output[i]);
            }
            output=new String[1000];
            c=0;
        }
        System.err.print(count+"\n");
        long end=System.currentTimeMillis();
        float elapsed=(float)(end-start)/count;
        System.err.println("time for decoding "+elapsed + " ms per sentence");
        writer.flush();
        writer.close();
        executor.shutdown();
    }

}
