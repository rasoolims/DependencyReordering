package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Decoding.ScoringThread;
import edu.columbia.cs.rasooli.Reordering.IO.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/17/15
 * Time: 12:41 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class Trainer {
    HashMap<String,String> universalMap;
    IndexMaps maps;
    int featLen;
    HashMap<String,int[]>[]  mostCommonPermutations;
    String trainPath;
    String devPath;
    int topK;
    int maxLen;
    AveragedPerceptron[]  classifier;
    
    public Trainer(String trainTreePath, String trainIntersectionPath,String devTreePath, String devIntersectionPath, String universalPOSPath, String trainPath, String devPath, int maxLen, int topK, int featLen) throws Exception {
        this.topK = topK;
        this.maxLen = maxLen;
        this.featLen=featLen;
        classifier=new AveragedPerceptron[maxLen];
        for(int i=0;i<maxLen;i++)
            classifier[i]=new AveragedPerceptron(topK, featLen);
        
        this.trainPath = trainPath;
        this.devPath = devPath;
        universalMap = BitextDependencyReader.createUniversalMap(universalPOSPath);
        maps = DependencyReader.readIndexMap(trainTreePath, universalMap);
        mostCommonPermutations = BitextDependencyReader.constructPosOrderFrequency(trainTreePath, trainIntersectionPath, universalMap, maps, maxLen, topK);
        BitextDependencyReader.createTrainData(trainTreePath, trainIntersectionPath, universalMap, maps, mostCommonPermutations, trainPath);

        if (!devTreePath.equals(""))
            BitextDependencyReader.createTrainData(devTreePath, devIntersectionPath, universalMap, maps, mostCommonPermutations, devPath);
    }
    
    public void trainWithPerceptron(int maxIter, String modelPath, int numOfThreads) throws Exception {
        System.err.println("Training started...");
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        CompletionService<FeaturedInstance> pool = new ExecutorCompletionService<FeaturedInstance>(executor);

        for (int i = 0; i < maxIter; i++) {
            long start = System.currentTimeMillis();
            System.err.println("\nIteration: " + (i + 1) + "...");
            int count = 0;
            float correct = 0;

            FileInputStream fos1 = new FileInputStream(trainPath);
            GZIPInputStream gz1 = new GZIPInputStream(fos1);
            ObjectInputStream trainReader = new ObjectInputStream(gz1);


            while (true) {
                count++;
                int index = trainReader.readInt();
                if (index == -1)
                    break;
                String goldLabel = (String) trainReader.readObject();
                ArrayList<Object>[] features = new ArrayList[featLen];
                for (int f = 0; f < featLen; f++)
                    features[f] = new ArrayList<Object>();

                int featIndex;
                while ((featIndex = trainReader.readInt()) != -1) {
                    Object feat = trainReader.readObject();
                    features[featIndex].add(feat);
                }

                double bestScore = Double.NEGATIVE_INFINITY;
                int bestLIndex = 0;
                int goldIndex = 0;
                int l = 0;
                for (String label : mostCommonPermutations[index].keySet()) {
                    double score = classifier[index].score(l, features, false);
                    if (score > bestScore) {
                        bestScore = score;
                        bestLIndex = l;
                    }
                    if (goldLabel.equals(label)) {
                        goldIndex = l;
                    }
                    l++;
                }

                if (goldIndex != bestLIndex) {
                    for (int f = 0; f < features.length; f++) {
                        for (Object feat : features[f]) {
                            classifier[index].updateWeight(goldIndex, f, feat, +1);
                            classifier[index].updateWeight(bestLIndex, f, feat, -1);
                        }
                    }
                } else correct++;

                classifier[index].incrementIteration();
                if (count % 10000 == 0)
                    System.err.print(count + "...");
            }
            System.err.print(count + "\n");
            float correctPredictions = 100f * correct / count;
            System.err.print("Correct prediction: " + correctPredictions + "\n");


            Info info = new Info(classifier, mostCommonPermutations, universalMap, topK, maps);
            info.saveModel(modelPath + "_iter" + (i + 1));
            long end = System.currentTimeMillis();
            long elapsed = (end - start) / 1000;
            System.err.println("time for training " + elapsed + " seconds");

            if (!devPath.equals("")) {
                FileInputStream fos2 = new FileInputStream(devPath);
                GZIPInputStream gz2 = new GZIPInputStream(fos2);
                ObjectInputStream devReader = new ObjectInputStream(gz2);

                HashMap<Object, Double>[][][] weights = info.getFinalWeights();
                AveragedPerceptron[] decodeClassifier = new AveragedPerceptron[weights.length];

                for (int l = 0; l < maxLen; l++) {
                    decodeClassifier[l] = new AveragedPerceptron(topK, featLen);
                    decodeClassifier[l].setAvgWeights(weights[l]);
                }

                count = 0;
                correct = 0;

                while (true) {
                    count++;
                    int index = devReader.readInt();
                    if (index == -1)
                        break;
                    String goldLabel = (String) devReader.readObject();
                    ArrayList<Object>[] features = new ArrayList[featLen];
                    for (int f = 0; f < featLen; f++)
                        features[f] = new ArrayList<Object>();

                    int featIndex;
                    while ((featIndex = devReader.readInt()) != -1) {
                        Object feat = devReader.readObject();
                        features[featIndex].add(feat);
                    }

                    double bestScore = Double.NEGATIVE_INFINITY;
                    int bestLIndex = 0;
                    int goldIndex = 0;
                    int l = 0;
                    for (String label : mostCommonPermutations[index].keySet()) {
                        double score = decodeClassifier[index].score(l, features, true);
                        if (score > bestScore) {
                            bestScore = score;
                            bestLIndex = l;
                        }
                        if (goldLabel.equals(label)) {
                            goldIndex = l;
                        }
                        l++;
                    }

                    if (goldIndex == bestLIndex) {
                        correct++;
                    }

                    if (count % 10000 == 0)
                        System.err.print(count + "...");
                }
            }


            System.err.print(count + "\n");
            correctPredictions = 100f * correct / count;
            System.err.print("Correct  dev prediction: " + correctPredictions + "\n");
        }

        executor.shutdown();
    }
}
