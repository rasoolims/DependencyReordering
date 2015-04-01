package edu.columbia.cs.rasooli.Reordering.Enums;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 4/1/15
 * Time: 2:47 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public enum ClassifierType {
    perceptron(0),
    pegasos(1);

    public int value;

    ClassifierType(int value) {
        this.value=value;
    }
    
    @Override
    public String toString(){
        if(this==ClassifierType.perceptron)
            return "perceptron";
        else   if(this==ClassifierType.pegasos)
            return "pegasos";
        return "---";
        
    }

}
