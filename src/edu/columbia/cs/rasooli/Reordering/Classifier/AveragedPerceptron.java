package edu.columbia.cs.rasooli.Reordering.Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/18/15
 * Time: 11:15 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class AveragedPerceptron extends Classifier implements Serializable {
    HashMap<Object, Double>[] weights;
    HashMap<Object, Double>[] avgWeights;

    public AveragedPerceptron(int size) {
        super();
        weights = new HashMap[size];
        avgWeights = new HashMap[size];
        for (int i=0;i<size;i++){
            weights[i]=new HashMap<Object, Double>();
            avgWeights[i]=new HashMap<Object, Double>();
        }
    }

    public static Classifier loadModel(String modelPath) throws  Exception {
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(modelPath));
        HashMap<Object, Double>[]  avgWeights = (HashMap[]) reader.readObject();

        AveragedPerceptron averagedPerceptron=new AveragedPerceptron(avgWeights.length);
        averagedPerceptron.avgWeights=avgWeights;

        return averagedPerceptron;
    }

    public void updateWeight(int slot, Object feature, double change) {
        if(!weights[slot].containsKey(feature)){
            weights[slot].put(feature, change);
        }  else{
            weights[slot].put(feature, weights[slot].get(feature) + change);
        }

        if(!avgWeights[slot].containsKey(feature)){
            avgWeights[slot].put(feature, iteration * change);
        }  else{
            avgWeights[slot].put(feature, avgWeights[slot].get(feature) + iteration * change);
        }
    }

    @Override
    public void saveModel(String modelPath) throws  Exception{
        System.err.print("Writing model to the file...");
        HashMap<Object, Double>[] finalAverageWeight = new HashMap[weights.length];
        
        for(int i=0;i<weights.length;i++) {
            finalAverageWeight[i]=new HashMap<Object, Double>();
            for (Object feat : weights[i].keySet()) {
                double newValue = weights[i].get(feat) - (avgWeights[i].get(feat) / iteration);
                if (newValue != 0.0)
                    finalAverageWeight[i].put(feat, newValue);
            }
        }
        ObjectOutput writer = new ObjectOutputStream(new FileOutputStream(modelPath));
        writer.writeObject(finalAverageWeight);
        writer.flush();
        writer.close();
        System.err.print("done\n");
    }

    public float score(ArrayList<Object>[] features,boolean decode){
        float score=0.0f;
        HashMap<Object, Double>[] map;
        if(decode)
            map= avgWeights;
        else
            map= weights;
        for(int i=0;i<features.length;i++) {
            for (Object feature : features[i]) {
                if (map[i].containsKey(feature))
                    score += map[i].get(feature);
            }
        }
        return score;
    }

    @Override
    public int size(){
        int size=0;
        for(int i=0;i<avgWeights.length;i++)
            size+=avgWeights[i].size();
        return size;
    }

    public HashMap<Object, Double>[] getWeights() {
        return weights;
    }

    public HashMap<Object, Double>[] getAvgWeights() {
        return avgWeights;
    }

    public void setAvgWeights(HashMap<Object, Double>[] avgWeights) {
        this.avgWeights = avgWeights;
    }
}