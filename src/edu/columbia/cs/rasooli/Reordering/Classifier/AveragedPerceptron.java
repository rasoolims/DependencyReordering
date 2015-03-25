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
    HashMap<String, Float> weights;
    HashMap<String, Float> avgWeights;

    public AveragedPerceptron() {
        super();
        weights = new HashMap<String, Float>();
        avgWeights = new HashMap<String, Float>();
    }

    @Override
    public void updateWeight(String feature, float change){
        if(!weights.containsKey(feature)){
            weights.put(feature,change);
        }  else{
            weights.put(feature,weights.get(feature)+change);
        }

        if(!avgWeights.containsKey(feature)){
            avgWeights.put(feature,iteration*change);
        }  else{
            avgWeights.put(feature,avgWeights.get(feature)+iteration*change);
        }
    }

    @Override
    public void saveModel(String modelPath) throws  Exception{
        System.err.print("Writing model to the file...");
        HashMap<String, Float> finalAverageWeight=new  HashMap<String, Float>(avgWeights.size());

        for(String feat:weights.keySet()){
            float newValue=  weights.get(feat)-(avgWeights.get(feat)/iteration);
            if(newValue!=0.0)
                finalAverageWeight.put(feat,newValue);
        }
        ObjectOutput writer = new ObjectOutputStream(new FileOutputStream(modelPath));
        writer.writeObject(finalAverageWeight);
        writer.flush();
        writer.close();
        System.err.print("done\n");
    }

    public static Classifier loadModel(String modelPath) throws  Exception {
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(modelPath));
        HashMap<String, Float> avgWeights= (HashMap<String, Float>)reader.readObject();

        AveragedPerceptron averagedPerceptron=new AveragedPerceptron();
        averagedPerceptron.avgWeights=avgWeights;

        return averagedPerceptron;
    }

    @Override
    public float score(ArrayList<String> features,boolean decode){
        float score=0.0f;
        HashMap<String,Float> map;
        if(decode)
            map= avgWeights;
        else
            map= weights;
        for(String feature:features){
            if(map.containsKey(feature))
                score+=map.get(feature);
        }
        return score;
    }

    @Override
    public int size(){
        return avgWeights.size();
    }

    public HashMap<String, Float> getWeights() {
        return weights;
    }

    public HashMap<String, Float> getAvgWeights() {
        return avgWeights;
    }

    public void setAvgWeights(HashMap<String, Float> avgWeights) {
        this.avgWeights = avgWeights;
    }
}