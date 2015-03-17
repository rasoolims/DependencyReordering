package edu.columbia.cs.rasooli.Reordering.Structures;

import java.util.HashSet;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/11/15
 * Time: 7:17 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class DependencyTree {
   // the zeroth word should be artificial ROOT token
    Word[] words;
    
    // shows the current index for words
    int[] indices;
    
    // uses true order
    int[] heads;
    
    // use true order
    String[] labels;
    
    HashSet<Integer>[] deps;

    public DependencyTree(Word[] words, int[] heads, String[] labels) {
        this.words = words;
        this.heads = heads;
        this.labels = labels;
        
        deps=new HashSet[words.length];
        for(int i=0;i< heads.length;i++)
            deps[i]=new HashSet<Integer>();
        
        for(int i=0;i< heads.length;i++)
            if(heads[i]>=0)
                deps[heads[i]].add(i);
        
        indices=new int[words.length];
        for(int i=0;i<indices.length;i++)
            indices[i]=i;
    }
    
    public boolean hasDep(int head){
        return deps[indices[head]].size()>0;
    }
    
    public int getCurrentIndex(int index){
        return indices[index];
    }
    
    public int getCurrentHead(int index){
        return heads[indices[index]];
    }
    
    public String getCurrentLabel(int index){
        return labels[indices[index]];
    }
    
    public Word getCurrentWord(int index){
        return words[indices[index]];
    }
    
    public  HashSet<Integer> getDependents(int head){
        return deps[indices[head]];
    }

}
