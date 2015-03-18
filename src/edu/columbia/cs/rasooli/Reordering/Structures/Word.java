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

    public Word(int realIndex, String wordForm, String fPos, String cPos) {
        this.realIndex = realIndex;
        this.wordForm = wordForm;
        this.fPos = fPos;
        this.cPos = cPos;
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
}
