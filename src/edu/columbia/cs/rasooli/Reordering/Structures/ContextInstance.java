package edu.columbia.cs.rasooli.Reordering.Structures;

import edu.columbia.cs.rasooli.Reordering.Enums.Position;

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
        features[index++].add(headWord.wordFormIndex);
        features[index++].add(headWord.cPosIndex);
        features[index++].add(headWord.fPosIndex);
        //endregion

        int currIndex=index;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == headIndex)
                continue;
            
            index=currIndex;
            
            //region children
            Position position = Position.before;
            if (indices[order[i]] == indices[headIndex] - 1)
                position =  Position.immediatelyBefore;
            else if (indices[order[i]] == indices[headIndex] + 1)
                position =  Position.immediatelyAfter;
            else if (indices[order[i]] > indices[headIndex])
                position =  Position.after;

            Word child = words[order[i]];

            features[index].add(position.value | (child.wordFormIndex<<2));
            features[index+1].add(position.value | (child.cPosIndex<<2));
            features[index+2].add(position.value | (child.fPosIndex<<2));
            //endregion

            //region there is a gap with head
            if (!position.equals(Position.immediatelyAfter) && !position.equals(Position.immediatelyBefore)) {
                int first, last;
                if (position.equals(Position.before)) {
                    last = treeOrder[indices[headIndex] - 1];
                    first = treeOrder[indices[order[i]] + 1];
                } else {
                    last = treeOrder[indices[order[i]] - 1];
                    first = treeOrder[indices[headIndex] + 1];
                }

                Word firstWord = words[first];
                Word lastWord = words[last];

                features[index+3].add(firstWord.wordFormIndex);
                features[index+4].add(firstWord.cPosIndex);
                features[index+5].add(firstWord.fPosIndex);

                features[index+6].add(lastWord.wordFormIndex);
                features[index+7].add(lastWord.cPosIndex);
                features[index+8].add(lastWord.fPosIndex);
            }
            index+=9;
            //endregion

            //region consecutive children
            if (i < order.length - 1 && order[i + 1] != headIndex && (indices[order[i + 1]] - indices[order[i]] > 1)) {
                int last = treeOrder[indices[order[i + 1]] - 1];
                int first = treeOrder[indices[order[i]] + 1];

                Word firstWord = words[first];
                Word lastWord = words[last];

                features[index].add(firstWord.wordFormIndex);
                features[index+1].add(firstWord.cPosIndex);
                features[index+2].add(firstWord.fPosIndex);

                features[index+3].add(lastWord.wordFormIndex);
                features[index+4].add(lastWord.cPosIndex);
                features[index+5].add(lastWord.fPosIndex);
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
            features[index].add( leftSibling.wordFormIndex);
            features[index+1].add(leftSibling.cPosIndex);
            features[index+2].add( leftSibling.fPosIndex);
        } else {
            features[index].add(-2);
            features[index+1].add(-2);
            features[index+2].add(-2);
        }

        if (headOrder < orderedSiblings.length - 1) {
            Word rightSibling = words[orderedSiblings[headOrder + 1]];
            features[index+3].add( rightSibling.wordFormIndex);
            features[index+4].add(rightSibling.cPosIndex);
            features[index+5].add( rightSibling.fPosIndex);
        } else {
            features[index+3].add(-2);
            features[index+4].add(-2);
            features[index+5].add(-2);
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
                        if(!(i==j && f1==f2)) {
                            try {
                                long ft1 = (Integer) features[f1].get(i);
                                long ft2 = ((Integer) features[f2].get(j));
                                long feat = ft1 | ft2 << 32;
                                features[index].add(feat);
                            }catch (Exception ex){
                                System.out.print("");
                            }
                        }
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
