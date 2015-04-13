package edu.columbia.cs.rasooli.Reordering.Structures;

import edu.columbia.cs.rasooli.Reordering.Enums.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
    int[] labels;
    String[] labelsStr;

    HashSet<Integer>[] deps;

    int[] order;

    public DependencyTree(Word[] words, int[] heads, int[] labels, String[] labelsStr) {
        this.words = words;
        this.heads = heads;
        this.labels = labels;
        this.labelsStr=labelsStr;
        if(labelsStr==null)
            System.out.print("ERROR!");

        deps = new HashSet[words.length];
        for (int i = 0; i < heads.length; i++)
            deps[i] = new HashSet<Integer>();

        for (int i = 0; i < heads.length; i++)
            if (heads[i] >= 0)
                deps[heads[i]].add(i);

        indices = new int[words.length];
        order = new int[words.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
            order[i] = i;
        }
    }

    public DependencyTree(Word[] words, int[] heads, int[] labels, int[] indices, int[] order, String[] labelsStr) {
        this.words = words;
        this.heads = heads;
        this.labels = labels;
        this.labelsStr=labelsStr;

        deps = new HashSet[words.length];
        for (int i = 0; i < heads.length; i++)
            deps[i] = new HashSet<Integer>();

        for (int i = 0; i < heads.length; i++)
            if (heads[i] >= 0)
                deps[heads[i]].add(i);

        this.indices = indices;
        this.order = order;
    }

    public boolean hasDep(int head) {
        return deps[indices[head]].size() > 0;
    }

    public int getCurrentIndex(int index) {
        return indices[index];
    }

    public int getCurrentHead(int index) {
        return heads[index];
    }

    public int getCurrentLabel(int index) {
        return labels[index];
    }

    public Word getCurrentWord(int index) {
        return words[index];
    }

    public HashSet<Integer> getDependents(int head) {
        return deps[head];
    }

    public DependencyTree getFullOrder(int[] instanceOrder, int head) throws Exception {
        DependencyTree tree = new DependencyTree(words, heads, labels, indices, order,labelsStr);

        HashSet<Integer> allHeadSubtree = getAllInSubtree(head);

        boolean ordered = true;
        for (int i = 0; i < instanceOrder.length - 1; i++) {
            if (instanceOrder[i] > instanceOrder[i + 1]) {
                ordered = false;
                break;
            }
        }

        if (ordered)
            return tree;

        int[] newOrder = new int[order.length];
        int[] newIndices = new int[indices.length];

        int nextIndex = order.length;
        for (int i = 0; i < order.length; i++) {
            if (allHeadSubtree.contains(order[i])) {
                int currentIndex = i;
                for (int j = 0; j < instanceOrder.length; j++) {
                    int dep = instanceOrder[j];
                    if (dep == head) {
                        newOrder[currentIndex] = dep;
                     //   System.out.println("s1\t"+currentIndex + "\t" + dep);
                        newIndices[dep] = currentIndex;
                        currentIndex++;
                    } else {
                        HashSet<Integer> allSub = getAllInSubtree(dep);
                        TreeSet<Integer> orderedSub = new TreeSet<Integer>();
                        for (int d : allSub)
                            orderedSub.add(indices[d]);
                        for (int d : orderedSub) {
                            try {
                                newOrder[currentIndex] = order[d];
                         //       System.out.println("s2\t"+currentIndex+"\t"+order[d]);
                                newIndices[order[d]] = currentIndex;
                            }catch (Exception ex){

                                throw new Exception(ex);
                            }
                            currentIndex++;
                        }
                    }
                }
                nextIndex = allHeadSubtree.size()+i;
                break;
            } else {
                newOrder[i] = order[i];
             //   System.out.println("s0\t"+i+"\t"+order[i]);
                newIndices[ order[i]] =i;
            }
        }

        for (int i = nextIndex; i < order.length; i++) {
            newOrder[i] = order[i];
          //  System.out.println("s3\t"+i+"\t"+order[i]);
            newIndices[  order[i] ] = i;
        }

        //sanity check
        TreeSet<Integer> set=new TreeSet<Integer>();
        for(int i=0;i<newOrder.length;i++)
            set.add(newOrder[i]);

        TreeSet<Integer> iset=new TreeSet<Integer>();
        for(int i=0;i<newIndices.length;i++)
            iset.add(newIndices[i]);
        
        if(set.size()<newOrder.length || iset.size()<newIndices.length) {
           System.out.println(set.size());
            System.out.println(iset.size());
            System.out.println(newOrder.length);
            for(int i=0;i<newOrder.length;i++)
                System.out.print(newOrder[i]+" ");
            System.out.println("\n" + newIndices.length);
            for(int i=0;i<order.length;i++)
                System.out.print(order[i]+" ");
            System.out.println("");
            for(int i=0;i<instanceOrder.length;i++)
                System.out.print(instanceOrder[i]+" ");
            System.out.println("ERROR!");

            System.exit(0);
        }
        
        order = newOrder;
        indices = newIndices;

        return tree;
    }

    private HashSet<Integer> getAllInSubtree(int head) {
        HashSet<Integer> deps = getDependents(head);

        HashSet<Integer> allSubtreeNodes = new HashSet<Integer>();
        allSubtreeNodes.add(head);

        for (int dep : deps)
            allSubtreeNodes.addAll(getAllInSubtree(dep));

        return allSubtreeNodes;
    }
    
    public String toPosString(int[] order, int head){
        StringBuilder builder=new StringBuilder();
        for(int i:order)
        if(i==head)
            builder.append("h^").append(words[i].fPos).append("|");
            else
            builder.append(words[i].fPos).append("|");
        return builder.toString();
    }
    
    public int size(){
        return words.length;
        
    }
    
    public String toConllOutput(final IndexMaps maps){
        StringBuilder builder=new StringBuilder();
        for(int j=1;j<getOrder().length;j++) {
            int i=getOrder()[j];
            String wordForm= getCurrentWord(i).getWordForm();
            String pos=   getCurrentWord(i).getfPos();
            String cpos=   getCurrentWord(i).getcPos();
            int head=getCurrentIndex(getCurrentHead(i));
            try {
                String label = labelsStr[i - 1];
                String out = j + "\t" + wordForm + "\t" + wordForm + "\t" + pos + "\t" + cpos + "\t_\t" + head + "\t" + label + "\t_\t_\n";
                builder.append(out);
            }catch (Exception ex){
                System.out.print("HERE!");
            }
        }
        builder.append("\n");
        return builder.toString();
    }

    public int[] getOrder() {
        return order;
    }

    //todo
    public ArrayList<Object>[] extractMainFeatures(int headIndex, int[] children) {
       int x=9+16*children.length;
        int size=(x*(x+1))/2+x;
        ArrayList<Object>[] features = new ArrayList[size];
        for (int i = 0; i < size; i++)
            features[i] = new ArrayList<Object>();

        int index = 0;

        //region head feature
        Word headWord = words[headIndex];
        features[index++].add(headWord.wordFormIndex);
        features[index++].add(headWord.cPosIndex);
        features[index++].add(headWord.fPosIndex);
        //endregion

        int currIndex = index;
        for (int i = 0; i < children.length; i++) {
           // index = currIndex;

            //region children
            Position position = Position.before;
            if (indices[children[i]] == indices[headIndex] - 1)
                position = Position.immediatelyBefore;
            else if (indices[children[i]] == indices[headIndex] + 1)
                position = Position.immediatelyAfter;
            else if (indices[children[i]] > indices[headIndex])
                position = Position.after;

            Word child = words[children[i]];

            features[index].add(position.value | (child.wordFormIndex << 2));
            features[index + 1].add(position.value | (child.cPosIndex << 2));
            features[index + 2].add(position.value | (child.fPosIndex << 2));
            features[index + 2].add(position.value | (labels[indices[children[i]]] << 2));
            //endregion

            //region there is a gap with head
            if (!position.equals(Position.immediatelyAfter) && !position.equals(Position.immediatelyBefore)) {
                int first, last;
                if (position.equals(Position.before)) {
                    last = order[indices[headIndex] - 1];
                    first = order[indices[children[i]] + 1];
                } else {
                    last = order[indices[children[i]] - 1];
                    first = order[indices[headIndex] + 1];
                }

                Word firstWord = words[first];
                Word lastWord = words[last];

                features[index + 3].add(firstWord.wordFormIndex);
                features[index + 4].add(firstWord.cPosIndex);
                features[index + 5].add(firstWord.fPosIndex);

                features[index + 6].add(lastWord.wordFormIndex);
                features[index + 7].add(lastWord.cPosIndex);
                features[index + 8].add(lastWord.fPosIndex);
            }
            index += 9;
            //endregion

            //region consecutive children
            if (i < children.length - 1 && children[i + 1] != headIndex && (indices[children[i + 1]] - indices[children[i]] > 1)) {
                int last = order[indices[order[i + 1]] - 1];
                int first = order[indices[order[i]] + 1];

                Word firstWord = words[first];
                Word lastWord = words[last];

                features[index].add(firstWord.wordFormIndex);
                features[index + 1].add(firstWord.cPosIndex);
                features[index + 2].add(firstWord.fPosIndex);

                features[index + 3].add(lastWord.wordFormIndex);
                features[index + 4].add(lastWord.cPosIndex);
                features[index + 5].add(lastWord.fPosIndex);
            }
            index += 6;
            //endregion
            
        }

        //region left and right siblings of head
        int headOfHead = getCurrentHead(headIndex);
        HashSet<Integer> siblings = getDependents(headOfHead);
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
            features[index].add(leftSibling.wordFormIndex);
            features[index + 1].add(leftSibling.cPosIndex);
            features[index + 2].add(leftSibling.fPosIndex);
        } else {
            features[index].add(-2);
            features[index + 1].add(-2);
            features[index + 2].add(-2);
        }

        if (headOrder < orderedSiblings.length - 1) {
            Word rightSibling = words[orderedSiblings[headOrder + 1]];
            features[index + 3].add(rightSibling.wordFormIndex);
            features[index + 4].add(rightSibling.cPosIndex);
            features[index + 5].add(rightSibling.fPosIndex);
        } else {
            features[index + 3].add(-2);
            features[index + 4].add(-2);
            features[index + 5].add(-2);
        }
        index += 6;
        //endregion


        //region bilexical features
        int l = index;
        for (int f1 = 0; f1 < l; f1++) {
            int featLen = features[f1].size();
            for (int f2 = f1; f2 < l; f2++) {
                int featLen2 = features[f2].size();
                for (i = 0; i < featLen; i++) {
                    for (int j = 0; j < featLen2; j++) {
                        if (!(i == j && f1 == f2)) {
                            try {
                                long ft1 = (Integer) features[f1].get(i);
                                long ft2 = ((Integer) features[f2].get(j));
                                long feat = ft1 | ft2 << 32;
                                features[index].add(feat);
                            } catch (Exception ex) {
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

    public ArrayList<Object>[] extractPivotFeatures(int headIndex, int child) {
        int size = 190;
        ArrayList<Object>[] features = new ArrayList[size];
        for (int i = 0; i < size; i++)
            features[i] = new ArrayList<Object>();

        int index = 0;

        //region head feature
        Word headWord = words[headIndex];
        features[index++].add(headWord.wordFormIndex);
        features[index++].add(headWord.cPosIndex);
        features[index++].add(headWord.fPosIndex);
        //endregion

        int currIndex = index;

        index = currIndex;

        //region children
        Position position = Position.before;
        if (indices[child] == indices[headIndex] - 1)
            position = Position.immediatelyBefore;
        else if (indices[child] == indices[headIndex] + 1)
            position = Position.immediatelyAfter;
        else if (indices[child] > indices[headIndex])
            position = Position.after;

        Word childWord = words[child];

        features[index].add(position.value | (childWord.wordFormIndex << 2));
        features[index + 1].add(position.value | (childWord.cPosIndex << 2));
        features[index + 2].add(position.value | (childWord.fPosIndex << 2));
        features[index + 2].add(position.value | (labels[indices[child]] << 2));
        //endregion

        //region there is a gap with head
        if (!position.equals(Position.immediatelyAfter) && !position.equals(Position.immediatelyBefore)) {
            int first, last;
            if (position.equals(Position.before)) {
                last = order[indices[headIndex] - 1];
                first = order[indices[child] + 1];
            } else {
                last = order[indices[child] - 1];
                first = order[indices[headIndex] + 1];
            }

            Word firstWord = words[first];
            Word lastWord = words[last];

            features[index + 3].add(firstWord.wordFormIndex);
            features[index + 4].add(firstWord.cPosIndex);
            features[index + 5].add(firstWord.fPosIndex);

            features[index + 6].add(lastWord.wordFormIndex);
            features[index + 7].add(lastWord.cPosIndex);
            features[index + 8].add(lastWord.fPosIndex);
        }
        index += 9;
        //endregion

        //region left and right siblings of head
        int headOfHead = getCurrentHead(headIndex);
        HashSet<Integer> siblings = getDependents(headOfHead);
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
            features[index].add(leftSibling.wordFormIndex);
            features[index + 1].add(leftSibling.cPosIndex);
            features[index + 2].add(leftSibling.fPosIndex);
        } else {
            features[index].add(-2);
            features[index + 1].add(-2);
            features[index + 2].add(-2);
        }

        if (headOrder < orderedSiblings.length - 1) {
            Word rightSibling = words[orderedSiblings[headOrder + 1]];
            features[index + 3].add(rightSibling.wordFormIndex);
            features[index + 4].add(rightSibling.cPosIndex);
            features[index + 5].add(rightSibling.fPosIndex);
        } else {
            features[index + 3].add(-2);
            features[index + 4].add(-2);
            features[index + 5].add(-2);
        }
        index += 6;
        //endregion


        //region bilexical features
        int l = index;
        for (int f1 = 0; f1 < l; f1++) {
            int featLen = features[f1].size();
            for (int f2 = f1; f2 < l; f2++) {
                int featLen2 = features[f2].size();
                for (i = 0; i < featLen; i++) {
                    for (int j = 0; j < featLen2; j++) {
                        if (!(i == j && f1 == f2)) {
                            try {
                                long ft1 = (Integer) features[f1].get(i);
                                long ft2 = ((Integer) features[f2].get(j));
                                long feat = ft1 | ft2 << 32;
                                features[index].add(feat);
                            } catch (Exception ex) {
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
}
