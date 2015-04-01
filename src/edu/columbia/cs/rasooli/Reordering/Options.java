package edu.columbia.cs.rasooli.Reordering;

import edu.columbia.cs.rasooli.Reordering.Enums.ClassifierType;

import java.io.File;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/24/15
 * Time: 10:17 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class Options {
    public String trainTreePath;
    public String trainIntersectionPath;
    public String devTreePath; 
    public String devIntersectionPath;
    public String universalPOSPath ;
    public String inputFile;
    public String inputIntersectionFile;
    public String outputFile;
    public int numOfThreads;
    public boolean decodeWithAlignment=false;
    public boolean decode=false;
    public int[] tunedIterations;
    public ClassifierType classifierType;
    public double pegasos_lambda=0.0001;

    public int maxIter;
    public String modelPath;
    public int topK ;
    public boolean train;
    
    public Options(){
        train=true;
        maxIter=10;
        topK=20;
        devTreePath="";
        devIntersectionPath="";
        numOfThreads=8;
        decodeWithAlignment=false;
        decode=false;
        tunedIterations = null;
        classifierType=ClassifierType.perceptron;
        pegasos_lambda=0.0001;
    }
    
    public Options(String[] args){
        this();
        
        for(int i=0;i<args.length;i++){
            if(args[i].equals("train"))
                train=true;
            else if (args[i].equals("decode")) {
                train = false;
                decode=true;
            }  else if (args[i].equals("decode_align")) {
                train = false;
                decode=false;
                decodeWithAlignment=true;
            }
            else if(args[i].equals("-tt"))
                trainTreePath=new File(args[i+1]).getAbsolutePath();

            else if(args[i].equals("-ti"))
                trainIntersectionPath=new File(args[i+1]).getAbsolutePath();
            else if(args[i].equals("-dt"))
                devTreePath=new File(args[i+1]).getAbsolutePath();
            else if(args[i].equals("-di"))
                devIntersectionPath=new File(args[i+1]).getAbsolutePath();
            else if(args[i].equals("-p"))
                universalPOSPath=new File(args[i+1]).getAbsolutePath();
            else if(args[i].equals("-c")) {
                if (args[i + 1].equals("perceptron"))
                    classifierType = ClassifierType.perceptron;
                else if (args[i + 1].equals("pegasos"))
                    classifierType = ClassifierType.pegasos;
            }
            else if(args[i].equals("-m"))
                modelPath=new File(args[i+1]).getAbsolutePath();
            else if(args[i].equals("-iter"))
                maxIter = Integer.parseInt(args[i + 1]);
                else if(args[i].equals("-l"))
                    pegasos_lambda = Float.parseFloat(args[i + 1]);
            else if(args[i].equals("-nt"))
                numOfThreads = Integer.parseInt(args[i + 1]);
            else if(args[i].equals("-top"))
                topK = Integer.parseInt(args[i + 1]);
            else if(args[i].equals("-i"))
                inputFile=new File(args[i+1]).getAbsolutePath();
            else if(args[i].equals("-is"))
                inputIntersectionFile=new File(args[i+1]).getAbsolutePath();
            else if(args[i].equals("-o"))
                outputFile=new File(args[i+1]).getAbsolutePath();
            else if (args[i].equals("-tune")) {
                String[] tunes = args[i + 1].trim().split(",");
                tunedIterations = new int[tunes.length];
                for (int j = 0; j < tunes.length; j++)
                    tunedIterations[j] = Integer.parseInt(tunes[j]);
            }
        }
    }
    
    public static String showHelp(){
        StringBuilder builder=new StringBuilder();
        
        builder.append("to train a model\n");
        builder.append("java -jar reorderer.jar train -tt [train-tree-file] -ti [train-intersection-file] ");
        builder.append(" -dt [dev-tree-file(optional)] -di [dev-intersection-file(optional)] -m [model-file] -p [universal-pos-file] ");
        builder.append("-iter [#training-iterations] -top [top-k-pruning(default:10)] -nt [#threads(default:8)] -c [perceptron or pegasos (default:perceptron)] -l [pegasos_lambda(default:0.0001)]\n");
        
       
        builder.append("\nto reorder input trees\n");
        builder.append("java -jar reorderer.jar  decode -m [model-file] -i [input-file] -o [output-file]  -nt [#threads(default:8)] -tune [numbers separated by comma]\n");

        builder.append("\nto reorder input trees with alignment guide\n");
        builder.append("java -jar reorderer.jar  decode_align -m [model-file] -i [input-file] -is [input-alignment-intersection-file] -o [output-file]  -nt [#threads(default:8)]  -tune [numbers separated by comma]\n");

        
        builder.append("\nargument order is not important!\n");
        
        return builder.toString();
        
    }
    
    public boolean hasSufficientArguments() {
        if (decode && tunedIterations != null && inputFile != null && outputFile != null && modelPath != null)
            return true;
        if (decodeWithAlignment && tunedIterations != null && inputFile != null && inputIntersectionFile != null && outputFile != null && modelPath != null)
            return true;
        if (train && trainIntersectionPath != null && trainTreePath != null && modelPath != null && universalPOSPath != null)
            return true;
        System.out.println(toString());
        return false;
    }
    
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        if(train){
            builder.append("train-tree: "+trainTreePath+"\n");
            builder.append("train-intersection: "+trainIntersectionPath+"\n");
            builder.append("dev-tree: "+devTreePath+"\n");
            builder.append("dev-intersection: "+devIntersectionPath+"\n");
            builder.append("model: "+modelPath+"\n");
            builder.append("universal-pos: "+universalPOSPath+"\n");
            builder.append("training-iterations: "+maxIter+"\n");
            builder.append("topK: "+topK+"\n");
            builder.append("classifier_type: "+classifierType.toString()+"\n");
            if(classifierType==ClassifierType.pegasos)
            builder.append("lambda: "+pegasos_lambda+"\n");
        }  else{
            builder.append("model: "+modelPath+"\n");
            builder.append("input-file: "+inputFile+"\n");
            builder.append("input-intersection-file: "+inputIntersectionFile+"\n");
            builder.append("output-file: "+outputFile+"\n");
        }
        builder.append("threads: "+numOfThreads+"\n");

        return builder.toString();
    }
}
