package edu.columbia.cs.rasooli.Reordering.Structures;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;

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
    HashMap<Object, Double>[][][] finalWeights;
    HashMap<String,String> universalPosMap;
    int topK;
    int featLen;
    HashMap<String,int[]>[]  mostCommonPermutations;
    IndexMaps maps;
    
    public Info(AveragedPerceptron[] perceptron, HashMap<String,int[]>[]  mostCommonPermutations, HashMap<String,String> universalPosMap, int topK,IndexMaps maps){
        this.universalPosMap=universalPosMap;
        this.topK=topK;
        this.mostCommonPermutations=mostCommonPermutations;
        this.maps=maps;

        finalWeights = new HashMap[perceptron.length][][];
        this.featLen= perceptron[0].getWeights()[0].length;
        
        for(int f=0;f<finalWeights.length;f++) {
           finalWeights[f]= new HashMap[perceptron[f].getWeights().length][perceptron[f].getWeights()[0].length];
            for (int i = 0; i < perceptron[f].getWeights().length; i++) {
                for (int j = 0; j < perceptron[f].getWeights()[i].length; j++) {
                    finalWeights[f][i][j] = new HashMap<Object, Double>();
                    for (Object feat : perceptron[f].getWeights()[i][j].keySet()) {
                        double newValue = perceptron[f].getWeights()[i][j].get(feat) - (perceptron[f].getAvgWeights()[i][j].get(feat) / perceptron[f].getIteration());
                        if (newValue != 0.0)
                            finalWeights[f][i][j].put(feat, newValue);
                    }
                }
            }
        }
    }


    public Info(String modelPath)throws Exception {
        FileInputStream fos = new FileInputStream(modelPath);
        GZIPInputStream gz = new GZIPInputStream(fos);

        ObjectInputStream reader = new ObjectInputStream(gz);
        finalWeights = (HashMap<Object, Double>[][][]) reader.readObject();
        mostCommonPermutations = (HashMap<String,int[]>[]) reader.readObject();
        topK = (Integer) reader.readObject();
        universalPosMap = ( HashMap<String,String>) reader.readObject();
        maps = (IndexMaps) reader.readObject();
    }
    
    public void saveModel(String modelPath) throws  Exception{
        FileOutputStream fos = new FileOutputStream(modelPath);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        ObjectOutput writer = new ObjectOutputStream(gz);
        writer.writeObject(finalWeights);
        writer.writeObject(mostCommonPermutations);
        writer.writeObject(topK);
        writer.writeObject(universalPosMap);
        writer.writeObject(maps);
        writer.flush();
        writer.close();
        System.err.print("done\n");
    }

    public HashMap<Object, Double>[][][] getFinalWeights() {
        return finalWeights;
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
