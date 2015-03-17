package edu.columbia.cs.rasooli.Reordering.Training;

import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.ContextInstance;

import java.util.*;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/12/15
 * Time: 12:22 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class TrainData {
    ContextInstance goldInstance;
    ContextInstance originalInstance;

    public TrainData( ContextInstance originalInstance, ContextInstance goldInstance) {
        this.originalInstance = originalInstance;
        this.goldInstance = goldInstance;
    }
    
    
    public static ArrayList<TrainData> getAllPossibleTrainData(ArrayList<BitextDependency> data){
        ArrayList<TrainData> trainData=new ArrayList<TrainData>();
        int numOfChangedOrders=0;
        int numOfChangedSentences=0;
        
        int count=0;
        for(BitextDependency bitextDependency:data){
            count++;
            if(count%1000==0)
                System.err.print(count+"...");
            boolean changed=false;
            for(int head: bitextDependency.getTrainableHeads()){
               HashSet<Integer> deps=bitextDependency.getSourceTree().getDependents(head);
                TreeSet<Integer> origOrderSet=new TreeSet<Integer>() ;
                origOrderSet.add(head);
                for(int dep:deps)
                origOrderSet.add(dep);
                
                int[] origOrder=new int[1+deps.size()];
                int i=0;
                for(int dep:origOrderSet)
                    origOrder[i++]=dep;
                
                ContextInstance origContext=new ContextInstance(head,origOrder,bitextDependency.getSourceTree());

                TreeMap<Integer, Integer> changedOrder=new TreeMap<Integer, Integer>();
                
                SortedSet<Integer>[] alignedSet=bitextDependency.getAlignedWords();
                changedOrder.put(alignedSet[head].first(), head);
                for(int dep:origOrderSet)
                    changedOrder.put(alignedSet[dep].first(), dep);

                int[] goldOrder=new int[1+deps.size()];
                i=0;
                for(int dep:changedOrder.keySet())
                     goldOrder[i++]=changedOrder.get(dep);
                
                ContextInstance goldContext=new ContextInstance(head,goldOrder,bitextDependency.getSourceTree());

                if(!goldContext.equals(origContext)) {
                    numOfChangedOrders++;
                    changed=true;
                }
                trainData.add(new TrainData(origContext,goldContext));
            }
            if(changed)
                numOfChangedSentences++;

        }
        System.err.print(count+"\n");
        float proportion=100.0f*(float)numOfChangedOrders/trainData.size();
        System.out.println(proportion);

        proportion=100.0f*(float)numOfChangedSentences/data.size();
        System.out.println(proportion);
        return trainData;
    }

    public ContextInstance getGoldInstance() {
        return goldInstance;
    }

    public ContextInstance getOriginalInstance() {
        return originalInstance;
    }
}
