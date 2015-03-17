package edu.columbia.cs.rasooli.Reordering.Structures;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/11/15
 * Time: 11:40 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class ContextInstance implements Comparable{
    int headIndex;
    int[] order;
    final static int[] possibilities=new int[]{2,6,24,120,720,5040,40320};

    public ContextInstance(int headIndex, int[] order) {
        this.headIndex = headIndex;
        this.order = order;
    }

    public  ArrayList<int[]> getAllPossibleContexts(){
       int possib=order.length>8?1:possibilities[order.length-1];
        if(possib==1){
            ArrayList<int[]> p=new ArrayList<int[]>();
            p.add(order);
            return p;
        }
         return permute(order);
    }

    public ArrayList<int[]> permute(int[] arr) {
        ArrayList<int[]> permutations = new ArrayList<int[]>();
        if (arr.length == 1)
            permutations.add(Arrays.copyOf(arr, arr.length));
        else {
            int first = arr[0];
            ArrayList<int[]> subsetPermutation = permute(Arrays.copyOfRange(arr, 1, arr.length));

            for (int i = 0; i < arr.length; i++) {
                for (int[] subset : subsetPermutation)
                    permutations.add(insertToArray(subset, first, i));
            }
        }
        return permutations;
    }
    
    private int[] insertToArray(int[] arr, int element, int index){
        int[] newArray=new int[arr.length+1];
        int j=0;
        int i=0;
        while(i+j<newArray.length){
            if(i==index && j==0) {
                newArray[i+j]=element;
                j++;
            }else{
                newArray[i+j]=arr[i];
                i++;
            }
        }
        return newArray;
    }

    //region compareTo
    @Override
    public int compareTo(Object o) {
        if(!(o instanceof ContextInstance))
            return 1;
        ContextInstance instance=(ContextInstance) o;
        if(order.length>instance.order.length)
            return    order.length-instance.order.length;
        if(headIndex!=instance.headIndex)
            return headIndex-instance.headIndex;
        
        for(int i=0;i< order.length;i++)
            if(order[i]<instance.order[i])
                    return order[i]-instance.order[i] ;
        return 0;   
    }
    
    @Override
    public boolean equals(Object o){
        return  compareTo(o)==0;
    }
    
    @Override
    public int hashCode(){
        return order.hashCode()+ headIndex;
    }
    //endregion
}
