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
    HashMap<String, Double> finalWeights;
    HashMap<String,String> universalPosMap;
    int topK;
    HashMap<String,Integer> posOrderFrequencyDic;

    public Info(AveragedPerceptron perceptron,HashMap<String,Integer> posOrderFrequencyDic, HashMap<String,String> universalPosMap, int topK){
        this.universalPosMap=universalPosMap;
        this.topK=topK;
        this.posOrderFrequencyDic=posOrderFrequencyDic;

        finalWeights = new HashMap<String, Double>(perceptron.getAvgWeights().size());

        for(String feat:perceptron.getWeights().keySet()){
            double newValue = perceptron.getWeights().get(feat) - (perceptron.getAvgWeights().get(feat) / perceptron.getIteration());
            if(newValue!=0.0)
                finalWeights.put(feat,newValue);
        }
    }


    public Info(String modelPath)throws Exception {
        FileInputStream fos = new FileInputStream(modelPath);
        GZIPInputStream gz = new GZIPInputStream(fos);

        ObjectInputStream reader = new ObjectInputStream(gz);
        finalWeights = (HashMap<String, Double>) reader.readObject();
        posOrderFrequencyDic = (HashMap<String, Integer>) reader.readObject();
        topK = (Integer) reader.readObject();
        universalPosMap = ( HashMap<String,String>) reader.readObject();
    }
    
    public void saveModel(String modelPath) throws  Exception{
        FileOutputStream fos = new FileOutputStream(modelPath);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        ObjectOutput writer = new ObjectOutputStream(gz);
        writer.writeObject(finalWeights);
        writer.writeObject(posOrderFrequencyDic);
        writer.writeObject(topK);
        writer.writeObject(universalPosMap);
        writer.flush();
        writer.close();
        System.err.print("done\n");
    }

    public HashMap<String, Double> getFinalWeights() {
        return finalWeights;
    }

    public HashMap<String, String> getUniversalPosMap() {
        return universalPosMap;
    }

    public int getTopK() {
        return topK;
    }

    public HashMap<String, Integer> getPosOrderFrequencyDic() {
        return posOrderFrequencyDic;
    }
}
