package edu.columbia.cs.rasooli.Reordering.Structures;

import java.util.*;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/11/15
 * Time: 11:40 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class ContextInstance implements Comparable {
    final static int[] possibilities = new int[]{2, 6, 24, 120, 720, 5040, 40320};
    public DependencyTree tree;
    int headIndex;
    int[] order;

    public ContextInstance(int headIndex, int[] order, DependencyTree originalTree) throws Exception {
        this.headIndex = headIndex;
        this.order = order;
        this.tree = originalTree.getFullOrder(order, headIndex);
    }

    public static int[] getPossibilities() {
        return possibilities;
    }

    private ArrayList<int[]> getAllPossibleContexts() {
        int possib = order.length > 7 ? 1 : possibilities[order.length - 1];
        if (possib == 1) {
            ArrayList<int[]> p = new ArrayList<int[]>();
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

    private int[] insertToArray(int[] arr, int element, int index) {
        int[] newArray = new int[arr.length + 1];
        int j = 0;
        int i = 0;
        while (i + j < newArray.length) {
            if (i == index && j == 0) {
                newArray[i + j] = element;
                j++;
            } else {
                newArray[i + j] = arr[i];
                i++;
            }
        }
        return newArray;
    }

    public int[] getOrder() {
        return order;
    }

    public HashSet<ContextInstance> getPossibleContexts(HashMap<String, Integer> posOrderMap, int max) throws Exception {
        ArrayList<int[]> permutations = getAllPossibleContexts();

        TreeSet<PosOrderFrequency> mostFrequentOrderings=new TreeSet<PosOrderFrequency>();
        for (int[] order : permutations) {
            String posOrderStr=tree.toPosString(order,headIndex);
            int freq=posOrderMap.containsKey(posOrderStr)?posOrderMap.get(posOrderStr):0;
            mostFrequentOrderings.add(new PosOrderFrequency(order,freq));

            if(mostFrequentOrderings.size()>max)
                mostFrequentOrderings.pollFirst();
        }

        HashSet<ContextInstance> candidates = new HashSet<ContextInstance>();
        for (PosOrderFrequency order : mostFrequentOrderings) {
            candidates.add(new ContextInstance(headIndex, order.order, tree));
        }
        if (candidates.size() >= max && !permutations.contains(this))
            candidates.add(this);
        return candidates;

    }

    public ArrayList<Object>[] extractMainFeatures() {
        int size=324;
        ArrayList<Object>[] features = new ArrayList[size];
        for(int i=0;i<size;i++)
            features[i]=new ArrayList<Object>();

        Word[] words = tree.words;
        int[] treeOrder = tree.order;
        int[] indices = tree.indices;
        
        int index=0;

        //region head feature
        Word headWord = words[headIndex];
        features[index++].add(headWord.wordForm);
        features[index++].add(headWord.cPos);
        features[index++].add(headWord.fPos);
        //endregion

        
        int currIndex=index;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == headIndex)
                continue;
            
            index=currIndex;
            
            //region children
            String position = "before";
            if (indices[order[i]] == indices[headIndex] - 1)
                position = "imm-before";
            else if (indices[order[i]] == indices[headIndex] + 1)
                position = "imm-after";
            else if (indices[order[i]] > indices[headIndex])
                position = "after";

            Word child = words[order[i]];

            features[index].add(position + ":" + child.wordForm);
            features[index+1].add(position + ":" + child.cPos);
            features[index+2].add(position + ":" + child.fPos);
            //endregion

            //region there is a gap with head
            if (!position.startsWith("imm")) {
                int first, last;
                if (position.equals("before")) {
                    last = treeOrder[indices[headIndex] - 1];
                    first = treeOrder[indices[order[i]] + 1];
                } else {
                    last = treeOrder[indices[order[i]] - 1];
                    first = treeOrder[indices[headIndex] + 1];
                }

                Word firstWord = words[first];
                Word lastWord = words[last];

                features[index+3].add(firstWord.wordForm);
                features[index+4].add(firstWord.cPos);
                features[index+5].add(firstWord.fPos);

                features[index+6].add(lastWord.wordForm);
                features[index+7].add(lastWord.cPos);
                features[index+8].add(lastWord.fPos);
            }
            index+=9;
            //endregion

            //region consecutive children
            if (i < order.length - 1 && order[i + 1] != headIndex && (indices[order[i + 1]] - indices[order[i]] > 1)) {
                int last = treeOrder[indices[order[i + 1]] - 1];
                int first = treeOrder[indices[order[i]] + 1];

                Word firstWord = words[first];
                Word lastWord = words[last];

                features[index].add(firstWord.wordForm);
                features[index+1].add(firstWord.cPos);
                features[index+2].add(firstWord.fPos);

                features[index+3].add(lastWord.wordForm);
                features[index+4].add(lastWord.cPos);
                features[index+5].add(lastWord.fPos);
            }
            index+=6;
            //endregion
        }

        //region left and right siblings of head
        int headOfHead = tree.getCurrentHead(headIndex);
        HashSet<Integer> siblings = tree.getDependents(headOfHead);
        TreeMap<Integer, Integer> siblingMap = new TreeMap<Integer, Integer>();

        for (int sib : siblings) {
            siblingMap.put(indices[sib], sib);
        }

        int headOrder = 0;
        int[] orderedSiblings = new int[siblingMap.size()];
        int i = 0;
        for (int sib : siblingMap.keySet()) {
            int sb = siblingMap.get(sib);
            if (sb == headIndex)
                headOrder = i;
            orderedSiblings[i++] = sb;
        }

        if (headOrder > 0) {
            Word leftSibling = words[orderedSiblings[headOrder - 1]];
            features[index].add( leftSibling.wordForm);
            features[index+1].add(leftSibling.cPos);
            features[index+2].add( leftSibling.fPos);
        } else {
            features[index].add("left_sibling:word:NONE");
            features[index+1].add("left_sibling:cpos:NONE");
            features[index+2].add("left_sibling:fpos:NONE");
        }

        if (headOrder < orderedSiblings.length - 1) {
            Word rightSibling = words[orderedSiblings[headOrder + 1]];
            features[index+3].add( rightSibling.wordForm);
            features[index+4].add(rightSibling.cPos);
            features[index+5].add( rightSibling.fPos);
        } else {
            features[index+3].add("right_sibling:word:NONE");
            features[index+4].add("right_sibling:cpos:NONE");
            features[index+5].add("right_sibling:fpos:NONE");
        }
        index+=6;
        //endregion


        //region bilexical features
        int l=index;
        for(int f1=0;f1<l;f1++) {
            int featLen = features[f1].size();
            for(int f2=f1;f2<l;f2++){
                int featLen2 = features[f2].size();
                for (i = 0; i < featLen; i++) {
                    for (int j = 0; j < featLen2; j++) {
                        if(!(i==j && f1==f2))
                            features[index].add(features[f1].get(i) + "|" + features[f2].get(j));
                    }
                }
                index++;
            }
        }
        //endregion

        return features;
    }

    public DependencyTree getTree() {
        return tree;
    }

    public int getHeadIndex() {
        return headIndex;
    }

    //region compareTo
    @Override
    public int compareTo(Object o) {
        if (!(o instanceof ContextInstance))
            return 1;
        ContextInstance instance = (ContextInstance) o;
        if (order.length > instance.order.length)
            return order.length - instance.order.length;
        if (headIndex != instance.headIndex)
            return headIndex - instance.headIndex;

        for (int i = 0; i < order.length; i++)
            if (order[i] < instance.order[i])
                return order[i] - instance.order[i];
        return 0;
    }

    
    @Override
    public boolean equals(Object o) {
        return compareTo(o) == 0;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < order.length; i++)
            hashCode = hashCode | (order[i] << i);
        return hashCode + headIndex;
    }
    //endregion
}
