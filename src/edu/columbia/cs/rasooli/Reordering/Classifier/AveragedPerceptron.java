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
    HashMap<Object, Double>[][] weights;
    HashMap<Object, Double>[][] avgWeights;

    public AveragedPerceptron(int labelSize,int size) {
        super();
        weights = new HashMap[labelSize][size];
        avgWeights = new HashMap[labelSize][size];
        for (int i=0;i<labelSize;i++) {
            for (int j = 0; j < size; j++) {
                weights[i][j] = new HashMap<Object, Double>();
                avgWeights[i][j] = new HashMap<Object, Double>();
            }
        }
    }

    public static Classifier loadModel(String modelPath) throws  Exception {
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(modelPath));
        HashMap<Object, Double>[][]  avgWeights = (HashMap[][]) reader.readObject();
        AveragedPerceptron averagedPerceptron=new AveragedPerceptron(avgWeights.length,avgWeights[0].length);
        averagedPerceptron.avgWeights=avgWeights;
        return averagedPerceptron;
    }

    public void updateWeight(int label, int slot, Object feature, double change) {
        if(!weights[label][slot].containsKey(feature)){
            weights[label][slot].put(feature, change);
        }  else{
            weights[label][slot].put(feature, weights[label][slot].get(feature) + change);
        }

        if(!avgWeights[label][slot].containsKey(feature)){
            avgWeights[label][slot].put(feature, iteration * change);
        }  else{
            avgWeights[label][slot].put(feature, avgWeights[label][slot].get(feature) + iteration * change);
        }
    }

    @Override
    public void saveModel(String modelPath) throws  Exception{
        System.err.print("Writing model to the file...");
        HashMap<Object, Double>[][] finalAverageWeight = new HashMap[weights.length][weights[0].length];
        
        for(int i=0;i<weights.length;i++) {
           for(int j=0;j<weights[i].length;j++) {
               finalAverageWeight[i][j] = new HashMap<Object, Double>();
               for (Object feat : weights[i][j].keySet()) {
                   double newValue = weights[i][j].get(feat) - (avgWeights[i][j].get(feat) / iteration);
                   if (newValue != 0.0)
                       finalAverageWeight[i][j].put(feat, newValue);
               }
           }
        }
        ObjectOutput writer = new ObjectOutputStream(new FileOutputStream(modelPath));
        writer.writeObject(finalAverageWeight);
        writer.flush();
        writer.close();
        System.err.print("done\n");
    }

    public float score(int label, ArrayList<Object>[] features,boolean decode){
        float score=0.0f;
        HashMap<Object, Double>[] map;
        if(decode)
            map= avgWeights[label];
        else
            map= weights[label];
      
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
            for(int j=0;j<avgWeights[i].length;j++)
                size+=avgWeights[i][j].size();
        return size;
    }

    public HashMap<Object, Double>[][] getWeights() {
        return weights;
    }

    public HashMap<Object, Double>[][] getAvgWeights() {
        return avgWeights;
    }

    public void setAvgWeights(HashMap<Object, Double>[][] avgWeights) {
        this.avgWeights = avgWeights;
    }
}