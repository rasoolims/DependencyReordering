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

    public ContextInstance(int headIndex, int[] order, DependencyTree originalTree) {
        this.headIndex = headIndex;
        this.order = order;
        this.tree = originalTree.getFullOrder(order, headIndex);
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

    public ArrayList<ContextInstance> getPossibleContexts(HashMap<String,Integer> posOrderMap, int max) {
        ArrayList<int[]> permutations = getAllPossibleContexts();

        TreeSet<PosOrderFrequency> mostFrequentOrderings=new TreeSet<PosOrderFrequency>();
        for (int[] order : permutations) {
            String posOrderStr=tree.toPosString(order,headIndex);
            int freq=posOrderMap.containsKey(posOrderStr)?posOrderMap.get(posOrderStr):0;
            mostFrequentOrderings.add(new PosOrderFrequency(order,freq));
            
            if(mostFrequentOrderings.size()>max)
                mostFrequentOrderings.pollFirst();
        }
        
        ArrayList<ContextInstance> candidates = new ArrayList<ContextInstance>();
        for (PosOrderFrequency order : mostFrequentOrderings) {
            candidates.add(new ContextInstance(headIndex, order.order, tree));
        }
        if(candidates.size()>=max)
            candidates.add(this);
        return candidates;

    }

    public ArrayList<String> extractMainFeatures() {
        ArrayList<String> features = new ArrayList<String>();

        Word[] words = tree.words;
        int[] treeOrder = tree.order;
        int[] indices = tree.indices;

        //region head feature
        Word headWord = words[headIndex];
        features.add("head:word:" + headWord.wordForm);
        features.add("head:cpos:" + headWord.cPos);
        features.add("head:fpos:" + headWord.fPos);
        //endregion

        for (int i = 0; i < order.length; i++) {
            if (order[i] == headIndex)
                continue;
            //region children
            String position = "before";
            if (indices[order[i]] == indices[headIndex] - 1)
                position = "imm-before";
            else if (indices[order[i]] == indices[headIndex] + 1)
                position = "imm-after";
            else if (indices[order[i]] > indices[headIndex])
                position = "after";

            Word child = words[order[i]];

            features.add("children:word:" + position + ":" + child.wordForm);
            features.add("children:cpos:" + position + ":" + child.cPos);
            features.add("children:fpos:" + position + ":" + child.fPos);
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

                features.add("first_gap:word:" + firstWord.wordForm);
                features.add("first_gap:cpos:" + firstWord.cPos);
                features.add("first_gap:fpos:" + firstWord.fPos);

                features.add("last_gap:word:" + lastWord.wordForm);
                features.add("last_gap:cpos:" + lastWord.cPos);
                features.add("last_gap:fpos:" + lastWord.fPos);
            }
            //endregion

            //region consecutive children
            if (i < order.length - 1 && order[i + 1] != headIndex && (indices[order[i + 1]] - indices[order[i]] > 1)) {
                int last = treeOrder[indices[order[i + 1]] - 1];
                int first = treeOrder[indices[order[i]] + 1];

                Word firstWord = words[first];
                Word lastWord = words[last];

                features.add("first_consec_gap:word:" + firstWord.wordForm);
                features.add("first_consec_gap:cpos:" + firstWord.cPos);
                features.add("first_consec_gap:fpos:" + firstWord.fPos);

                features.add("last_consec_gap:word:" + lastWord.wordForm);
                features.add("last_consec_gap:cpos:" + lastWord.cPos);
                features.add("last_consec_gap:fpos:" + lastWord.fPos);
            }
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
            features.add("left_sibling:word:" + leftSibling.wordForm);
            features.add("left_sibling:cpos:" + leftSibling.cPos);
            features.add("left_sibling:fpos:" + leftSibling.fPos);
        } else {
            features.add("left_sibling:word:NONE");
            features.add("left_sibling:cpos:NONE");
            features.add("left_sibling:fpos:NONE");
        }

        if (headOrder < orderedSiblings.length - 1) {
            Word rightSibling = words[orderedSiblings[headOrder + 1]];
            features.add("right_sibling:word:" + rightSibling.wordForm);
            features.add("right_sibling:cpos:" + rightSibling.cPos);
            features.add("right_sibling:fpos:" + rightSibling.fPos);
        } else {
            features.add("right_sibling:word:NONE");
            features.add("right_sibling:cpos:NONE");
            features.add("right_sibling:fpos:NONE");
        }
        //endregion


        //region bilexical features
        int featLen = features.size();
        for (i = 0; i < featLen; i++) {
            for (int j = 0; j < featLen; j++) {
                if (i == j)
                    continue;
                features.add(features.get(i) + "|" + features.get(j));
            }
        }
        //endregion

        return features;
    }

    public static int[] getPossibilities() {
        return possibilities;
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
        return order.hashCode() + headIndex;
    }
    //endregion
}
