package edu.columbia.cs.rasooli.Test;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.FileManagement.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Training.TrainData;
import edu.columbia.cs.rasooli.Reordering.Training.Trainer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

class BitextDependencyReaderTest {
    public static void main(String[] args) throws Exception {
        String filePath = new File("").getAbsolutePath();
        String p1=  filePath + "/src/sample_data/parse.mst.en";
        String p2=   filePath + "/src/sample_data/intersection.txt";
        String p3=     filePath + "/src/sample_data/en-ptb.map";
        String p4=    filePath + "/src/sample_data/parse.mst.en";
        String p5=   filePath + "/src/sample_data/intersection.txt";
        String p6=    "/tmp/reorder.model";

        if(args.length>5){
            p1=new File(args[0]).getAbsolutePath();
            p2=new File(args[1]).getAbsolutePath();
            p3=new File(args[2]).getAbsolutePath();
            p4=new File(args[3]).getAbsolutePath();
            p5=new File(args[4]).getAbsolutePath();
            p6=new File(args[5]).getAbsolutePath();
        }

        ArrayList<BitextDependency> data = testReadFromBitext(p1, p2,p3 );
        ArrayList<BitextDependency> devBitext = testReadFromBitext(p4, p5,p3 );
        HashMap<String,Integer> posOrderMap=TrainData.constructPosOrderFrequency(data);
      ArrayList<TrainData> trainingData = TrainData.getAllPossibleTrainData(data);
        ArrayList<TrainData> devData = TrainData.getAllPossibleTrainData(devBitext);
        Trainer.trainWithPerceptron(trainingData, devData, new AveragedPerceptron(), 10, p6, posOrderMap, 20);
        System.out.println("test successful " + data.size());
    }

    public static ArrayList<BitextDependency> testReadFromBitext(String parsedFilePath, String alignIntersectionPath, String universalPosPath) throws Exception {
        return BitextDependencyReader.readFromBitext(parsedFilePath, alignIntersectionPath, universalPosPath);
    }
}