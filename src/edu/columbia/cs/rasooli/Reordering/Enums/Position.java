package edu.columbia.cs.rasooli.Reordering.Enums;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/26/15
 * Time: 12:07 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public enum Position {
    immediatelyBefore(0),
    before(1),
    immediatelyAfter(2),
    after(3);

    public int value;

    Position(int value) {
        this.value=value;
    }
    
}
