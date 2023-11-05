package org.common;

import java.io.Serializable;

public class LexiconInfo implements Serializable {

    private int offset;
    private int document_frequency; // how many documents contains the term
    private int collection_frequency;   // TODO - useless?

    public LexiconInfo(int offset, int df) {
        this.offset = offset;
        this.document_frequency = df;
    }

    public void increaseDF(){
        this.document_frequency++;
    }

    public void increaseCF(){
        this.collection_frequency++;
    }

    @Override
    public String toString() {
        return "{from=" + offset + "}";
    }
}
