package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
import edu.columbia.cs.rasooli.Reordering.Classifier.OnlinePegasos;
import edu.columbia.cs.rasooli.Reordering.IO.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.IndexMaps;
import edu.columbia.cs.rasooli.Reordering.Structures.Info;
import edu.columbia.cs.rasooli.Reordering.Structures.Pair;

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
    int pivotFeatLen;
    HashMap<String, int[]>[] mostCommonPermutations;
    HashMap<String, int[]>[] mostCommonRightPermutations;
    HashMap<String, int[]>[] mostCommonLeftPermutations;
    String trainTreePath;
    String trainIntersectionPath;
    String devIntersectionPath;
    String devTreePath;
    int topK;
    int maxLen;
    Classifier pivotClassifer;
    Classifier[] classifier;
    Classifier[] leftClassifier;
    Classifier[] rightClassifier;

    public Trainer(String trainTreePath, String trainIntersectionPath, String devTreePath, String devIntersectionPath, String universalPOSPath, int maxLen, int topK, int featLen, int pivotFeatLen) throws Exception {
        this.trainIntersectionPath = trainIntersectionPath;
        this.trainTreePath = trainTreePath;
        this.devTreePath = devTreePath;
        this.devIntersectionPath = devIntersectionPath;
        this.topK = topK;
        this.maxLen = maxLen;
        this.featLen = featLen;
        this.pivotFeatLen = pivotFeatLen;

        universalMap = BitextDependencyReader.createUniversalMap(universalPOSPath);
        maps = DependencyReader.readIndexMap(trainTreePath, universalMap);
        mostCommonPermutations = BitextDependencyReader.constructMostCommonOrderings(trainTreePath, trainIntersectionPath, universalMap, maps, maxLen, topK);
        Pair<HashMap<String, int[]>[], HashMap<String, int[]>[]> pair = BitextDependencyReader.constructMostCommonLeftRightOrderings(trainTreePath, trainIntersectionPath, universalMap, maps, maxLen, topK);
        mostCommonRightPermutations = pair.second;
        mostCommonLeftPermutations = pair.first;
    }

    public void trainWithPerceptron(int maxIter, String modelPath, boolean twoClassifier) throws Exception {
        if (!twoClassifier)
            trainWithOneClassifier(maxIter, modelPath);
        else
            trainWithTwoClassifiers(maxIter, modelPath);

    }

    public void trainWithOneClassifier(int maxIter, String modelPath) throws Exception {
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


    public void trainWithTwoClassifiers(int maxIter, String modelPath) throws Exception {
        System.err.println("Training started...");
        leftClassifier = new AveragedPerceptron[maxLen];
        rightClassifier = new AveragedPerceptron[maxLen];
        pivotClassifer = new AveragedPerceptron(1, pivotFeatLen);
        for (int i = 0; i < maxLen; i++) {
            leftClassifier[i] = new AveragedPerceptron(topK, featLen);
            rightClassifier[i] = new AveragedPerceptron(topK, featLen);
        }

        //todo
        Pair<ArrayList<PivotTrainData>, Pair<ArrayList<TrainData>, ArrayList<TrainData>>> dataPairs = BitextDependencyReader.getLeftRightTrainData(trainTreePath, trainIntersectionPath, universalMap, maps, maxLen);
        System.err.println("number of pivot instances:" + dataPairs.first.size());

        Pair<ArrayList<PivotTrainData>, Pair<ArrayList<TrainData>, ArrayList<TrainData>>> devDataPairs = null;
        if (devTreePath != "") {
            devDataPairs = BitextDependencyReader.getLeftRightTrainData(devTreePath, devIntersectionPath, universalMap, maps, maxLen);
            System.err.println("number of dev pivot instances:" + devDataPairs.first.size());
        }


        for (int i = 0; i < maxIter; i++) {
            long start = System.currentTimeMillis();
            System.err.println("\nIteration: " + (i + 1) + "...");
            int count = 0;
            double[] leftCorrect = new double[maxLen];
            double[] rightCorrect = new double[maxLen];
            double pivotCorrect = 0;
            int allPivot = dataPairs.first.size();

            int[] sepLeftCount = new int[mostCommonLeftPermutations.length];
            int[] sepRightCount = new int[mostCommonRightPermutations.length];

            //region training pivot
            System.err.println("Training pivot classifier");
            count = 0;

            for (PivotTrainData trainData : dataPairs.first) {
                double score = pivotClassifer.scores(trainData.features, false)[0];
                boolean decision = score >= 0 ? true : false;

                if (decision != trainData.isBefore) {
                    // update
                    if (trainData.isBefore) {
                        for (int f = 0; f < trainData.features.length; f++) {
                            for (Object feat : trainData.features[f]) {
                                pivotClassifer.updateWeight(0, f, feat, +1);
                            }
                        }
                    } else {
                        for (int f = 0; f < trainData.features.length; f++) {
                            for (Object feat : trainData.features[f]) {
                                pivotClassifer.updateWeight(0, f, feat, -1);
                            }
                        }

                    }
                } else pivotCorrect++;

                pivotClassifer.incrementIteration();
            }
            double correctPredictions = 100f * pivotCorrect / allPivot;
            System.err.print(">> Correct pivot prediction: " + correctPredictions + "\n");
            System.err.print("\n");
            //endregion

            //region training left
            System.err.println("Training left classifiers");
            count = 0;

            for (TrainData trainData : dataPairs.second.first) {
                count++;

                int index = trainData.index;
                ArrayList<Object>[] features = trainData.features;
                double bestScore = Double.NEGATIVE_INFINITY;
                String goldLabel = trainData.goldLabel;
                int bestLIndex = -1;
                int goldIndex = -1;
                double[] scores = leftClassifier[index].scores(features, false);

                int l = 0;
                for (String label : mostCommonLeftPermutations[index].keySet()) {
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
                            leftClassifier[index].updateWeight(goldIndex, f, feat, +1);
                            leftClassifier[index].updateWeight(bestLIndex, f, feat, -1);
                        }
                    }
                } else
                    leftCorrect[index]++;
                sepLeftCount[index]++;

                leftClassifier[index].incrementIteration();
                if (count % 10000 == 0)
                    System.err.print(count + "...");
            }
            for (int b = 0; b < mostCommonLeftPermutations.length; b++) {
                correctPredictions = 100f * leftCorrect[b] / sepLeftCount[b];
                System.err.print("Correct left prediction " + b + ":" + correctPredictions + " from " + sepLeftCount[b] + " instances \n");
            }
            System.err.print("\n");
            //endregion

            //region training right
            System.err.println("Training right classifiers");
            count = 0;
            for (TrainData trainData : dataPairs.second.second) {
                count++;

                int index = trainData.index;
                ArrayList<Object>[] features = trainData.features;
                double bestScore = Double.NEGATIVE_INFINITY;
                String goldLabel = trainData.goldLabel;
                int bestLIndex = -1;
                int goldIndex = -1;
                double[] scores = rightClassifier[index].scores(features, false);

                int l = 0;
                for (String label : mostCommonRightPermutations[index].keySet()) {
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
                            rightClassifier[index].updateWeight(goldIndex, f, feat, +1);
                            rightClassifier[index].updateWeight(bestLIndex, f, feat, -1);
                        }
                    }
                } else
                    rightCorrect[index]++;
                sepRightCount[index]++;

                rightClassifier[index].incrementIteration();
                if (count % 10000 == 0)
                    System.err.print(count + "...");
            }
            for (int b = 0; b < mostCommonRightPermutations.length; b++) {
                correctPredictions = 100f * rightCorrect[b] / sepRightCount[b];
                System.err.print("Correct right prediction " + b + ":" + correctPredictions + " from " + sepRightCount[b] + " instances\n");
            }
            //endregion

            // System.err.print(count + "\n");
            Info info = new Info(leftClassifier, rightClassifier, pivotClassifer, mostCommonLeftPermutations, mostCommonRightPermutations, universalMap, topK, maps);

            if (i == 0)
                info.saveInitModel(modelPath);

            info.saveModel(modelPath + "_iter" + (i + 1));

            long end = System.currentTimeMillis();
            long elapsed = (end - start) / 1000;
            System.err.println("time for training " + elapsed + " seconds");

            //region dev
            if (devDataPairs != null) {
                leftCorrect = new double[maxLen];
                rightCorrect = new double[maxLen];
                pivotCorrect = 0;
                allPivot = dataPairs.first.size();
                sepLeftCount = new int[mostCommonLeftPermutations.length];
                sepRightCount = new int[mostCommonRightPermutations.length];

                //region decoding with pivot
                System.err.println("Decoding with pivot classifier");
                count = 0;

                for (PivotTrainData trainData : devDataPairs.first) {
                    double score = pivotClassifer.scores(trainData.features, true)[0];
                    boolean decision = score >= 0 ? true : false;
                    if (decision == trainData.isBefore) pivotCorrect++;
                }
                correctPredictions = 100f * pivotCorrect / allPivot;
                System.err.print(">> Correct dev pivot prediction: " + correctPredictions + "\n");
                System.err.print("\n");
                //endregion

                //region training left
                System.err.println("Decoding with the left classifiers");
                count = 0;

                for (TrainData trainData : devDataPairs.second.first) {
                    count++;
                    int index = trainData.index;
                    ArrayList<Object>[] features = trainData.features;
                    double bestScore = Double.NEGATIVE_INFINITY;
                    String goldLabel = trainData.goldLabel;
                    int bestLIndex = -1;
                    int goldIndex = -1;
                    double[] scores = leftClassifier[index].scores(features, true);

                    int l = 0;
                    for (String label : mostCommonLeftPermutations[index].keySet()) {
                        if (scores[l] > bestScore) {
                            bestScore = scores[l];
                            bestLIndex = l;
                        }
                        if (label.equals(goldLabel))
                            goldIndex = l;
                        l++;
                    }

                    if (goldIndex == bestLIndex)
                        leftCorrect[index]++;
                    sepLeftCount[index]++;

                    if (count % 10000 == 0)
                        System.err.print(count + "...");
                }
                for (int b = 0; b < mostCommonLeftPermutations.length; b++) {
                    correctPredictions = 100f * leftCorrect[b] / sepLeftCount[b];
                    System.err.print("Correct dev left prediction " + b + ":" + correctPredictions + " from " + sepLeftCount[b] + " instances\n");
                }
                System.err.print("\n");
                //endregion

                //region training right
                System.err.println("Decoding with the right classifiers");
                count = 0;
                for (TrainData trainData : devDataPairs.second.second) {
                    count++;

                    int index = trainData.index;
                    ArrayList<Object>[] features = trainData.features;
                    double bestScore = Double.NEGATIVE_INFINITY;
                    String goldLabel = trainData.goldLabel;
                    int bestLIndex = -1;
                    int goldIndex = -1;
                    double[] scores = rightClassifier[index].scores(features, true);

                    int l = 0;
                    for (String label : mostCommonRightPermutations[index].keySet()) {
                        if (scores[l] > bestScore) {
                            bestScore = scores[l];
                            bestLIndex = l;
                        }
                        if (label.equals(goldLabel))
                            goldIndex = l;
                        l++;
                    }

                    if (goldIndex == bestLIndex) rightCorrect[index]++;
                    sepRightCount[index]++;

                    if (count % 10000 == 0)
                        System.err.print(count + "...");
                }
                for (int b = 0; b < mostCommonRightPermutations.length; b++) {
                    correctPredictions = 100f * rightCorrect[b] / sepRightCount[b];
                    System.err.print("Correct dev right prediction " + b + ":" + correctPredictions + " from " + sepRightCount[b] + " instances\n");
                }
                //endregion
            }
            //endregion
        }
    }


    public void trainWithPegasos(int maxIter, String modelPath, double lambda) throws Exception {
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
