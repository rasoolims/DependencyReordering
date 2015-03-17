package edu.columbia.cs.rasooli.Test;

import edu.columbia.cs.rasooli.Reordering.FileManagement.BitextDependencyReader;
import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;
import edu.columbia.cs.rasooli.Reordering.Training.TrainData;

import java.io.File;
import java.util.ArrayList;

class BitextDependencyReaderTest {
    public static void main(String[] args) throws Exception {
        String filePath = new File("").getAbsolutePath();
        ArrayList<BitextDependency> data=   testReadFromBitext(filePath+"/src/sample_data/parse.mst.en",filePath+"/src/sample_data/intersection.txt",filePath+"/src/sample_data/en-ptb.map");
       ArrayList<TrainData> trainingData= TrainData.getAllPossibleTrainData(data);
        for(TrainData trainData:trainingData){
          ArrayList<ContextInstance> candidates=  trainData.getOriginalInstance().getPossibleContexts();
            for(ContextInstance candidate:candidates){
             ArrayList<String> features=  candidate.extractMainFeatures();
            }
        }
        System.out.println("test successful "+data.size());
    }
    public static  ArrayList<BitextDependency> testReadFromBitext(String parsedFilePath, String alignIntersectionPath, String universalPosPath ) throws Exception{
      return   BitextDependencyReader.readFromBitext(parsedFilePath, alignIntersectionPath, universalPosPath) ;
    }
}