package edu.columbia.cs.rasooli.Reordering.Structures;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/11/15
 * Time: 7:15 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class Word {
    int realIndex;

    String wordForm;
    String fPos;
    String cPos;
    
    int wordFormIndex;
    int fPosIndex;
    int cPosIndex;

    public Word(int realIndex, String wordForm, String fPos, String cPos, IndexMaps maps) {
        this.realIndex = realIndex;
        this.wordForm = wordForm;
        if(maps.strMap.containsKey(wordForm))
            wordFormIndex = maps.strMap.get(wordForm);
        else
        wordFormIndex =-1;
        
        this.fPos = fPos;
        if(maps.strMap.containsKey(fPos))
            fPosIndex= maps.strMap.get(fPos);
        else
            fPosIndex=-1;
        
        this.cPos = cPos;
        this.fPos = fPos;
        if(maps.strMap.containsKey(cPos))
            cPosIndex= maps.strMap.get(cPos);
        else
            cPosIndex=-1;
    }

    public int getRealIndex() {
        return realIndex;
    }

    public String getWordForm() {
        return wordForm;
    }

    public String getfPos() {
        return fPos;
    }

    public String getcPos() {
        return cPos;
    }

    public int getWordFormIndex() {
        return wordFormIndex;
    }

    public int getfPosIndex() {
        return fPosIndex;
    }

    public int getcPosIndex() {
        return cPosIndex;
    }
}
