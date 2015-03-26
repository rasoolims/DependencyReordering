package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Decoding.ScoringThread;
import edu.columbia.cs.rasooli.Reordering.IO.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.IO.DependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/17/15
 * Time: 12:41 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class Trainer {
    public static void trainWithPerceptron(String trainTreePath, String trainIntersectionPath, String devTreePath, String devIntersectionPath, String universalPOSPath ,AveragedPerceptron classifier , int maxIter, String modelPath, int topK, int numOfThreads) throws Exception {
        System.err.println("Training started...");
        HashMap<String,String> universalMap= BitextDependencyReader.createUniversalMap(universalPOSPath);
        IndexMaps maps= DependencyReader.readIndexMap(trainTreePath, universalMap);
        HashMap<String,Integer> posOrderFrequencyDic=BitextDependencyReader.constructPosOrderFrequency(trainTreePath,trainIntersectionPath,universalMap,maps) ;

        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        CompletionService<FeaturedInstance> pool = new ExecutorCompletionService<FeaturedInstance>(executor);

        for(int i=0;i<maxIter;i++) {
            long start=System.currentTimeMillis();
            System.err.println("\nIteration: "+(i+1)+"...");
            int count=0;
            float correct=0;
            BufferedReader treeReader=new BufferedReader(new FileReader(trainTreePath));
            BufferedReader intersectionReader=new BufferedReader(new FileReader(trainIntersectionPath));

            BitextDependency bitextDependency;
            while((bitextDependency=BitextDependencyReader.readNextBitextDependency(treeReader,intersectionReader,universalMap,maps))!=null){
               try {
                   for (TrainData trainData : bitextDependency.getAllPossibleTrainData(posOrderFrequencyDic, topK)) {
                       double bestScore = Double.NEGATIVE_INFINITY;
                       ContextInstance bestCandidate = null;
                       ArrayList<Object>[] bestFeats = null;
                      HashSet<ContextInstance> candidates= trainData.originalInstance.getPossibleContexts(posOrderFrequencyDic, topK);
                       candidates.add(trainData.getGoldInstance());
                       
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
                               bestFeats = featuredInstance.getFeatures(); 
                           }
                       }

                       // perceptron update
                       if (!bestCandidate.equals(trainData.getGoldInstance())) {
                           for (int k = 0; k < bestFeats.length; k++) {
                               HashMap<Object, Integer> changedFeats = new HashMap<Object, Integer>();
                               for (Object feat : trainData.goldFeatures[k])
                                   changedFeats.put(feat, 1);
                               for (Object feat : bestFeats[k]) {
                                   if (changedFeats.containsKey(feat))
                                       changedFeats.put(feat, changedFeats.get(feat) - 1);
                                   else
                                       changedFeats.put(feat, -1);
                               }
                               for (Object feat : changedFeats.keySet()) {
                                   int change = changedFeats.get(feat);
                                   if (change != 0) {
                                       classifier.updateWeight(k, feat, change);
                                   }
                               }
                           }
                       } else
                           correct++;

                       classifier.incrementIteration();

                       count++;
                       if (count % 10000 == 0)
                           System.err.print(count + "...");
                   }
               }catch (Exception ex){
                   System.out.print("");
               }
            }
            System.err.print(count+"\n");
            float correctPredictions =100f*correct/count;
            System.err.print("Correct prediction: "+correctPredictions+"\n");

            Info info=new Info(classifier,posOrderFrequencyDic,universalMap,topK,maps);
            info.saveModel(modelPath + "_iter" + (i + 1));
            long end=System.currentTimeMillis();
            long elapsed=(end-start)/1000;
            System.err.println("time for training "+elapsed + " seconds");

            if(!devTreePath.equals("")) {
                count = 0;
                correct = 0;
                AveragedPerceptron decodeClassifier = new AveragedPerceptron(classifier.getWeights().length);
                decodeClassifier.setAvgWeights(info.getFinalWeights());
                System.err.print("decoding classifier size: " + decodeClassifier.size() + "\n");

                BufferedReader devTreeReader = new BufferedReader(new FileReader(devTreePath));
                BufferedReader devIntersectionReader = new BufferedReader(new FileReader(devIntersectionPath));
                while ((bitextDependency = BitextDependencyReader.readNextBitextDependency(devTreeReader, devIntersectionReader, universalMap,maps)) != null) {
                    try {
                    for (TrainData data : bitextDependency.getAllPossibleTrainData(posOrderFrequencyDic, topK)) {
                        double bestScore = Double.NEGATIVE_INFINITY;
                        ContextInstance bestCandidate = null;

                        HashSet<ContextInstance> candidates= data.originalInstance.getPossibleContexts(posOrderFrequencyDic, topK);

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
                        
                        if (bestCandidate.equals(data.getGoldInstance()))
                            correct++;
                        count++;
                        if (count % 10000 == 0)
                            System.err.print(count + "...");
                    }
                    } catch (Exception ex) {

                    }

                }


                System.err.print(count + "\n");
                correctPredictions = 100f * correct / count;
                System.err.print("Correct  dev prediction: " + correctPredictions + "\n");
            }
        }
        
        executor.shutdown();
    }
}
