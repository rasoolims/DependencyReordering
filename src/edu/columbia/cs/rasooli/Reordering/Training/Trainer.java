package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
import edu.columbia.cs.rasooli.Reordering.FileManagement.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;

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
    public static void trainWithPerceptron(ArrayList<TrainData> trainingData, ArrayList<TrainData> devData, AveragedPerceptron classifier , int maxIter, String modelPath, HashMap<String,Integer> posOrderFrequencyDic, int topK) throws Exception {
        System.err.println("Training started: "+trainingData.size()+" training instance and "+devData.size()+" dev instances ...");
        
        for(int i=0;i<maxIter;i++) {
            System.err.println("Iteration: "+(i+1)+"...");
            int count=0;
             float correct=0;
            
            for (TrainData trainData : trainingData) {
               float bestScore = Float.NEGATIVE_INFINITY;
               ContextInstance bestCandidate = null;
                ArrayList<String>   bestFeats=null;
                
               for (ContextInstance candidate : trainData.originalInstance.getPossibleContexts(posOrderFrequencyDic,topK)) {
                   ArrayList<String>   features = candidate.extractMainFeatures();
                   float score = classifier.score(features, false);
                   if (score > bestScore) {
                       bestScore = score;
                       bestCandidate = candidate;
                       bestFeats=features;
                   }
               }

               // perceptron update
               if (!bestCandidate.equals(trainData.getGoldInstance())) {
                   HashMap<String, Integer> changedFeats = new HashMap<String, Integer>();
                   for (String feat : trainData.goldFeatures)
                       changedFeats.put(feat, 1);
                   for (String feat : bestFeats) {
                       if (changedFeats.containsKey(feat))
                           changedFeats.put(feat, changedFeats.get(feat) - 1);
                       else
                           changedFeats.put(feat, -1);
                   }
                   for (String feat : changedFeats.keySet()) {
                       int change = changedFeats.get(feat);
                       if (change != 0) {
                           classifier.updateWeight(feat, change);
                       }
                   }
               }  else
               correct++;
                
               classifier.incrementIteration();
                
                count++;
                if(count%1000==0)
                    System.err.print(count+"...");
           }
            System.err.print(count+"\n");
            float correctPredictions =100f*correct/count;
            System.err.print("Correct prediction: "+correctPredictions+"\n");
            
            classifier.saveModel(modelPath+"_iter"+(i+1));

            count=0;
            correct=0;
            Classifier decodeClassifier= AveragedPerceptron.loadModel(modelPath+"_iter"+(i+1)) ;
            System.err.print("decoding classifier size: "+decodeClassifier.size()+"\n");
            for (TrainData data : devData) {
                float bestScore = Float.NEGATIVE_INFINITY;
                ContextInstance bestCandidate = null;

                for (ContextInstance candidate : data.originalInstance.getPossibleContexts(posOrderFrequencyDic,topK)) {
                    ArrayList<String> features = candidate.extractMainFeatures();
                    float score = decodeClassifier.score(features, true);
                    if (score > bestScore) {
                        bestScore = score;
                        bestCandidate = candidate;
                    }
                }
                if (bestCandidate.equals(data.getGoldInstance()))
                    correct++;
                count++;
                if(count%1000==0)
                    System.err.print(count+"...");
            }
            System.err.print(count+"\n");
            correctPredictions =100f*correct/count;
            System.err.print("Correct  dev prediction: "+correctPredictions+"\n");
        }
    }


    public static void trainWithPerceptron(String trainTreePath, String trainIntersectionPath, String devTreePath, String devIntersectionPath, String universalPOSPath ,AveragedPerceptron classifier , int maxIter, String modelPath, int topK) throws Exception {
        System.err.println("Training started...");
        HashMap<String,String> universalMap= BitextDependencyReader.createUniversalMap(universalPOSPath);
        HashMap<String,Integer> posOrderFrequencyDic=BitextDependencyReader.constructPosOrderFrequency(trainTreePath,trainIntersectionPath,universalMap) ;
        for(int i=0;i<maxIter;i++) {
            System.err.println("Iteration: "+(i+1)+"...");
            int count=0;
            float correct=0;
            BufferedReader treeReader=new BufferedReader(new FileReader(trainTreePath));
            BufferedReader intersectionReader=new BufferedReader(new FileReader(trainIntersectionPath));

            BitextDependency bitextDependency;
            while((bitextDependency=BitextDependencyReader.readNextBitextDependency(treeReader,intersectionReader,universalMap))!=null){
                for(TrainData trainData:bitextDependency.getAllPossibleTrainData(posOrderFrequencyDic,topK)){
                    float bestScore = Float.NEGATIVE_INFINITY;
                    ContextInstance bestCandidate = null;
                    ArrayList<String>   bestFeats=null;

                    for (ContextInstance candidate : trainData.originalInstance.getPossibleContexts(posOrderFrequencyDic,topK)) {
                        ArrayList<String>   features = candidate.extractMainFeatures();
                        float score = classifier.score(features, false);
                        if (score > bestScore) {
                            bestScore = score;
                            bestCandidate = candidate;
                            bestFeats=features;
                        }
                    }

                    // perceptron update
                    if (!bestCandidate.equals(trainData.getGoldInstance())) {
                        HashMap<String, Integer> changedFeats = new HashMap<String, Integer>();
                        for (String feat : trainData.goldFeatures)
                            changedFeats.put(feat, 1);
                        for (String feat : bestFeats) {
                            if (changedFeats.containsKey(feat))
                                changedFeats.put(feat, changedFeats.get(feat) - 1);
                            else
                                changedFeats.put(feat, -1);
                        }
                        for (String feat : changedFeats.keySet()) {
                            int change = changedFeats.get(feat);
                            if (change != 0) {
                                classifier.updateWeight(feat, change);
                            }
                        }
                    }  else
                        correct++;

                    classifier.incrementIteration();

                    count++;
                    if(count%1000==0)
                        System.err.print(count+"...");  
                }
            }
            System.err.print(count+"\n");
            float correctPredictions =100f*correct/count;
            System.err.print("Correct prediction: "+correctPredictions+"\n");

            classifier.saveModel(modelPath+"_iter"+(i+1));

            count=0;
            correct=0;
            Classifier decodeClassifier= AveragedPerceptron.loadModel(modelPath+"_iter"+(i+1)) ;
            System.err.print("decoding classifier size: "+decodeClassifier.size()+"\n");


            BufferedReader devTreeReader=new BufferedReader(new FileReader(devTreePath));
            BufferedReader devIntersectionReader=new BufferedReader(new FileReader(devIntersectionPath));
            while((bitextDependency=BitextDependencyReader.readNextBitextDependency(devTreeReader,devIntersectionReader,universalMap))!=null){
                for(TrainData data:bitextDependency.getAllPossibleTrainData(posOrderFrequencyDic,topK)){
                    float bestScore = Float.NEGATIVE_INFINITY;
                    ContextInstance bestCandidate = null;

                    for (ContextInstance candidate : data.originalInstance.getPossibleContexts(posOrderFrequencyDic,topK)) {
                        ArrayList<String> features = candidate.extractMainFeatures();
                        float score = decodeClassifier.score(features, true);
                        if (score > bestScore) {
                            bestScore = score;
                            bestCandidate = candidate;
                        }
                    }
                    if (bestCandidate.equals(data.getGoldInstance()))
                        correct++;
                    count++;
                    if(count%1000==0)
                        System.err.print(count+"...");
                }
            }

            System.err.print(count+"\n");
            correctPredictions =100f*correct/count;
            System.err.print("Correct  dev prediction: "+correctPredictions+"\n");
        }
    }

}
