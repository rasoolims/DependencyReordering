package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
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
    AveragedPerceptron[] classifier;

    public Trainer(String trainTreePath, String trainIntersectionPath, String devTreePath, String devIntersectionPath, String universalPOSPath, String trainPath, String devPath, int maxLen, int topK, int featLen) throws Exception {
        this.trainIntersectionPath = trainIntersectionPath;
        this.trainTreePath = trainTreePath;
        this.devTreePath = devTreePath;
        this.devIntersectionPath = devIntersectionPath;
        this.topK = topK;
        this.maxLen = maxLen;
        this.featLen = featLen;
        classifier = new AveragedPerceptron[maxLen];
        for (int i = 0; i < maxLen; i++)
            classifier[i] = new AveragedPerceptron(topK, featLen);

        universalMap = BitextDependencyReader.createUniversalMap(universalPOSPath);
        maps = DependencyReader.readIndexMap(trainTreePath, universalMap);
        mostCommonPermutations = BitextDependencyReader.constructMostCommonOrderings(trainTreePath, trainIntersectionPath, universalMap, maps, maxLen, topK);
    }

    public void trainWithPerceptron(int maxIter, String modelPath, int numOfThreads) throws Exception {
        System.err.println("Training started...");

        int max = 200000;

        for (int i = 0; i < maxIter; i++) {
            long start = System.currentTimeMillis();
            System.err.println("\nIteration: " + (i + 1) + "...");
            int count = 0;
            float correct = 0;

            BufferedReader depReader = new BufferedReader(new FileReader(trainTreePath));
            BufferedReader intersectionReader = new BufferedReader(new FileReader(trainIntersectionPath));
            ArrayList<TrainData> data;

            while ((data = BitextDependencyReader.getNextTrainData(depReader, intersectionReader, universalMap, maps, mostCommonPermutations, max)) != null) {
                for (TrainData trainData : data) {
                    count++;

                    int index = trainData.index;
                    ArrayList<Object>[] features = trainData.features;
                    float bestScore = Float.NEGATIVE_INFINITY;
                    String goldLabel = trainData.goldLabel;
                    int bestLIndex = -1;
                    int goldIndex = -1;
                    float[] scores = classifier[index].scores(features, false);

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
                    } else correct++;

                    classifier[index].incrementIteration();
                    if (count % 10000 == 0)
                        System.err.print(count + "...");
                }
            }
            System.err.print(count + "\n");
            float correctPredictions = 100f * correct / count;
            System.err.print("Correct prediction: " + correctPredictions + "\n");
            Info info = new Info(classifier, mostCommonPermutations, universalMap, topK, maps);

            info.saveModel(modelPath + "_iter" + (i + 1));
            long end = System.currentTimeMillis();
            long elapsed = (end - start) / 1000;
            System.err.println("time for training " + elapsed + " seconds");


            if (!devTreePath.equals("")) {
                depReader = new BufferedReader(new FileReader(devTreePath));
                intersectionReader = new BufferedReader(new FileReader(devIntersectionPath));

                count = 0;
                correct = 0;

                while ((data = BitextDependencyReader.getNextTrainData(depReader, intersectionReader, universalMap, maps, mostCommonPermutations, max)) != null) {
                    for (TrainData trainData : data) {
                        count++;

                        int index = trainData.index;
                        ArrayList<Object>[] features = trainData.features;

                        float bestScore = Float.NEGATIVE_INFINITY;
                        String goldLabel = trainData.goldLabel;
                        int bestLIndex = -1;
                        int goldIndex = -1;
                        float[] scores = classifier[index].decScores(features);

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
                        
                        if (goldIndex == bestLIndex) {
                            correct++;
                        }

                        if (count % 10000 == 0)
                            System.err.print(count + "...");
                    }
                }
            }

            System.err.print(count + "\n");
            correctPredictions = 100f * correct / count;
            System.err.print("Correct  dev prediction: " + correctPredictions + "\n");
        }
    }
}
