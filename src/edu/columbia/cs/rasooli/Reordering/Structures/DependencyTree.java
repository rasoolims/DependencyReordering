package edu.columbia.cs.rasooli.Reordering.Structures;

import java.util.HashSet;
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
    String[] labels;

    HashSet<Integer>[] deps;

    int[] order;

    public DependencyTree(Word[] words, int[] heads, String[] labels) {
        this.words = words;
        this.heads = heads;
        this.labels = labels;

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

    public DependencyTree(Word[] words, int[] heads, String[] labels, int[] indices, int[] order) {
        this.words = words;
        this.heads = heads;
        this.labels = labels;

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

    public String getCurrentLabel(int index) {
        return labels[index];
    }

    public Word getCurrentWord(int index) {
        return words[index];
    }

    public HashSet<Integer> getDependents(int head) {
        return deps[head];
    }

    public DependencyTree getFullOrder(int[] instanceOrder, int head) {
        DependencyTree tree = new DependencyTree(words, heads, labels, indices, order);

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
                        newOrder[currentIndex] = order[dep];
                        newIndices[order[dep]] = currentIndex;
                        currentIndex++;
                    } else {
                        HashSet<Integer> allSub = getAllInSubtree(dep);
                        TreeSet<Integer> orderedSub = new TreeSet<Integer>();
                        for (int d : allSub)
                            orderedSub.add(indices[d]);
                        for (int d : orderedSub) {
                            newOrder[currentIndex] = order[d];
                            newIndices[order[d]] = currentIndex;
                            currentIndex++;
                        }
                    }
                }
                nextIndex = i + allHeadSubtree.size();
                break;
            } else {
                newOrder[i] = order[i];
                newIndices[i] = indices[i];
            }
        }

        for (int i = nextIndex; i < order.length; i++) {
            newOrder[i] = order[i];
            newIndices[i] = indices[i];
        }

        tree.order = newOrder;
        tree.indices = newIndices;

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
}
