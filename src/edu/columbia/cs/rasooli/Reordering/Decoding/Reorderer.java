package edu.columbia.cs.rasooli.Reordering.Decoding;

import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
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
    Classifier[] classifier;
    Classifier[] leftClassifier;
    Classifier[] rightClassifier;
    Classifier pivotClassifier;
    HashMap<String,int[]>[]  mostCommonPermutations;
    HashMap<String, int[]>[] leftMostCommonPermutations;
    HashMap<String, int[]>[] rightMostCommonPermutations;
    int topK;
    HashMap<String,String> universalMap;
    boolean twoClassifier = true;
    int maxLen = 5;
    
    IndexMaps maps;
    int numOfThreads;
    
    
    public Reorderer(Classifier[] classifier, HashMap<String,int[]>[]  mostCommonPermutations, HashMap<String,String> universalMap, int topK, int numOfThreads, IndexMaps maps) {
        twoClassifier = false;
        maxLen = classifier.length + 1;
        this.classifier = classifier;
        this.mostCommonPermutations = mostCommonPermutations;
        this.universalMap=universalMap;
        this.topK=topK;
        this.numOfThreads=numOfThreads; 
        this.maps=maps;
    }

    public Reorderer(Classifier[] leftClassifier, Classifier[] rightClassifier, Classifier pivotClassifier, HashMap<String, int[]>[] leftMostCommonPermutations, HashMap<String, int[]>[] rightMostCommonPermutations, HashMap<String, String> universalMap, int topK, int numOfThreads, IndexMaps maps) throws Exception {
        twoClassifier = true;
        maxLen = leftClassifier.length + 1;
        this.leftClassifier = leftClassifier;
        this.rightClassifier = rightClassifier;
        this.leftMostCommonPermutations = leftMostCommonPermutations;
        this.rightMostCommonPermutations = rightMostCommonPermutations;

        for (int i = 0; i < leftMostCommonPermutations.length; i++) {
            if (leftMostCommonPermutations[i].size() == 0) {
                StringBuilder str = new StringBuilder();
                int[] ord = new int[i + 2];
                for (int j = 0; j < i + 2; j++) {
                    str.append(j + "-");
                    ord[j] = j;
                }
                leftMostCommonPermutations[i].put(str.toString(), ord);
            }
        }

        for (int i = 0; i < rightMostCommonPermutations.length; i++) {
            if (rightMostCommonPermutations[i].size() == 0) {
                StringBuilder str = new StringBuilder();
                int[] ord = new int[i + 2];
                for (int j = 0; j < i + 2; j++) {
                    str.append(j + "-");
                    ord[j] = j;
                }
                rightMostCommonPermutations[i].put(str.toString(), ord);
            }
        }

        this.pivotClassifier = pivotClassifier;
        this.universalMap = universalMap;
        this.topK = topK;
        this.numOfThreads = numOfThreads;
        this.maps = maps;
    }

    public DependencyTree reorder(DependencyTree tree) throws Exception {
        if (twoClassifier)
            return reorderWithTwoClassifier(tree);
        else
            return reorderWithOneClassifier(tree);
    }

    public DependencyTree reorderWithAlignmentGuide(BitextDependency bitextDependency) throws Exception {
        if (twoClassifier)
            return reorderWithAlignmentGuideWithTwoClassifier(bitextDependency);
        else
            return reorderWithAlignmentGuideWithOneClassifier(bitextDependency);
    }

    public DependencyTree reorderWithOneClassifier(DependencyTree tree) throws Exception {
        HashSet<Integer> heads=new HashSet<Integer>();
        for(int h=1;h<tree.size();h++)
            if(tree.hasDep(h))
                heads.add(h);
        
        ArrayList<ContextInstance> reorderingInstances=new ArrayList<ContextInstance>();
        
        for(int head:heads) {
            HashSet<Integer> dx = tree.getDependents(head);
            HashSet<Integer> deps = new HashSet<Integer>(dx);
            deps.add(head);

            TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
            // origOrderSet.add(head);
            for (int dep : deps)
                origOrderSet.add(dep);

            int[] origOrder = new int[1 + dx.size()];
            int i = 0;
            for (int dep : origOrderSet)
                origOrder[i++] = dep;

            ContextInstance origContext = new ContextInstance(head, origOrder, tree);
            ArrayList<Object>[] features = origContext.extractMainFeatures();
            double bestScore = Double.NEGATIVE_INFINITY;
            int[] bestOrder = null;

            int index = dx.size() - 1;
            if (index < mostCommonPermutations.length) {
                int l = 0;
                double[] scores = classifier[index].scores(features, true);

                for (String label : mostCommonPermutations[index].keySet()) {

                    if (scores[l] > bestScore) {
                        bestScore = scores[l];
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

            // for(int b=0;b<bestOrder.length;b++)
            //   System.out.print(bestOrder[b]+" ");
            //System.out.print("\n---b----\n");
            ContextInstance bestCandidate = new ContextInstance(head, newOrder, tree);

            reorderingInstances.add(bestCandidate);
        }
        
        DependencyTree currentTree=tree;
        for(ContextInstance instance:reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
        }
        
        return currentTree;
    }

    public DependencyTree reorderWithAlignmentGuideWithOneClassifier(BitextDependency bitextDependency) throws Exception {
     DependencyTree tree=bitextDependency.getSourceTree();
        
        HashSet<Integer> heads=new HashSet<Integer>();
        for(int h=1;h<tree.size();h++)
            if(tree.hasDep(h))
                heads.add(h);

        ArrayList<ContextInstance> reorderingInstances=new ArrayList<ContextInstance>();

        for(int head:heads) {
            if(bitextDependency.getTrainableHeads().contains(head)){
                HashSet<Integer> dx = bitextDependency.getSourceTree().getDependents(head);
                HashSet<Integer> deps = new HashSet<Integer>(dx);
                deps.add(head);
                TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
                // origOrderSet.add(head);
                for (int dep : deps)
                    origOrderSet.add(dep);
                
                int index = 0;
                int[] ordering = new int[origOrderSet.size()];
                HashMap<Integer, Integer> revOrdering = new HashMap<Integer, Integer>();
                revOrdering.put(head, index);
                // ordering[index++] = head;
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

                    int[] goldOrder = new int[1 + dx.size()];
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
                HashSet<Integer> dx = tree.getDependents(head);
                HashSet<Integer> deps = new HashSet<Integer>(dx);
                deps.add(head);

                TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
                //  origOrderSet.add(head);
                for (int dep : deps)
                    origOrderSet.add(dep);

                int[] origOrder = new int[1 + dx.size()];
                int i = 0;
                for (int dep : origOrderSet)
                    origOrder[i++] = dep;

                ContextInstance origContext = new ContextInstance(head, origOrder, tree);
                ArrayList<Object>[] features = origContext.extractMainFeatures();
                double bestScore = Double.NEGATIVE_INFINITY;
                int[] bestOrder = null;

                int index = dx.size() - 1;
                if (index < mostCommonPermutations.length) {
                    int l = 0;
                    double[] scores = classifier[index].scores(features, true);
                    for (String label : mostCommonPermutations[index].keySet()) {
                        if (scores[l] > bestScore) {
                            bestScore = scores[l];
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

        DependencyTree currentTree = tree;
        for (ContextInstance instance : reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
        }

        return currentTree;
    }

    public DependencyTree reorderWithTwoClassifier(DependencyTree tree) throws Exception {
        HashSet<Integer> heads = new HashSet<Integer>();
        for (int h = 1; h < tree.size(); h++)
            if (tree.hasDep(h))
                heads.add(h);

        ArrayList<ContextInstance> reorderingInstances = new ArrayList<ContextInstance>();

        for (int head : heads) {
            HashSet<Integer> deps = tree.getDependents(head);

            ArrayList<Integer> left = new ArrayList<Integer>();
            ArrayList<Integer> right = new ArrayList<Integer>();

            for (int d : deps) {
                ArrayList<Object>[] features = tree.extractPivotFeatures(head, d);
                double score = pivotClassifier.scores(features, true)[0];
                if (score >= 0)
                    left.add(d);
                else
                    right.add(d);
            }

            int[] leftChildren = new int[left.size()];
            int[] rightChildren = new int[right.size()];

            int i = 0;
            for (int d : left)
                leftChildren[i++] = d;
            i = 0;
            for (int d : right)
                rightChildren[i++] = d;

            int[] newOrder = new int[deps.size() + 1];
            i = 0;
            if (leftChildren.length > 1 && leftChildren.length <= maxLen) {
                ArrayList<Object>[] features = tree.extractMainFeatures(head, leftChildren);
                int l = 0;
                double bestScore = Double.NEGATIVE_INFINITY;
                String bestLabel = "";
                int index = leftChildren.length - 2;
                double[] score = leftClassifier[index].scores(features, true);
                for (String label : leftMostCommonPermutations[index].keySet()) {
                    if (score[l] >= bestScore) {
                        bestScore = score[l];
                        bestLabel = label;
                    }
                    l++;
                }

                int[] order = leftMostCommonPermutations[index].get(bestLabel);
                for (int j = 0; j < order.length; j++)
                    newOrder[j] = leftChildren[order[j]];
            } else {
                for (int j = 0; j < leftChildren.length; j++)
                    newOrder[j] = leftChildren[j];
            }
            newOrder[leftChildren.length] = head;

            if (rightChildren.length > 1 && rightChildren.length <= maxLen) {
                ArrayList<Object>[] features = tree.extractMainFeatures(head, rightChildren);
                int l = 0;
                double bestScore = Double.NEGATIVE_INFINITY;
                String bestLabel = "";
                int index = rightChildren.length - 2;
                if (index >= rightClassifier.length)
                    System.out.print("ERROR!");
                double[] score = rightClassifier[index].scores(features, true);
                for (String label : rightMostCommonPermutations[index].keySet()) {
                    if (score[l] >= bestScore) {
                        bestScore = score[l];
                        bestLabel = label;
                    }
                    l++;
                }
                int[] order = rightMostCommonPermutations[index].get(bestLabel);

                for (int j = 0; j < order.length; j++)
                    newOrder[j + leftChildren.length + 1] = rightChildren[order[j]];
            } else {

                for (int j = 0; j < rightChildren.length; j++)
                    newOrder[j + leftChildren.length + 1] = rightChildren[j];
            }

            ContextInstance bestCandidate = new ContextInstance(head, newOrder, tree);

            reorderingInstances.add(bestCandidate);
        }

        DependencyTree currentTree = tree;
        for (ContextInstance instance : reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
        }

        return currentTree;
    }

    public DependencyTree reorderWithAlignmentGuideWithTwoClassifier(BitextDependency bitextDependency) throws Exception {
        DependencyTree tree = bitextDependency.getSourceTree();

        StringBuilder builder=new StringBuilder();
        
        builder.append("original tree\n");
        builder.append(bitextDependency.getSourceTree().toConllOutput(maps)+"\n");
        builder.append("\nalignment\n");
        builder.append(bitextDependency.getAlignedWordsStrings());
        
        HashSet<Integer> heads = new HashSet<Integer>();
        for (int h = 1; h < tree.size(); h++)
            if (tree.hasDep(h))
                heads.add(h);

        ArrayList<ContextInstance> reorderingInstances = new ArrayList<ContextInstance>();
   
        builder.append("\nprocessing heads\n");
        for (int head : heads) {
            builder.append("head:"+head+"\n");
            if (bitextDependency.getTrainableHeads().contains(head)) {
                builder.append("head:"+head+"trainable\n");

                HashSet<Integer> dx = bitextDependency.getSourceTree().getDependents(head);
                HashSet<Integer> deps = new HashSet<Integer>(dx);
                deps.add(head);
                TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
                // origOrderSet.add(head);
                for (int dep : deps)
                    origOrderSet.add(dep);

                int index = 0;
                int[] ordering = new int[origOrderSet.size()];
                HashMap<Integer, Integer> revOrdering = new HashMap<Integer, Integer>();
                revOrdering.put(head, index);
                // ordering[index++] = head;
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

                    int[] goldOrder = new int[1 + dx.size()];
                    int i = 0;
                    for (int dep : changedOrder.keySet()) {
                        goldOrder[i++] = revOrdering.get(changedOrder.get(dep));
                    }

                    builder.append("orders:\n") ;
                    int[] newOrder = new int[ordering.length];
                    if (goldOrder != null)
                        for (int o = 0; o < newOrder.length; o++) {
                            newOrder[o] = ordering[goldOrder[o]];
                            builder.append(newOrder[o]+" ");
                        }
                    else
                        newOrder = ordering;
                    
                    builder.append("\n");

                    ContextInstance bestCandidate = new ContextInstance(head, newOrder, tree);
                    reorderingInstances.add(bestCandidate);
                } catch (Exception ex) {
                    System.out.print("err!...");
                }
            } else {
                HashSet<Integer> deps = tree.getDependents(head);
                ArrayList<Integer> left = new ArrayList<Integer>();
                ArrayList<Integer> right = new ArrayList<Integer>();

                for (int d : deps) {
                    ArrayList<Object>[] features = tree.extractPivotFeatures(head, d);
                    double score = pivotClassifier.scores(features, true)[0];
                    if (score >= 0)
                        left.add(d);
                    else
                        right.add(d);
                }

                int[] leftChildren = new int[left.size()];
                int[] rightChildren = new int[right.size()];

                int i = 0;
                for (int d : left)
                    leftChildren[i++] = d;
                i = 0;
                for (int d : right)
                    rightChildren[i++] = d;

                int[] newOrder = new int[deps.size() + 1];
                i = 0;
                builder.append("left: ");
                if (leftChildren.length > 1 && leftChildren.length <= maxLen) {
                    ArrayList<Object>[] features = tree.extractMainFeatures(head, leftChildren);
                    int l = 0;
                    double bestScore = Double.NEGATIVE_INFINITY;
                    String bestLabel = "";
                    int index = leftChildren.length - 2;
                    double[] score = leftClassifier[index].scores(features, true);
                    for (String label : leftMostCommonPermutations[index].keySet()) {
                        if (score[l] >= bestScore) {
                            bestScore = score[l];
                            bestLabel = label;
                        }
                        l++;
                    }

                    int[] order = leftMostCommonPermutations[index].get(bestLabel);
                    for (int j = 0; j < order.length; j++) {
                        newOrder[j] = leftChildren[order[j]];
                        builder.append(newOrder[j]+" ");
                    }
                } else {
                    for (int j = 0; j < leftChildren.length; j++) {
                        newOrder[j] = leftChildren[j];
                        builder.append(newOrder[j]+" ");
                    }
                }
                builder.append("middle: ");
                builder.append( head+" ");
                newOrder[leftChildren.length] = head;
            
                builder.append("right: ");
                if (rightChildren.length > 1 && rightChildren.length <= maxLen) {
                    ArrayList<Object>[] features = tree.extractMainFeatures(head, rightChildren);
                    int l = 0;
                    double bestScore = Double.NEGATIVE_INFINITY;
                    String bestLabel = "";
                    int index = rightChildren.length - 2;
                    double[] score = rightClassifier[index].scores(features, true);
                    for (String label : rightMostCommonPermutations[index].keySet()) {
                        if (score[l] >= bestScore) {
                            bestScore = score[l];
                            bestLabel = label;
                        }
                        l++;
                    }

                    int[] order = rightMostCommonPermutations[index].get(bestLabel);
                    for (int j = 0; j < order.length; j++) {
                        newOrder[j + leftChildren.length + 1] = rightChildren[order[j]];
                        builder.append(newOrder[j + leftChildren.length + 1]+" ");
                    }
                } else {
                    for (int j = 0; j < rightChildren.length; j++) {
                        newOrder[j + leftChildren.length + 1] = rightChildren[j];
                        builder.append(newOrder[j + leftChildren.length + 1]+" ");
                    }
                }
                builder.append("\n");
                ContextInstance bestCandidate = new ContextInstance(head, newOrder, tree);

                reorderingInstances.add(bestCandidate);
            }
        }

        builder.append("\nreordering step by step\n");
        DependencyTree currentTree=tree;
        for(ContextInstance instance:reorderingInstances) {
            currentTree = (new ContextInstance(instance.getHeadIndex(), instance.getOrder(), currentTree)).getTree();
            builder.append(currentTree.toConllOutput(maps)+"\n");
        }
        builder.append("\nfinal tree\n");
        builder.append(currentTree.toConllOutput(maps)+"\n");
        builder.append("\n----------------------------------------------------------------------------------------------------------\n");

        System.out.println(builder.toString());
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
            pool.submit(new TreeReorderingThread(c, tree, this,maps));
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
        double elapsed=(double)(end-start)/count;
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
          pool.submit(new BitextReorderingThread(c,bitextDependency,this,maps));
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
        double elapsed=(double)(end-start)/count;
        System.err.println("time for decoding "+elapsed + " ms per sentence");
        writer.flush();
        writer.close();
        executor.shutdown();
    }

}
