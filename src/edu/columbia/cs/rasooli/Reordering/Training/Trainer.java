package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
import edu.columbia.cs.rasooli.Reordering.Classifier.OnlinePegasos;
import edu.columbia.cs.rasooli.Reordering.IO.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.IndexMaps;
import edu.columbia.cs.rasooli.Reordering.Structures.Info;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/17/15
 * Time: 12:41 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class Trainer {
    HashMap<String, String> universalMap;
    IndexMaps maps;
    int featLen;
    HashMap<String, int[]>[] mostCommonPermutations;
    String trainTreePath;
    String trainIntersectionPath;
    String devIntersectionPath;
    String devTreePath;
    int topK;
    int maxLen;
    Classifier[] classifier;

    public Trainer(String trainTreePath, String trainIntersectionPath, String devTreePath, String devIntersectionPath, String universalPOSPath, String trainPath, String devPath, int maxLen, int topK, int featLen) throws Exception {
        this.trainIntersectionPath = trainIntersectionPath;
        this.trainTreePath = trainTreePath;
        this.devTreePath = devTreePath;
        this.devIntersectionPath = devIntersectionPath;
        this.topK = topK;
        this.maxLen = maxLen;
        this.featLen = featLen;
        

        universalMap = BitextDependencyReader.createUniversalMap(universalPOSPath);
        maps = DependencyReader.readIndexMap(trainTreePath, universalMap);
        mostCommonPermutations = BitextDependencyReader.constructMostCommonOrderings(trainTreePath, trainIntersectionPath, universalMap, maps, maxLen, topK);
    }

    public void trainWithPerceptron(int maxIter, String modelPath, int numOfThreads) throws Exception {
        System.err.println("Training started...");
        classifier = new AveragedPerceptron[maxLen];
        for (int i = 0; i < maxLen; i++)
            classifier[i] = new AveragedPerceptron(topK, featLen);
        
        int max = 200000;

        for (int i = 0; i < maxIter; i++) {
            long start = System.currentTimeMillis();
            System.err.println("\nIteration: " + (i + 1) + "...");
            int count = 0;
            int cCount = 0;
            double[] correct = new double[mostCommonPermutations.length];
            int[] sepCount = new int[mostCommonPermutations.length];

            BufferedReader depReader = new BufferedReader(new FileReader(trainTreePath));
            BufferedReader intersectionReader = new BufferedReader(new FileReader(trainIntersectionPath));
            ArrayList<TrainData> data;

            while ((data = BitextDependencyReader.getNextTrainData(depReader, intersectionReader, universalMap, maps, mostCommonPermutations, max)) != null) {
                for (TrainData trainData : data) {
                    count++;

                    int index = trainData.index;
                    ArrayList<Object>[] features = trainData.features;
                    double bestScore = Double.NEGATIVE_INFINITY;
                    String goldLabel = trainData.goldLabel;
                    int bestLIndex = -1;
                    int goldIndex = -1;
                    double[] scores = classifier[index].scores(features, false);

                    int l = 0;
                    for (String label : mostCommonPermutations[index].keySet()) {
                        if (scores[l] > bestScore) {
                            bestScore = scores[l];
                            bestLIndex = l;
                        }
                        if (label.equals(goldLabel))
                            goldIndex = l;
                        l++;
                    }
                    
                    if (goldIndex != bestLIndex) {
                        for (int f = 0; f < features.length; f++) {
                            for (Object feat : features[f]) {
                                classifier[index].updateWeight(goldIndex, f, feat, +1);
                                classifier[index].updateWeight(bestLIndex, f, feat, -1);
                            }
                        }
                    } else {
                        correct[index]++;
                        cCount++;
                    }
                    sepCount[index]++;

                    classifier[index].incrementIteration();
                    if (count % 10000 == 0)
                        System.err.print(count + "...");
                }
            }
            System.err.print(count + "\n");
            double correctPredictions = 100f * cCount / count;
            System.err.print("Correct prediction :" + correctPredictions + "\n");
            Info info = new Info(classifier, mostCommonPermutations, universalMap, topK, maps);

            if (i == 0)
                info.saveInitModel(modelPath);

            info.saveModel(modelPath + "_iter" + (i + 1));

            long end = System.currentTimeMillis();
            long elapsed = (end - start) / 1000;
            System.err.println("time for training " + elapsed + " seconds");


            if (!devTreePath.equals("")) {
                depReader = new BufferedReader(new FileReader(devTreePath));
                intersectionReader = new BufferedReader(new FileReader(devIntersectionPath));

                count = 0;
                correct = new double[mostCommonPermutations.length];
                sepCount = new int[mostCommonPermutations.length];

                while ((data = BitextDependencyReader.getNextTrainData(depReader, intersectionReader, universalMap, maps, mostCommonPermutations, max)) != null) {
                    for (TrainData trainData : data) {
                        count++;

                        int index = trainData.index;
                        ArrayList<Object>[] features = trainData.features;

                        double bestScore = Double.NEGATIVE_INFINITY;
                        String goldLabel = trainData.goldLabel;
                        int bestLIndex = -1;
                        int goldIndex = -1;
                        double[] scores = classifier[index].scores(features);

                        int l = 0;
                        for (String label : mostCommonPermutations[index].keySet()) {
                            if (scores[l] > bestScore) {
                                bestScore = scores[l];
                                bestLIndex = l;
                            }
                            if (label.equals(goldLabel))
                                goldIndex = l;
                            l++;
                        }

                        if (goldIndex == bestLIndex)
                            correct[index]++;
                        sepCount[index]++;

                        if (count % 10000 == 0)
                            System.err.print(count + "...");
                    }
                }
            }

            System.err.print(count + "\n");
            for (int b = 0; b < mostCommonPermutations.length; b++) {
                correctPredictions = 100f * correct[b] / sepCount[b];
                System.err.print("Correct prediction " + b + ":" + correctPredictions + "\n");
            }
        }
    }

    public void trainWithPegasos(int maxIter, String modelPath, double lambda, int numOfThreads) throws Exception {
        System.err.println("Training started...");
        classifier = new OnlinePegasos[maxLen];
        for (int i = 0; i < maxLen; i++)
            classifier[i] = new OnlinePegasos(topK, featLen,lambda);
        
        int max = 200000;

        for (int i = 0; i < maxIter; i++) {
            long start = System.currentTimeMillis();
            System.err.println("\nIteration: " + (i + 1) + "...");
            int count = 0;
            int cCount = 0;
            double[] correct = new double[mostCommonPermutations.length];
            int[] sepCount = new int[mostCommonPermutations.length];

            BufferedReader depReader = new BufferedReader(new FileReader(trainTreePath));
            BufferedReader intersectionReader = new BufferedReader(new FileReader(trainIntersectionPath));
            ArrayList<TrainData> data;

            while ((data = BitextDependencyReader.getNextTrainData(depReader, intersectionReader, universalMap, maps, mostCommonPermutations, max)) != null) {
                for (TrainData trainData : data) {
                    count++;

                    int index = trainData.index;
                    ArrayList<Object>[] features = trainData.features;
                    double bestScore = Double.NEGATIVE_INFINITY;
                    String goldLabel = trainData.goldLabel;
                    int bestLIndex = -1;
                    int goldIndex = -1;
                    double[] scores = classifier[index].scores(features, false);

                    int l = 0;
                    for (String label : mostCommonPermutations[index].keySet()) {
                        if (scores[l] > bestScore) {
                            bestScore = scores[l];
                            bestLIndex = l;
                        }
                        if (label.equals(goldLabel))
                            goldIndex = l;
                        l++;
                    }

                    if (goldIndex != bestLIndex) {
                        OnlinePegasos pegas=(OnlinePegasos)classifier[index];
                        pegas.updateWeights(goldIndex,bestLIndex,features);
                    } else {
                        correct[index]++;
                        cCount++;
                    }
                    sepCount[index]++;

                    classifier[index].incrementIteration();
                    if (count % 10000 == 0)
                        System.err.print(count + "...");
                }
            }
            System.err.print(count + "\n");
            double correctPredictions = 100f * cCount / count;
            System.err.print("Correct prediction :" + correctPredictions + "\n");
            Info info = new Info(classifier, mostCommonPermutations, universalMap, topK, maps);

            if (i == 0)
                info.saveInitModel(modelPath);

            info.saveModel(modelPath + "_iter" + (i + 1));

            long end = System.currentTimeMillis();
            long elapsed = (end - start) / 1000;
            System.err.println("time for training " + elapsed + " seconds");


            if (!devTreePath.equals("")) {
                depReader = new BufferedReader(new FileReader(devTreePath));
                intersectionReader = new BufferedReader(new FileReader(devIntersectionPath));

                count = 0;
                correct = new double[mostCommonPermutations.length];
                sepCount = new int[mostCommonPermutations.length];

                while ((data = BitextDependencyReader.getNextTrainData(depReader, intersectionReader, universalMap, maps, mostCommonPermutations, max)) != null) {
                    for (TrainData trainData : data) {
                        count++;

                        int index = trainData.index;
                        ArrayList<Object>[] features = trainData.features;

                        double bestScore = Double.NEGATIVE_INFINITY;
                        String goldLabel = trainData.goldLabel;
                        int bestLIndex = -1;
                        int goldIndex = -1;
                        double[] scores = classifier[index].scores(features);

                        int l = 0;
                        for (String label : mostCommonPermutations[index].keySet()) {
                            if (scores[l] > bestScore) {
                                bestScore = scores[l];
                                bestLIndex = l;
                            }
                            if (label.equals(goldLabel))
                                goldIndex = l;
                            l++;
                        }

                        if (goldIndex == bestLIndex)
                            correct[index]++;
                        sepCount[index]++;

                        if (count % 10000 == 0)
                            System.err.print(count + "...");
                    }
                }
            }

            System.err.print(count + "\n");
            for (int b = 0; b < mostCommonPermutations.length; b++) {
                correctPredictions = 100f * correct[b] / sepCount[b];
                System.err.print("Correct prediction " + b + ":" + correctPredictions + "\n");
            }
        }
    }

}
