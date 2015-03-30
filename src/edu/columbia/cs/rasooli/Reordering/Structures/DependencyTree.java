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

    public DependencyTree getFullOrder(int[] instanceOrder, int head) throws Exception {
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
    
    public String toConllOutput(){
        StringBuilder builder=new StringBuilder();
        for(int j=1;j<getOrder().length;j++) {
            int i=getOrder()[j];
            String wordForm= getCurrentWord(i).getWordForm();
            String pos=   getCurrentWord(i).getfPos();
            String cpos=   getCurrentWord(i).getcPos();
            int head=getCurrentIndex(getCurrentHead(i));
            String label=getCurrentLabel(j);
            String out=j+"\t"+wordForm+"\t"+wordForm+"\t"+pos+"\t"+cpos+ "\t_\t"+head+"\t"+label+"\t_\t_\n";
            builder.append(out);
        }
        builder.append("\n");
        return builder.toString();
    }

    public int[] getOrder() {
        return order;
    }
}
