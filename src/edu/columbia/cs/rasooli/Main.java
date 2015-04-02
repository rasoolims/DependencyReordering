package edu.columbia.cs.rasooli;

import edu.columbia.cs.rasooli.Reordering.Classifier.AveragedPerceptron;
import edu.columbia.cs.rasooli.Reordering.Decoding.Reorderer;
import edu.columbia.cs.rasooli.Reordering.Enums.ClassifierType;
import edu.columbia.cs.rasooli.Reordering.Options;
import edu.columbia.cs.rasooli.Reordering.Structures.Info;
import edu.columbia.cs.rasooli.Reordering.Training.Trainer;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String filePath = new File("").getAbsolutePath();
        String p1 = filePath + "/sample_data/parse.mst.en";
        String p2 = filePath + "/sample_data/intersection.txt";
      //  p1="/tmp/parse.train";
      //  p2="/tmp/intersection.train";
        String p3 = filePath + "/sample_data/en-ptb.map";
        String p4 = filePath + "/sample_data/parse.mst.en.dev";
        String p5 = filePath + "/sample_data/intersection.txt.dev";
        String p6 = "/tmp/reorder.model";
        
        int size=324;

        if (args.length > 2) {
            Options options = new Options(args);
            if (options.hasSufficientArguments()) {
                System.out.println(options);
                if (options.train) {
                    Trainer trainer=new Trainer(options.trainTreePath, options.trainIntersectionPath,
                            options.devTreePath, options.devIntersectionPath, options.universalPOSPath,options.trainTreePath+".train",options.devTreePath+".dev",7,options.topK,324);
                  if(options.classifierType== ClassifierType.perceptron)
                      trainer.trainWithPerceptron(options.maxIter, options.modelPath);
                  else if(options.classifierType== ClassifierType.pegasos)
                      trainer.trainWithPegasos(options.maxIter, options.modelPath, options.pegasos_lambda);
                }
                else if(options.decode || options.decodeWithAlignment){
                    Info info = new Info(options.modelPath, options.tunedIterations);
                    AveragedPerceptron[] classifier=new AveragedPerceptron[info.getFinalWeights().length];
                    for(int i=0;i<classifier.length;i++) {
                        classifier[i]=new AveragedPerceptron(info.getTopK(), info.getFeatLen());
                        classifier[i].setAvgWeights(info.getFinalWeights()[i]);
                    }
                    Reorderer reorderer=new Reorderer(
                            classifier,info.getMostCommonPermutations(),info.getUniversalPosMap(),info.getTopK(),options.numOfThreads ,info.getMaps()
                    );
                    if(options.decode)
                    reorderer.decode(options.inputFile,options.outputFile);
                    else if(options.decodeWithAlignment)
                        reorderer.decodeWithAlignmentGuide(options.inputFile,options.inputIntersectionFile,options.outputFile);
                }
            } else
                System.out.println(Options.showHelp());
        } else {
            System.out.println(Options.showHelp());
            double lambda=1f;
           /*
            for (int t = 0; t < 5; t++) {
                System.err.println("\nlambda " + lambda);
                
                Trainer trainer = new Trainer(p1, p2, p4, p5, p3, p1 + ".train", p4 + ".dev", 7, 20, 324);

                trainer.trainWithPegasos(3, p6, 0.01f);

                int[] tuned = {3, 3, 3, 3, 3, 3, 3};
                Info info = new Info(p6, tuned);
                OnlinePegasos[] classifier = new OnlinePegasos[info.getFinalWeights().length];
             
                for (int i = 0; i < classifier.length; i++) {
                    classifier[i] = new OnlinePegasos(info.getTopK(), info.getFeatLen());
                    classifier[i].setWeights(info.getFinalWeights()[i]);
                }

                Reorderer reorderer = new Reorderer(
                        classifier, info.getMostCommonPermutations(), info.getUniversalPosMap(), info.getTopK(), 4, info.getMaps()
                );
                reorderer.decode(p4, p4 + ".out");
                reorderer.decodeWithAlignmentGuide(p4, p5, p5 + ".out");
                lambda/=10;
            }
            */
            System.err.println("\nperceptron ");

            Trainer trainer = new Trainer(p1, p2, p4, p5, p3, p1 + ".train", p4 + ".dev", 7, 20, 324);
            trainer.trainWithPerceptron(3, p6);

            int[] tuned = {3, 3, 3, 3, 3, 3, 3};
            Info info = new Info(p6, tuned);
            AveragedPerceptron[] classifier=new AveragedPerceptron[info.getFinalWeights().length];
            for(int i=0;i<classifier.length;i++) {
             classifier[i]=new AveragedPerceptron(info.getTopK(), info.getFeatLen());
                classifier[i].setAvgWeights(info.getFinalWeights()[i]);
            }

            Reorderer reorderer = new Reorderer(
                    classifier, info.getMostCommonPermutations(), info.getUniversalPosMap(), info.getTopK(), 1, info.getMaps()
            );
            reorderer.decode(p4, p4 + ".out");
            reorderer.decodeWithAlignmentGuide(p4, p5, p5 + ".out");
        }

    }
}
