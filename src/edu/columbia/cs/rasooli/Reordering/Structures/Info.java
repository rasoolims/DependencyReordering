package edu.columbia.cs.rasooli.Reordering.Structures;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Classifier.Classifier;
import edu.columbia.cs.rasooli.Reordering.Classifier.OnlinePegasos;
import edu.columbia.cs.rasooli.Reordering.Enums.ClassifierType;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/24/15
 * Time: 10:44 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class Info {
    HashMap<Object, CompactArray>[][] finalWeights;
    HashMap<Object, CompactArray>[][] finalLeftWeights;
    HashMap<Object, CompactArray>[][] finalRightWeights;
    HashMap<Object, CompactArray>[] pivotWeights;
    HashMap<String,String> universalPosMap;
    int topK;
    int featLen;
    HashMap<String,int[]>[]  mostCommonPermutations;
    HashMap<String, int[]>[] mostLeftCommonPermutations;
    HashMap<String, int[]>[] mostRightCommonPermutations;
    IndexMaps maps;
    boolean twoClassifer;
    
    public Info(Classifier[] classifier, HashMap<String,int[]>[]  mostCommonPermutations, HashMap<String,String> universalPosMap, int topK,IndexMaps maps) throws Exception {
        twoClassifer = false;
        finalLeftWeights = new HashMap[0][0];
        finalRightWeights = new HashMap[0][0];
        pivotWeights = new HashMap[0];
        mostLeftCommonPermutations = new HashMap[0];
        mostRightCommonPermutations = new HashMap[0];

        this.universalPosMap=universalPosMap;
        this.topK=topK;
        this.mostCommonPermutations=mostCommonPermutations;
        this.maps=maps;

        
        finalWeights = new HashMap[classifier.length][];
        this.featLen = classifier[0].featLen();
        
        for(int f=0;f<finalWeights.length;f++) {
            if(classifier[f].getType()== ClassifierType.pegasos){
                finalWeights[f]=((OnlinePegasos)classifier[f]).getWeights();
            }else  if(classifier[f].getType()== ClassifierType.perceptron) {
                finalWeights[f] = new HashMap[classifier[f].featLen()];
                for (int i = 0; i < classifier[f].featLen(); i++) {
                    finalWeights[f][i] = new HashMap<Object, CompactArray>();
                    for (Object feat : ((AveragedPerceptron)classifier[f]).getWeights()[i].keySet()) {
                        CompactArray vals = ((AveragedPerceptron)classifier[f]).getWeights()[i].get(feat);
                        CompactArray avgVals = ((AveragedPerceptron)classifier[f]).getAvgWeights()[i].get(feat);
                        finalWeights[f][i].put(feat, getAveragedCompactArray(vals, avgVals, classifier[f].getIteration()));
                    }
                }
            }
        }
    }


    public Info(Classifier[] leftClassifier, Classifier[] rightClassifier, Classifier pivotClassifier, HashMap<String, int[]>[] mostLeftCommonPermutations, HashMap<String, int[]>[] mostRightCommonPermutations, HashMap<String, String> universalPosMap, int topK, IndexMaps maps) throws Exception {
        twoClassifer = true;
        finalWeights = new HashMap[0][0];
        mostCommonPermutations = new HashMap[0];

        this.universalPosMap = universalPosMap;
        this.topK = topK;
        this.mostLeftCommonPermutations = mostLeftCommonPermutations;
        this.mostRightCommonPermutations = mostRightCommonPermutations;
        this.maps = maps;


        finalLeftWeights = new HashMap[leftClassifier.length][];
        this.featLen = leftClassifier[0].featLen();
        for (int f = 0; f < finalLeftWeights.length; f++) {
            if (leftClassifier[f].getType() == ClassifierType.pegasos) {
                finalLeftWeights[f] = ((OnlinePegasos) leftClassifier[f]).getWeights();
            } else if (leftClassifier[f].getType() == ClassifierType.perceptron) {
                finalLeftWeights[f] = new HashMap[leftClassifier[f].featLen()];
                for (int i = 0; i < leftClassifier[f].featLen(); i++) {
                    finalLeftWeights[f][i] = new HashMap<Object, CompactArray>();
                    for (Object feat : ((AveragedPerceptron) leftClassifier[f]).getWeights()[i].keySet()) {
                        CompactArray vals = ((AveragedPerceptron) leftClassifier[f]).getWeights()[i].get(feat);
                        CompactArray avgVals = ((AveragedPerceptron) leftClassifier[f]).getAvgWeights()[i].get(feat);
                        finalLeftWeights[f][i].put(feat, getAveragedCompactArray(vals, avgVals, leftClassifier[f].getIteration()));
                    }
                }
            }
        }

        finalRightWeights = new HashMap[rightClassifier.length][];
        this.featLen = rightClassifier[0].featLen();
        for (int f = 0; f < finalRightWeights.length; f++) {
            if (rightClassifier[f].getType() == ClassifierType.pegasos) {
                finalRightWeights[f] = ((OnlinePegasos) rightClassifier[f]).getWeights();
            } else if (rightClassifier[f].getType() == ClassifierType.perceptron) {
                finalRightWeights[f] = new HashMap[rightClassifier[f].featLen()];
                for (int i = 0; i < rightClassifier[f].featLen(); i++) {
                    finalRightWeights[f][i] = new HashMap<Object, CompactArray>();
                    for (Object feat : ((AveragedPerceptron) rightClassifier[f]).getWeights()[i].keySet()) {
                        CompactArray vals = ((AveragedPerceptron) rightClassifier[f]).getWeights()[i].get(feat);
                        CompactArray avgVals = ((AveragedPerceptron) rightClassifier[f]).getAvgWeights()[i].get(feat);
                        finalRightWeights[f][i].put(feat, getAveragedCompactArray(vals, avgVals, rightClassifier[f].getIteration()));
                    }
                }
            }
        }

        pivotWeights = new HashMap[pivotClassifier.featLen()];
        if (pivotClassifier.getType() == ClassifierType.pegasos) {
            pivotWeights = ((OnlinePegasos) pivotClassifier).getWeights();
        } else if (pivotClassifier.getType() == ClassifierType.perceptron) {
            pivotWeights = new HashMap[pivotClassifier.featLen()];
            for (int i = 0; i < pivotClassifier.featLen(); i++) {
                pivotWeights[i] = new HashMap<Object, CompactArray>();
                for (Object feat : ((AveragedPerceptron) pivotClassifier).getWeights()[i].keySet()) {
                    CompactArray vals = ((AveragedPerceptron) pivotClassifier).getWeights()[i].get(feat);
                    CompactArray avgVals = ((AveragedPerceptron) pivotClassifier).getAvgWeights()[i].get(feat);
                    pivotWeights[i].put(feat, getAveragedCompactArray(vals, avgVals, pivotClassifier.getIteration()));
                }
            }

        }

    }


    public Info(String modelPath, int[] tunedIterations) throws Exception {
        FileInputStream fos = new FileInputStream(modelPath);
        GZIPInputStream gz = new GZIPInputStream(fos);

        ObjectInputStream reader = new ObjectInputStream(gz);
        twoClassifer = reader.readBoolean();
        mostCommonPermutations = (HashMap<String, int[]>[]) reader.readObject();
        mostLeftCommonPermutations = (HashMap<String, int[]>[]) reader.readObject();
        mostRightCommonPermutations = (HashMap<String, int[]>[]) reader.readObject();
        topK = (Integer) reader.readObject();
        universalPosMap = (HashMap<String, String>) reader.readObject();
        maps = (IndexMaps) reader.readObject();
        featLen = reader.readInt();

        if (twoClassifer) {
            finalWeights = new HashMap[tunedIterations.length][];
            for (int i = 0; i < tunedIterations.length; i++) {
                int iter = tunedIterations[i];
                String mPath = modelPath + "_iter" + iter + "_len_" + i;
                fos = new FileInputStream(mPath);
                gz = new GZIPInputStream(fos);

                reader = new ObjectInputStream(gz);
                finalWeights[i] = (HashMap<Object, CompactArray>[]) reader.readObject();
            }
        } else {
            int arrLen = (tunedIterations.length - 1) / 2;

            int iter = tunedIterations[0];
            String mPath = modelPath + "_iter" + iter + "_pivot";
            fos = new FileInputStream(mPath);
            gz = new GZIPInputStream(fos);
            reader = new ObjectInputStream(gz);
            pivotWeights = (HashMap<Object, CompactArray>[]) reader.readObject();


            finalLeftWeights = new HashMap[arrLen][];
            for (int i = 1; i < arrLen + 1; i++) {
                iter = tunedIterations[i];
                mPath = modelPath + "_iter" + iter + "_l_len_" + (i - 1);
                fos = new FileInputStream(mPath);
                gz = new GZIPInputStream(fos);

                reader = new ObjectInputStream(gz);
                finalLeftWeights[i] = (HashMap<Object, CompactArray>[]) reader.readObject();
            }

            finalRightWeights = new HashMap[arrLen][];
            for (int i = arrLen + 1; i < tunedIterations.length; i++) {
                iter = tunedIterations[i];
                mPath = modelPath + "_iter" + iter + "_r_len_" + (i - arrLen - 1);
                fos = new FileInputStream(mPath);
                gz = new GZIPInputStream(fos);

                reader = new ObjectInputStream(gz);
                finalRightWeights[i] = (HashMap<Object, CompactArray>[]) reader.readObject();
            }
        }

    }

    public void saveInitModel(String modelPath) throws Exception {
        FileOutputStream fos = new FileOutputStream(modelPath);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        ObjectOutput writer = new ObjectOutputStream(gz);
        writer.writeBoolean(twoClassifer);
        writer.writeObject(mostCommonPermutations);
        writer.writeObject(mostLeftCommonPermutations);
        writer.writeObject(mostRightCommonPermutations);
        writer.writeObject(topK);
        writer.writeObject(universalPosMap);
        writer.writeObject(maps);
        writer.writeInt(featLen);
        writer.flush();
        writer.close();

    }

    public void saveModel(String modelPath) throws Exception {
        if (!twoClassifer) {
            for (int i = 0; i < finalWeights.length; i++) {
                FileOutputStream fos = new FileOutputStream(modelPath + "_len_" + i);
                GZIPOutputStream gz = new GZIPOutputStream(fos);
                ObjectOutput writer = new ObjectOutputStream(gz);
                writer.writeObject(finalWeights[i]);
                writer.flush();
                writer.close();
            }
        } else {
            for (int i = 0; i < finalLeftWeights.length; i++) {
                FileOutputStream fos = new FileOutputStream(modelPath + "_l_len_" + i);
                GZIPOutputStream gz = new GZIPOutputStream(fos);
                ObjectOutput writer = new ObjectOutputStream(gz);
                writer.writeObject(finalLeftWeights[i]);
                writer.flush();
                writer.close();
            }

            for (int i = 0; i < finalRightWeights.length; i++) {
                FileOutputStream fos = new FileOutputStream(modelPath + "_r_len_" + i);
                GZIPOutputStream gz = new GZIPOutputStream(fos);
                ObjectOutput writer = new ObjectOutputStream(gz);
                writer.writeObject(finalRightWeights[i]);
                writer.flush();
                writer.close();
            }

            FileOutputStream fos = new FileOutputStream(modelPath + "_pivot");
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutput writer = new ObjectOutputStream(gz);
            writer.writeObject(pivotWeights);
            writer.flush();
            writer.close();
        }
        System.err.print("done\n");
    }

    public HashMap<Object, CompactArray>[][] getFinalWeights() {
        return finalWeights;
    }

    private CompactArray getAveragedCompactArray(CompactArray ca, CompactArray aca, int iteration) throws Exception{
        int offset = ca.getOffset();
        double[] a = ca.getArray();
        double[] aa = aca.getArray();
        double[] aNew = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            aNew[i] = a[i] - (aa[i] / iteration);
        }
        return new CompactArray(offset, aNew);
    }

    public HashMap<String, String> getUniversalPosMap() {
        return universalPosMap;
    }

    public int getTopK() {
        return topK;
    }

    public IndexMaps getMaps() {
        return maps;
    }

    public HashMap<String, int[]>[] getMostCommonPermutations() {
        return mostCommonPermutations;
    }

    public int getFeatLen() {
        return featLen;
    }
}
