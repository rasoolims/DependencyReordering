package edu.columbia.cs.rasooli.Reordering.IO;

import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;
import edu.columbia.cs.rasooli.Reordering.Structures.Word;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/24/15
 * Time: 10:36 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class DependencyReader {
    public static DependencyTree readNextBitextDependency(BufferedReader depReader,HashMap<String, String> universalMap) throws IOException {
        DependencyTree tree=null;
        String line1;
        while ((line1 = depReader.readLine()) != null) {
            if (line1.trim().length() == 0)
                continue;
            String[] w = line1.trim().split("\t");
            String[] t = depReader.readLine().trim().split("\t");
            String[] l = depReader.readLine().trim().split("\t");
            String[] h = depReader.readLine().trim().split("\t");

            assert w.length == t.length && t.length == l.length && h.length == l.length;

            String[] words = new String[w.length + 1];
            words[0] = "ROOT";
            for (int i = 0; i < w.length; i++)
                words[i + 1] = w[i];

            String[] tags = new String[t.length + 1];
            tags[0] = "ROOT";
            for (int i = 0; i < t.length; i++)
                tags[i + 1] = t[i];

            String[] labels = new String[l.length + 1];
            labels[0] = "";
            for (int i = 0; i < l.length; i++)
                labels[i + 1] = l[i];

            int[] heads = new int[h.length + 1];
            heads[0] = -1;
            for (int i = 0; i < h.length; i++)
                heads[i + 1] = Integer.parseInt(h[i]);

            Word[] wordStructs = new Word[words.length];
            for (int i = 0; i < words.length; i++) {
                wordStructs[i] = new Word(i, words[i], tags[i], universalMap.get(tags[i]));
            }


            tree = new DependencyTree(wordStructs, heads, labels);
        }
        return tree;
    }

}
