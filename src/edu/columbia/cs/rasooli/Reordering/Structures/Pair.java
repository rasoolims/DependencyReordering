package edu.columbia.cs.rasooli.Reordering.Structures;

import java.io.Serializable;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 3/17/15
 * Time: 10:43 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class Pair<T1, T2> implements Comparable, Cloneable, Serializable {

    public T1 first;
    public T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    @Override
    public Pair<T1, T2> clone() {
        return new Pair<T1, T2>(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair))
            return false;
        Pair pair = (Pair) o;

        if (pair.second == null)
            if (second == null)
                return pair.first.equals(first);
            else
                return false;
        if (second == null)
            return false;
        return pair.first.equals(first) && pair.second.equals(second);
    }

    @Override
    public int hashCode() {
        int firstHash = 0;
        int secondHash = 0;
        if (first != null)
            firstHash = first.hashCode();
        if (second != null)
            secondHash = second.hashCode();
        return firstHash + secondHash;
    }

    @Override
    public int compareTo(Object o) {
        if (equals(o))
            return 0;
        return hashCode() - o.hashCode();
    }
}