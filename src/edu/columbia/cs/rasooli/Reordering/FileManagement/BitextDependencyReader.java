package edu.columbia.cs.rasooli.Reordering.FileManagement;

import edu.columbia.cs.rasooli.Reordering.Structures.BitextDependency;
import edu.columbia.cs.rasooli.Reordering.Structures.DependencyTree;
import edu.columbia.cs.rasooli.Reordering.Structures.Word;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/12/15
 * Time: 12:21 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class BitextDependencyReader {
    private static HashMap<String, String> createUniversalMap(String universalPosPath) throws Exception {
        HashMap<String, String> universalMap = new HashMap<String, String>();
        BufferedReader universalPosReader = new BufferedReader(new FileReader(universalPosPath));
        String line;
        while ((line = universalPosReader.readLine()) != null) {
            String[] split = line.trim().split("\t");
            if (split.length == 2)
                universalMap.put(split[0], split[1]);
        }
        universalMap.put("ROOT", "ROOT");
        return universalMap;
    }

    /**
     * @param parsedFilePath        should have mst format
     * @param alignIntersectionPath should be a alignment intersection
     */
    public static ArrayList<BitextDependency> readFromBitext(String parsedFilePath, String alignIntersectionPath, String universalPosPath) throws Exception {
        HashMap<String, String> universalMap = createUniversalMap(universalPosPath);

        ArrayList<BitextDependency> data = new ArrayList<BitextDependency>();

        BufferedReader depReader = new BufferedReader(new FileReader(parsedFilePath));
        BufferedReader intersectionReader = new BufferedReader(new FileReader(alignIntersectionPath));

        String line1;
        int count = 0;
        int contextCount = 0;
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


            DependencyTree tree = new DependencyTree(wordStructs, heads, labels);

            String[] split = intersectionReader.readLine().trim().split(" ");
            SortedSet<Integer>[] alignedWords = new SortedSet[words.length];
            for (int i = 0; i < alignedWords.length; i++)
                alignedWords[i] = new TreeSet<Integer>();

            for (String s : split) {
                String[] inds = s.split("-");
                int d1 = Integer.parseInt(inds[0]);
                assert d1 < words.length;
                alignedWords[d1].add(Integer.parseInt(inds[1]));
            }

            BitextDependency bitext = new BitextDependency(alignedWords, tree);
            if (bitext.getTrainableHeads().size() > 0)
                data.add(bitext);
            contextCount += bitext.getTrainableHeads().size();
            count++;
            if (count % 10000 == 0)
                System.out.print(count + "(" + data.size() + ":" + contextCount + ")...");
        }
        System.out.print(count + "\n");
        return data;
    }

}
