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
        String p4 = filePath + "/sample_data/parse.mst.en.dev";
        String p5 = filePath + "/sample_data/intersection.txt.dev";
        String p6 = "/tmp/reorder.model";
        
        int size=324;

        if (args.length > 2) {
            Options options = new Options(args);
            if (options.hasSufficientArguments()) {
                System.out.println(options);
                if (options.train) {
                    if (options.twoClassifier) {
                        Trainer trainer = new Trainer(options.trainTreePath, options.trainIntersectionPath,
                                options.devTreePath, options.devIntersectionPath, options.universalPOSPath, 5, options.topK, 189);
                        trainer.trainWithPerceptron(options.maxIter, options.modelPath, true);
                    } else {
                        Trainer trainer = new Trainer(options.trainTreePath, options.trainIntersectionPath,
                                options.devTreePath, options.devIntersectionPath, options.universalPOSPath, 7, options.topK, 189);
                        trainer.trainWithPerceptron(options.maxIter, options.modelPath, false);
                    }
                }
                else if(options.decode || options.decodeWithAlignment){
                    Info info = new Info(options.modelPath, options.tunedIterations);
                    if (info.twoClassifer) {
                        AveragedPerceptron[] leftClassifier = new AveragedPerceptron[info.getFinalLeftWeights().length];
                        for (int i = 0; i < leftClassifier.length; i++) {
                            leftClassifier[i] = new AveragedPerceptron(info.getTopK(), info.getFeatLen());
                            leftClassifier[i].setAvgWeights(info.getFinalLeftWeights()[i]);
                        }

                        AveragedPerceptron[] rightClassifier = new AveragedPerceptron[info.getFinalRightWeights().length];
                        for (int i = 0; i < rightClassifier.length; i++) {
                            rightClassifier[i] = new AveragedPerceptron(info.getTopK(), info.getFeatLen());
                            rightClassifier[i].setAvgWeights(info.getFinalRightWeights()[i]);
                        }

                        AveragedPerceptron pivotClassifer = new AveragedPerceptron(1, info.getPivotWeights().length);
                        pivotClassifer.setAvgWeights(info.getPivotWeights());

                        Reorderer reorderer = new Reorderer(
                                leftClassifier, rightClassifier, pivotClassifer, info.getMostLeftCommonPermutations(), info.getMostRightCommonPermutations(),
                                info.getUniversalPosMap(), info.getTopK(), options.numOfThreads, info.getMaps()
                        );
                        if (options.decode)
                            reorderer.decode(options.inputFile, options.outputFile);
                        else if (options.decodeWithAlignment)
                            reorderer.decodeWithAlignmentGuide(options.inputFile, options.inputIntersectionFile, options.outputFile);

                    } else {
                        AveragedPerceptron[] classifier = new AveragedPerceptron[info.getFinalWeights().length];
                        for (int i = 0; i < classifier.length; i++) {
                            classifier[i] = new AveragedPerceptron(info.getTopK(), info.getFeatLen());
                            classifier[i].setAvgWeights(info.getFinalWeights()[i]);
                        }
                        Reorderer reorderer = new Reorderer(
                                classifier, info.getMostCommonPermutations(), info.getUniversalPosMap(), info.getTopK(), options.numOfThreads, info.getMaps()
                        );
                        if (options.decode)
                            reorderer.decode(options.inputFile, options.outputFile);
                        else if (options.decodeWithAlignment)
                            reorderer.decodeWithAlignmentGuide(options.inputFile, options.inputIntersectionFile, options.outputFile);
                    }
                }
            } else
                System.out.println(Options.showHelp());
        } else {
            System.out.println(Options.showHelp());
            System.err.println("\nperceptron ");

            Trainer trainer = new Trainer(p1, p2, p4, p5, p3, 5, 20,  190);
            trainer.trainWithPerceptron(3, p6, true);

            int[] tuned = {3, 3, 3, 3, 3, 3, 3, 3, 3};
            Info info = new Info(p6, tuned);
            AveragedPerceptron[] leftClassifier = new AveragedPerceptron[info.getFinalLeftWeights().length];
            for (int i = 0; i < leftClassifier.length; i++) {
                leftClassifier[i] = new AveragedPerceptron(info.getTopK(), info.getFeatLen());
                leftClassifier[i].setAvgWeights(info.getFinalLeftWeights()[i]);
            }

            AveragedPerceptron[] rightClassifier = new AveragedPerceptron[info.getFinalRightWeights().length];
            for (int i = 0; i < rightClassifier.length; i++) {
                rightClassifier[i] = new AveragedPerceptron(info.getTopK(), info.getFeatLen());
                rightClassifier[i].setAvgWeights(info.getFinalRightWeights()[i]);
            }

            AveragedPerceptron pivotClassifer = new AveragedPerceptron(1, info.getPivotWeights().length);
            pivotClassifer.setAvgWeights(info.getPivotWeights());


            Reorderer reorderer = new Reorderer(
                    leftClassifier, rightClassifier, pivotClassifer, info.getMostLeftCommonPermutations(), info.getMostRightCommonPermutations(),
                    info.getUniversalPosMap(), info.getTopK(), 1, info.getMaps()
            );
            reorderer.decode(p4, p4 + ".out");
            reorderer.decodeWithAlignmentGuide(p4, p5, p5 + ".out");
        }

    }
}
