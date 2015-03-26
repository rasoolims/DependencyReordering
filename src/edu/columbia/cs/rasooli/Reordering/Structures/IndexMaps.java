package edu.columbia.cs.rasooli.Reordering.Structures;

import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/25/15
 * Time: 10:29 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class IndexMaps {
    public HashMap<String,Integer> strMap;
    public String[] reversedMap;

    public IndexMaps(HashMap<String, Integer> strMap) {
        this.strMap = strMap;
        reversedMap=new String[strMap.size()];
        for(String str:strMap.keySet())
            reversedMap[strMap.get(str)]=str;
    }
    
    
}
