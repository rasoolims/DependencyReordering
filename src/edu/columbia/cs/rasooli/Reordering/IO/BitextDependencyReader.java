package edu.columbia.cs.rasooli.Reordering.IO;

import edu.columbia.cs.rasooli.Reordering.Structures.*;
import edu.columbia.cs.rasooli.Reordering.Training.PivotTrainData;
import edu.columbia.cs.rasooli.Reordering.Training.TrainData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/12/15
 * Time: 12:21 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class BitextDependencyReader {
    public static HashMap<String, String> createUniversalMap(String universalPosPath) throws Exception {
        HashMap<String, String> universalMap = new HashMap<String, String>();
        BufferedReader universalPosReader = new BufferedReader(new FileReader(universalPosPath));
        String line;
        while ((line = universalPosReader.readLine()) != null) {
            String[] split = line.trim().split("\t");
            if (split.length == 2)
                universalMap.put(split[0], split[1]);
        }
        universalMap.put("ROOT", "ROOT");
        return universalMap;
    }

    /**
     * @param parsedFilePath        should have mst format
     * @param alignIntersectionPath should be a alignment intersection
     */
    public static ArrayList<BitextDependency> readFromBitext(String parsedFilePath, String alignIntersectionPath, String universalPosPath, IndexMaps maps) throws Exception {
        HashMap<String, String> universalMap = createUniversalMap(universalPosPath);

        ArrayList<BitextDependency> data = new ArrayList<BitextDependency>();

        BufferedReader depReader = new BufferedReader(new FileReader(parsedFilePath));
        BufferedReader intersectionReader = new BufferedReader(new FileReader(alignIntersectionPath));

        String line1;
        int count = 0;
        int contextCount = 0;
        while ((line1 = depReader.readLine()) != null) {
            if (line1.trim().length() == 0)
                continue;
            String[] w = line1.trim().split("\t");
            String[] t = depReader.readLine().trim().split("\t");
            String[] l = depReader.readLine().trim().split("\t");
            String[] h = depReader.readLine().trim().split("\t");

            assert w.length == t.length && t.length == l.length && h.length == l.length;

            String[] words = new String[w.length + 1];
            words[0] = "ROOT";
            for (int i = 0; i < w.length; i++)
                words[i + 1] = w[i];

            String[] tags = new String[t.length + 1];
            tags[0] = "ROOT";
            for (int i = 0; i < t.length; i++)
                tags[i + 1] = t[i];

            int[] labels = new int[l.length + 1];
            labels[0] = maps.strMap.get("ROOT");
            for (int i = 0; i < l.length; i++)
                labels[i + 1] = maps.strMap.get(l[i]);

            int[] heads = new int[h.length + 1];
            heads[0] = -1;
            for (int i = 0; i < h.length; i++)
                heads[i + 1] = Integer.parseInt(h[i]);

            Word[] wordStructs = new Word[words.length];
            for (int i = 0; i < words.length; i++) {
                wordStructs[i] = new Word(i, words[i], tags[i], universalMap.get(tags[i]), maps);
            }


            DependencyTree tree = new DependencyTree(wordStructs, heads, labels,l);

            String[] split = intersectionReader.readLine().trim().split(" ");
            SortedSet<Integer>[] alignedWords = new SortedSet[words.length];
            for (int i = 0; i < alignedWords.length; i++)
                alignedWords[i] = new TreeSet<Integer>();

            for (String s : split) {
                String[] inds = s.split("-");
                int d1 = Integer.parseInt(inds[0]);
                assert d1 < words.length;
                alignedWords[d1].add(Integer.parseInt(inds[1]));
            }

            BitextDependency bitext = new BitextDependency(alignedWords, tree);
            if (bitext.getTrainableHeads().size() > 0)
                data.add(bitext);
            contextCount += bitext.getTrainableHeads().size();
            count++;
            if (count % 10000 == 0)
                System.err.print(count + "(" + data.size() + ":" + contextCount + ")...");
        }
        System.err.print(count + "\n");
        return data;
    }

    public static BitextDependency readNextBitextDependency(BufferedReader depReader, BufferedReader intersectionReader, HashMap<String, String> universalMap, IndexMaps maps) throws IOException {
        String line1;
        while ((line1 = depReader.readLine()) != null) {
            if (line1.trim().length() == 0)
                continue;
            String[] w = line1.trim().split("\t");
            String[] t = depReader.readLine().trim().split("\t");
            String[] l = depReader.readLine().trim().split("\t");
            String[] h = depReader.readLine().trim().split("\t");

            assert w.length == t.length && t.length == l.length && h.length == l.length;

            String[] words = new String[w.length + 1];
            words[0] = "ROOT";
            for (int i = 0; i < w.length; i++)
                words[i + 1] = w[i];

            String[] tags = new String[t.length + 1];
            tags[0] = "ROOT";
            for (int i = 0; i < t.length; i++)
                tags[i + 1] = t[i];

            int[] labels = new int[l.length + 1];
            labels[0] = maps.strMap.get("ROOT");
            for (int i = 0; i < l.length; i++)
                labels[i + 1] = maps.strMap.get(l[i]);

            int[] heads = new int[h.length + 1];
            heads[0] = -1;
            for (int i = 0; i < h.length; i++)
                heads[i + 1] = Integer.parseInt(h[i]);

            Word[] wordStructs = new Word[words.length];
            for (int i = 0; i < words.length; i++) {
                wordStructs[i] = new Word(i, words[i], tags[i], universalMap.get(tags[i]), maps);
            }


            DependencyTree tree = new DependencyTree(wordStructs, heads, labels,l);

            String[] split = intersectionReader.readLine().trim().split(" ");
            SortedSet<Integer>[] alignedWords = new SortedSet[words.length];
            for (int i = 0; i < alignedWords.length; i++)
                alignedWords[i] = new TreeSet<Integer>();

            for (String s : split) {
                String[] inds = s.split("-");
                int d1 = Integer.parseInt(inds[0]);
                assert d1 < words.length;
                alignedWords[d1].add(Integer.parseInt(inds[1]));
            }

            BitextDependency bitext = new BitextDependency(alignedWords, tree);
            return bitext;

        }
        return null;
    }

    public static HashMap<String, int[]>[] constructMostCommonOrderings(String parsedFilePath, String alignIntersectionPath, HashMap<String, String> universalMap, IndexMaps maps, int maxLength, int topK) throws Exception {
        HashMap<String, Pair<Integer, int[]>>[] posOrderMap = new HashMap[maxLength];
        for (int i = 0; i < posOrderMap.length; i++)
            posOrderMap[i] = new HashMap<String, Pair<Integer, int[]>>();

        System.err.print("Constructing common reorderings...");

        BufferedReader depReader = new BufferedReader(new FileReader(parsedFilePath));
        BufferedReader intersectionReader = new BufferedReader(new FileReader(alignIntersectionPath));

        String line1;
        int count = 0;
        while ((line1 = depReader.readLine()) != null) {
            if (line1.trim().length() == 0)
                continue;
            String[] w = line1.trim().split("\t");
            String[] t = depReader.readLine().trim().split("\t");
            String[] l = depReader.readLine().trim().split("\t");
            String[] h = depReader.readLine().trim().split("\t");

            assert w.length == t.length && t.length == l.length && h.length == l.length;

            String[] words = new String[w.length + 1];
            words[0] = "ROOT";
            for (int i = 0; i < w.length; i++)
                words[i + 1] = w[i];

            String[] tags = new String[t.length + 1];
            tags[0] = "ROOT";
            for (int i = 0; i < t.length; i++)
                tags[i + 1] = t[i];

            int[] labels = new int[l.length + 1];
            labels[0] = maps.strMap.get("ROOT");
            for (int i = 0; i < l.length; i++)
                labels[i + 1] =maps.strMap.get( l[i]);

            int[] heads = new int[h.length + 1];
            heads[0] = -1;
            for (int i = 0; i < h.length; i++)
                heads[i + 1] = Integer.parseInt(h[i]);

            Word[] wordStructs = new Word[words.length];
            for (int i = 0; i < words.length; i++) {
                wordStructs[i] = new Word(i, words[i], tags[i], universalMap.get(tags[i]), maps);
            }


            DependencyTree tree = new DependencyTree(wordStructs, heads, labels,l);

            String[] split = intersectionReader.readLine().trim().split(" ");
            SortedSet<Integer>[] alignedWords = new SortedSet[words.length];
            for (int i = 0; i < alignedWords.length; i++)
                alignedWords[i] = new TreeSet<Integer>();

            for (String s : split) {
                String[] inds = s.split("-");
                int d1 = Integer.parseInt(inds[0]);
                assert d1 < words.length;
                alignedWords[d1].add(Integer.parseInt(inds[1]));
            }

            BitextDependency bitextDependency = new BitextDependency(alignedWords, tree);
            if (bitextDependency.getTrainableHeads().size() > 0) {
                for (int head : bitextDependency.getTrainableHeads()) {
                    HashSet<Integer> dx = bitextDependency.getSourceTree().getDependents(head);
                    HashSet<Integer> deps = new HashSet(dx);
                    deps.add(head);

                    TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
                    // origOrderSet.add(head);
                    for (int dep : deps)
                        origOrderSet.add(dep);

                    int index = 0;
                    int[] ordering = new int[origOrderSet.size()];
                    HashMap<Integer, Integer> revOrdering = new HashMap<Integer, Integer>();
                    //revOrdering.put(head, index);
                    //  ordering[index++] = head;
                    for (int d : deps) {
                        revOrdering.put(d, index);
                            ordering[index++] = d;
                    }

                    TreeMap<Integer, Integer> changedOrder = new TreeMap<Integer, Integer>();

                    SortedSet<Integer>[] alignedSet = bitextDependency.getAlignedWords();
                    // changedOrder.put(alignedSet[head].first(), head);
                    for (int dep : origOrderSet)
                        changedOrder.put(alignedSet[dep].first(), dep);

                    int[] goldOrder = new int[deps.size()];
                    StringBuilder goldOrderStr = new StringBuilder();
                    int i = 0;
                    for (int dep : changedOrder.keySet()) {
                        goldOrder[i++] = revOrdering.get(changedOrder.get(dep));
                        goldOrderStr.append(revOrdering.get(changedOrder.get(dep)) + "-");
                    }

                    if (dx.size() <= maxLength) {
                        String orderStr = goldOrderStr.toString();
                        if (!posOrderMap[goldOrder.length - 2].containsKey(orderStr))
                            posOrderMap[goldOrder.length - 2].put(orderStr, new Pair<Integer, int[]>(1, goldOrder));
                        else {
                            Pair<Integer, int[]> prevPair = posOrderMap[goldOrder.length - 2].get(orderStr);
                            prevPair.first += 1;
                            posOrderMap[goldOrder.length - 2].put(orderStr, prevPair);
                        }
                    }
                }
            }
            count++;
            if (count % 1000000 == 0)
                System.err.print(count + "...");
        }

        HashMap<String, int[]>[] mostCommonPermutations = new HashMap[maxLength];
        for (int i = 0; i < maxLength; i++) {
            mostCommonPermutations[i] = new HashMap<String, int[]>(topK);

            TreeSet<LabelOrderFrequency> labelOrderFrequencies = new TreeSet<LabelOrderFrequency>();
            for (String orderStr : posOrderMap[i].keySet()) {
                Pair<Integer, int[]> pair = posOrderMap[i].get(orderStr);
                labelOrderFrequencies.add(new LabelOrderFrequency(orderStr, pair.second, pair.first));
                if (labelOrderFrequencies.size() > topK)
                    labelOrderFrequencies.pollFirst();
            }

            for (LabelOrderFrequency labelOrderFrequency : labelOrderFrequencies) {
                mostCommonPermutations[i].put(labelOrderFrequency.getLabel(), labelOrderFrequency.getOrder());
            }

        }
        System.err.print(count + "\n");

        return mostCommonPermutations;
    }

    public static ArrayList<TrainData> getNextTrainData(BufferedReader depReader ,BufferedReader intersectionReader, HashMap<String, String> universalMap, IndexMaps maps, HashMap<String, int[]>[] mostCommonPermutations, int max) throws Exception {
        ArrayList<TrainData> trainData=new ArrayList<TrainData>(max);
        
        String line1;
        while ((line1 = depReader.readLine()) != null) {
            if (line1.trim().length() == 0)
                continue;
            String[] w = line1.trim().split("\t");
            String[] t = depReader.readLine().trim().split("\t");
            String[] l = depReader.readLine().trim().split("\t");
            String[] h = depReader.readLine().trim().split("\t");

            assert w.length == t.length && t.length == l.length && h.length == l.length;

            String[] words = new String[w.length + 1];
            words[0] = "ROOT";
            for (int i = 0; i < w.length; i++)
                words[i + 1] = w[i];

            String[] tags = new String[t.length + 1];
            tags[0] = "ROOT";
            for (int i = 0; i < t.length; i++)
                tags[i + 1] = t[i];

            int[] labels = new int[l.length + 1];
            labels[0] = maps.strMap.get("ROOT");
            for (int i = 0; i < l.length; i++)
                labels[i + 1] = maps.strMap.get(l[i]);

            int[] heads = new int[h.length + 1];
            heads[0] = -1;
            for (int i = 0; i < h.length; i++)
                heads[i + 1] = Integer.parseInt(h[i]);

            Word[] wordStructs = new Word[words.length];
            for (int i = 0; i < words.length; i++) {
                wordStructs[i] = new Word(i, words[i], tags[i], universalMap.get(tags[i]), maps);
            }

            DependencyTree tree = new DependencyTree(wordStructs, heads, labels,l);
            String[] split = intersectionReader.readLine().trim().split(" ");
            SortedSet<Integer>[] alignedWords = new SortedSet[words.length];
            for (int i = 0; i < alignedWords.length; i++)
                alignedWords[i] = new TreeSet<Integer>();

            for (String s : split) {
                String[] inds = s.split("-");
                int d1 = Integer.parseInt(inds[0]);
                assert d1 < words.length;
                alignedWords[d1].add(Integer.parseInt(inds[1]));
            }

            BitextDependency bitextDependency = new BitextDependency(alignedWords, tree);
            if (bitextDependency.getTrainableHeads().size() > 0) {
                for (int head : bitextDependency.getTrainableHeads()) {
                    HashSet<Integer> deps = bitextDependency.getSourceTree().getDependents(head);
                    if (deps.size() > mostCommonPermutations.length)
                        continue;

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

                    ContextInstance originalInstance = new ContextInstance(head, ordering, bitextDependency.getSourceTree());
                    ArrayList<Object>[] features = originalInstance.extractMainFeatures();

                    TreeMap<Integer, Integer> changedOrder = new TreeMap<Integer, Integer>();

                    SortedSet<Integer>[] alignedSet = bitextDependency.getAlignedWords();
                    changedOrder.put(alignedSet[head].first(), head);
                    for (int dep : origOrderSet)
                        changedOrder.put(alignedSet[dep].first(), dep);

                    int[] goldOrder = new int[1 + deps.size()];
                    StringBuilder goldOrderStr = new StringBuilder();
                    int i = 0;
                    for (int dep : changedOrder.keySet()) {
                        goldOrder[i++] = revOrdering.get(changedOrder.get(dep));
                        goldOrderStr.append(revOrdering.get(changedOrder.get(dep)) + "-");
                    }

                    String goldLabel = goldOrderStr.toString();
                    int ind = goldOrder.length - 2;
                    if (mostCommonPermutations[ind].containsKey(goldLabel)) {
                        trainData.add(new TrainData(ind,goldLabel,features));
                        if(trainData.size()>=max)
                            return trainData;
                    }
                }
            }
        }
        if(trainData.size()==0)
            return null;
        return trainData;
    }


    public static Pair<HashMap<String, int[]>[],HashMap<String, int[]>[]> constructMostCommonLeftRightOrderings(String parsedFilePath, String alignIntersectionPath, HashMap<String, String> universalMap, IndexMaps maps, int maxLength, int topK) throws Exception{
        HashMap<String, int[]>[] leftOrdering=new HashMap[maxLength-1];
        HashMap<String, int[]>[] rightOrdering=new HashMap[maxLength-1];
        for(int i=0;i<maxLength-1;i++){
            leftOrdering[i]=new HashMap<String, int[]>();
            rightOrdering[i]=new HashMap<String, int[]>();
        }


        HashMap<String, Pair<Integer, int[]>>[] leftOrderMap = new HashMap[maxLength-1];
        HashMap<String, Pair<Integer, int[]>>[] rightOrderMap = new HashMap[maxLength-1];
        for (int i = 0; i < leftOrderMap.length; i++) {
            leftOrderMap[i] = new HashMap<String, Pair<Integer, int[]>>();
            rightOrderMap[i] = new HashMap<String, Pair<Integer, int[]>>();
        }

        BufferedReader depReader = new BufferedReader(new FileReader(parsedFilePath));
        BufferedReader intersectionReader = new BufferedReader(new FileReader(alignIntersectionPath));

        String line1;
        int count = 0;
        while ((line1 = depReader.readLine()) != null) {
            if (line1.trim().length() == 0)
                continue;
            String[] w = line1.trim().split("\t");
            String[] t = depReader.readLine().trim().split("\t");
            String[] l = depReader.readLine().trim().split("\t");
            String[] h = depReader.readLine().trim().split("\t");

            assert w.length == t.length && t.length == l.length && h.length == l.length;

            String[] words = new String[w.length + 1];
            words[0] = "ROOT";
            for (int i = 0; i < w.length; i++)
                words[i + 1] = w[i];

            String[] tags = new String[t.length + 1];
            tags[0] = "ROOT";
            for (int i = 0; i < t.length; i++)
                tags[i + 1] = t[i];

            int[] labels = new int[l.length + 1];
            labels[0] = maps.strMap.get("ROOT");
            for (int i = 0; i < l.length; i++)
                labels[i + 1] = maps.strMap.get( l[i]);

            int[] heads = new int[h.length + 1];
            heads[0] = -1;
            for (int i = 0; i < h.length; i++)
                heads[i + 1] = Integer.parseInt(h[i]);

            Word[] wordStructs = new Word[words.length];
            for (int i = 0; i < words.length; i++) {
                wordStructs[i] = new Word(i, words[i], tags[i], universalMap.get(tags[i]), maps);
            }


            DependencyTree tree = new DependencyTree(wordStructs, heads, labels,l);

            String[] split = intersectionReader.readLine().trim().split(" ");
            SortedSet<Integer>[] alignedWords = new SortedSet[words.length];
            for (int i = 0; i < alignedWords.length; i++)
                alignedWords[i] = new TreeSet<Integer>();

            for (String s : split) {
                String[] inds = s.split("-");
                int d1 = Integer.parseInt(inds[0]);
                assert d1 < words.length;
                alignedWords[d1].add(Integer.parseInt(inds[1]));
            }

            BitextDependency bitextDependency = new BitextDependency(alignedWords, tree);
            if (bitextDependency.getTrainableHeads().size() > 0) {
                for (int head : bitextDependency.getTrainableHeads()) {
                    HashSet<Integer> dx = bitextDependency.getSourceTree().getDependents(head);
                    HashSet<Integer> deps = new HashSet(dx);
                    deps.add(head);

                    TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
                    // origOrderSet.add(head);
                    for (int dep : deps)
                        origOrderSet.add(dep);

                    TreeMap<Integer, Integer> changedOrder = new TreeMap<Integer, Integer>();

                    SortedSet<Integer>[] alignedSet = bitextDependency.getAlignedWords();
                    // changedOrder.put(alignedSet[head].first(), head);
                    for (int dep : origOrderSet)
                        changedOrder.put(alignedSet[dep].first(), dep);

                    ArrayList<Integer> goldLeftOrder=new ArrayList<Integer>();
                    ArrayList<Integer> goldRightOrder=new ArrayList<Integer>();
                    TreeMap<Integer,Integer> leftRevMap=new TreeMap<Integer, Integer>();
                    TreeMap<Integer,Integer> rightRevMap=new TreeMap<Integer, Integer>();


                    int index=0;

                    boolean left=true;
                    for (int dep : changedOrder.keySet()) {
                        if(changedOrder.get(dep)==head) {
                            index=0;
                            left=false;
                        } else if(left){
                            goldLeftOrder.add(changedOrder.get(dep));
                            leftRevMap.put(changedOrder.get(dep),index++);
                        } else{
                            goldRightOrder.add(changedOrder.get(dep));
                            rightRevMap.put(changedOrder.get(dep),index++);
                        }
                    }

                    int[] goldLeftOrderArray=new int[goldLeftOrder.size()];
                    int[] goldRightOrderArray=new int[goldRightOrder.size()];
                    StringBuilder goldLeftStr=new StringBuilder();
                    StringBuilder goldRightStr=new StringBuilder();


                    int i = 0;
                    for (int dep : leftRevMap.keySet()) {
                        goldLeftOrderArray[i++] = leftRevMap.get(dep);
                        goldLeftStr.append(leftRevMap.get(dep) + "-");
                    }

                    i = 0;
                    for (int dep : rightRevMap.keySet()) {
                        goldRightOrderArray[i++] = rightRevMap.get(dep);
                        goldRightStr.append(rightRevMap.get(dep) + "-");
                    }
                    
                    if(goldLeftOrder.size()<maxLength && goldLeftOrder.size()>1) {
                        int ind=goldLeftOrder.size()-2;
                        String orderStr=goldLeftStr.toString();
                        if(!leftOrderMap[ind].containsKey(orderStr))
                            leftOrderMap[ind].put(orderStr,new Pair<Integer, int[]>(1,goldLeftOrderArray));
                        else{
                            leftOrderMap[ind].get(orderStr).first++;
                        }
                    }

                    if(goldRightOrder.size()<maxLength && goldRightOrder.size()>1){
                        int ind=goldRightOrder.size()-2;
                        String orderStr=goldRightStr.toString();
                        if(!rightOrderMap[ind].containsKey(orderStr))
                            rightOrderMap[ind].put(orderStr,new Pair<Integer, int[]>(1,goldRightOrderArray));
                        else{
                            rightOrderMap[ind].get(orderStr).first++;
                        }
                    }
                }
            }
            count++;
            if (count % 100000 == 0)
                System.err.print(count + "...");
        }

        for (int i = 0; i < maxLength-1; i++) {
            TreeSet<LabelOrderFrequency> labelOrderFrequencies = new TreeSet<LabelOrderFrequency>();
            for (String orderStr : leftOrderMap[i].keySet()) {
                Pair<Integer, int[]> pair = leftOrderMap[i].get(orderStr);
                labelOrderFrequencies.add(new LabelOrderFrequency(orderStr, pair.second, pair.first));
                if (labelOrderFrequencies.size() > topK)
                    labelOrderFrequencies.pollFirst();
            }

            for (LabelOrderFrequency labelOrderFrequency : labelOrderFrequencies) {
                leftOrdering[i].put(labelOrderFrequency.getLabel(), labelOrderFrequency.getOrder());
            }

            labelOrderFrequencies = new TreeSet<LabelOrderFrequency>();
            for (String orderStr : rightOrderMap[i].keySet()) {
                Pair<Integer, int[]> pair = rightOrderMap[i].get(orderStr);
                labelOrderFrequencies.add(new LabelOrderFrequency(orderStr, pair.second, pair.first));
                if (labelOrderFrequencies.size() > topK)
                    labelOrderFrequencies.pollFirst();
            }

            for (LabelOrderFrequency labelOrderFrequency : labelOrderFrequencies) {
                rightOrdering[i].put(labelOrderFrequency.getLabel(), labelOrderFrequency.getOrder());
            }

        }
        System.err.print(count + "\n");
        
        
        return  new Pair<HashMap<String, int[]>[], HashMap<String, int[]>[]>(leftOrdering,rightOrdering);

    }

    //todo
    public static Pair<ArrayList<PivotTrainData>, Pair<ArrayList<TrainData>, ArrayList<TrainData>>> getLeftRightTrainData(BufferedReader depReader,BufferedReader intersectionReader , HashMap<String, String> universalMap, IndexMaps maps, int maxLength, int maxInstance) throws Exception {
         int instance=0;

        ArrayList<TrainData> leftTrainData = new ArrayList<TrainData>();
        ArrayList<TrainData> rightTrainData = new ArrayList<TrainData>();
        ArrayList<PivotTrainData> pivotTrainData = new ArrayList<PivotTrainData>();

        String line1;
        int count = 0;
        while ((line1 = depReader.readLine()) != null) {
            if (line1.trim().length() == 0)
                continue;
            String[] w = line1.trim().split("\t");
            String[] t = depReader.readLine().trim().split("\t");
            String[] l = depReader.readLine().trim().split("\t");
            String[] h = depReader.readLine().trim().split("\t");

            assert w.length == t.length && t.length == l.length && h.length == l.length;

            String[] words = new String[w.length + 1];
            words[0] = "ROOT";
            for (int i = 0; i < w.length; i++)
                words[i + 1] = w[i];

            String[] tags = new String[t.length + 1];
            tags[0] = "ROOT";
            for (int i = 0; i < t.length; i++)
                tags[i + 1] = t[i];

            int[] labels = new int[l.length + 1];
            labels[0] = maps.strMap.get("ROOT");
            for (int i = 0; i < l.length; i++)
                if(maps.strMap.containsKey(l[i]))
                labels[i + 1] =maps.strMap.get( l[i]);
            else
                labels[i+1]=-1;

            int[] heads = new int[h.length + 1];
            heads[0] = -1;
            for (int i = 0; i < h.length; i++)
                heads[i + 1] = Integer.parseInt(h[i]);

            Word[] wordStructs = new Word[words.length];
            for (int i = 0; i < words.length; i++) {
                wordStructs[i] = new Word(i, words[i], tags[i], universalMap.get(tags[i]), maps);
            }


            DependencyTree tree = new DependencyTree(wordStructs, heads, labels,l);

            String[] split = intersectionReader.readLine().trim().split(" ");
            SortedSet<Integer>[] alignedWords = new SortedSet[words.length];
            for (int i = 0; i < alignedWords.length; i++)
                alignedWords[i] = new TreeSet<Integer>();

            for (String s : split) {
                String[] inds = s.split("-");
                int d1 = Integer.parseInt(inds[0]);
                assert d1 < words.length;
                alignedWords[d1].add(Integer.parseInt(inds[1]));
            }

            BitextDependency bitextDependency = new BitextDependency(alignedWords, tree);
            if (bitextDependency.getTrainableHeads().size() > 0) {
                for (int head : bitextDependency.getTrainableHeads()) {
                    HashSet<Integer> dx = bitextDependency.getSourceTree().getDependents(head);
                    HashSet<Integer> deps = new HashSet(dx);
                    deps.add(head);

                    TreeSet<Integer> origOrderSet = new TreeSet<Integer>();
                    // origOrderSet.add(head);
                    for (int dep : deps)
                        origOrderSet.add(dep);

                    TreeMap<Integer, Integer> changedOrder = new TreeMap<Integer, Integer>();

                    SortedSet<Integer>[] alignedSet = bitextDependency.getAlignedWords();
                    // changedOrder.put(alignedSet[head].first(), head);
                    for (int dep : origOrderSet)
                        changedOrder.put(alignedSet[dep].first(), dep);

                    ArrayList<Integer> goldLeftOrder = new ArrayList<Integer>();
                    ArrayList<Integer> goldRightOrder = new ArrayList<Integer>();
                    TreeMap<Integer, Integer> leftRevMap = new TreeMap<Integer, Integer>();
                    TreeMap<Integer, Integer> rightRevMap = new TreeMap<Integer, Integer>();
                    TreeSet<Integer> unorderedLeftChildren = new TreeSet<Integer>();
                    TreeSet<Integer> unorderedRightChildren = new TreeSet<Integer>();

                    int index = 0;

                    boolean left = true;
                    for (int dep : changedOrder.keySet()) {
                        if (changedOrder.get(dep) == head) {
                            index = 0;
                            left = false;
                        } else if (left) {
                            goldLeftOrder.add(changedOrder.get(dep));
                            unorderedLeftChildren.add(changedOrder.get(dep));
                            leftRevMap.put(changedOrder.get(dep), index++);
                        } else {
                            goldRightOrder.add(changedOrder.get(dep));
                            unorderedRightChildren.add(changedOrder.get(dep));
                            rightRevMap.put(changedOrder.get(dep), index++);
                        }
                    }

                    int[] goldLeftOrderArray = new int[goldLeftOrder.size()];
                    int[] goldRightOrderArray = new int[goldRightOrder.size()];
                    StringBuilder goldLeftStr = new StringBuilder();
                    StringBuilder goldRightStr = new StringBuilder();


                    int i = 0;
                    for (int dep : leftRevMap.keySet()) {
                        goldLeftOrderArray[i++] = leftRevMap.get(dep);
                        goldLeftStr.append(leftRevMap.get(dep) + "-");
                    }
                    i = 0;
                    for (int dep : rightRevMap.keySet()) {
                        goldRightOrderArray[i++] = rightRevMap.get(dep);
                        goldRightStr.append(rightRevMap.get(dep) + "-");
                    }

                    int[] leftChildren = new int[unorderedLeftChildren.size()];
                    i = 0;
                    for (int ch : unorderedLeftChildren)
                        leftChildren[i++] = ch;
                    int[] rightChildren = new int[unorderedRightChildren.size()];
                    i = 0;
                    for (int ch : unorderedRightChildren)
                        rightChildren[i++] = ch;

                    if (leftChildren.length <= maxLength && leftChildren.length > 1)
                        leftTrainData.add(new TrainData(goldLeftOrderArray.length - 2, goldLeftStr.toString(), tree.extractMainFeatures(head, leftChildren)));
                    if (rightChildren.length <= maxLength && rightChildren.length > 1)
                        rightTrainData.add(new TrainData(goldRightOrderArray.length - 2, goldRightStr.toString(), tree.extractMainFeatures(head, rightChildren)));

                    for (int dep : dx) {
                        boolean before = true;
                        if (unorderedRightChildren.contains(dep))
                            before = false;
                        pivotTrainData.add(new PivotTrainData(before, tree.extractPivotFeatures(head, dep)));
                    }
                    instance++;
                }
                
                
                count++;
            }
            if(instance>=maxInstance)
                break;
        }
        if (instance==0)
            return null;
        
        return new Pair<ArrayList<PivotTrainData>, Pair<ArrayList<TrainData>, ArrayList<TrainData>>>(pivotTrainData,
                new Pair<ArrayList<TrainData>, ArrayList<TrainData>>(leftTrainData, rightTrainData));
    }

}
