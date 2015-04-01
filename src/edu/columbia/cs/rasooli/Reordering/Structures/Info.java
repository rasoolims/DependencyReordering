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
    HashMap<String,String> universalPosMap;
    int topK;
    int featLen;
    HashMap<String,int[]>[]  mostCommonPermutations;
    IndexMaps maps;
    
    public Info(Classifier[] classifier, HashMap<String,int[]>[]  mostCommonPermutations, HashMap<String,String> universalPosMap, int topK,IndexMaps maps){
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

    public Info(String modelPath, int[] tunedIterations) throws Exception {
        FileInputStream fos = new FileInputStream(modelPath);
        GZIPInputStream gz = new GZIPInputStream(fos);

        ObjectInputStream reader = new ObjectInputStream(gz);
        mostCommonPermutations = (HashMap<String,int[]>[]) reader.readObject();
        topK = (Integer) reader.readObject();
        universalPosMap = ( HashMap<String,String>) reader.readObject();
        maps = (IndexMaps) reader.readObject();
        featLen = reader.readInt();

        finalWeights = new HashMap[tunedIterations.length][];
        for (int i = 0; i < tunedIterations.length; i++) {
            int iter = tunedIterations[i];
            String mPath = modelPath + "_iter" + iter + "_len_" + i;
            fos = new FileInputStream(mPath);
            gz = new GZIPInputStream(fos);

            reader = new ObjectInputStream(gz);
            finalWeights[i] = (HashMap<Object, CompactArray>[]) reader.readObject();
        }

    }

    public void saveInitModel(String modelPath) throws Exception {
        FileOutputStream fos = new FileOutputStream(modelPath);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        ObjectOutput writer = new ObjectOutputStream(gz);
        writer.writeObject(mostCommonPermutations);
        writer.writeObject(topK);
        writer.writeObject(universalPosMap);
        writer.writeObject(maps);
        writer.writeInt(featLen);
        writer.flush();
        writer.close();

    }

    public void saveModel(String modelPath) throws Exception {
        for (int i = 0; i < finalWeights.length; i++) {
            FileOutputStream fos = new FileOutputStream(modelPath + "_len_" + i);
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutput writer = new ObjectOutputStream(gz);
            writer.writeObject(finalWeights[i]);
            writer.flush();
            writer.close();
        }
        System.err.print("done\n");
    }

    public HashMap<Object, CompactArray>[][] getFinalWeights() {
        return finalWeights;
    }

    private CompactArray getAveragedCompactArray(CompactArray ca, CompactArray aca, int iteration) {
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
