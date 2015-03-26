package edu.columbia.cs.rasooli;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Decoding.Reorderer;
import edu.columbia.cs.rasooli.Reordering.Options;
import edu.columbia.cs.rasooli.Reordering.Structures.Info;
import edu.columbia.cs.rasooli.Reordering.Training.Trainer;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String filePath = new File("").getAbsolutePath();
        String p1 = filePath + "/sample_data/parse.mst.en";
        String p2 = filePath + "/sample_data/intersection.txt";
        String p3 = filePath + "/sample_data/en-ptb.map";
        String p4 = filePath + "/sample_data/parse.mst.en";
        String p5 = filePath + "/sample_data/intersection.txt";
        String p6 = "/tmp/reorder.model";
        
        int size=324;

        if (args.length > 2) {
            Options options = new Options(args);
            if (options.hasSufficientArguments()) {
                System.out.println(options);
                if (options.train)
                    Trainer.trainWithPerceptron(options.trainTreePath, options.trainIntersectionPath,
                            options.devTreePath, options.devIntersectionPath, options.universalPOSPath, new AveragedPerceptron(size),
                            options.maxIter, options.modelPath, options.topK,options.numOfThreads);
                else {
                    Info info=new Info(options.modelPath);
                    AveragedPerceptron classifier=new AveragedPerceptron(size);
                    classifier.setAvgWeights(info.getFinalWeights());
                    Reorderer reorderer=new Reorderer(
                         classifier,info.getPosOrderFrequencyDic(),info.getUniversalPosMap(),info.getTopK(),options.numOfThreads
                    );
                    reorderer.decode(options.inputFile,options.outputFile);
                }
            } else
                System.out.println(Options.showHelp());
        } else {
            System.out.println(Options.showHelp());
            Trainer.trainWithPerceptron(p1, p2, p4, p5, p3, new AveragedPerceptron(size), 10, p6, 20,4);
        }

    }
}
