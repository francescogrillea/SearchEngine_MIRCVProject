package org.common;

import java.io.Serializable;
import java.util.Objects;

public class Posting implements Comparable<Integer>, Serializable {

    private int doc_id;
    private short term_frequency = 0;

    public Posting(int doc_id) {
        this.doc_id = doc_id;
        this.term_frequency += 1;
    }

    public int getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(int doc_id) {
        this.doc_id = doc_id;
    }

    public int getTerm_frequency() {
        return term_frequency;
    }

    public void setTerm_frequency(short term_frequency) {
        this.term_frequency = term_frequency;
    }

    @Override
    public int compareTo(Integer o) {
        return Integer.compare(this.doc_id, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;

        if (o instanceof Posting){
            Posting posting = (Posting) o;
            return this.doc_id == posting.doc_id;
        }
        if(o instanceof Integer){
            return this.doc_id == (int) o;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doc_id);
    }

    public void increaseTF(){
        this.term_frequency++;
    }

    @Override
    public String toString() {
        return "{doc_id=" + doc_id +
                ", term_frequency=" + term_frequency +
                '}';
    }
}
