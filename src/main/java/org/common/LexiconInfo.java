package org.common;

public class LexiconInfo {

    private int offset;
    private int length;     // N
    private int document_frequency; // how many documents contains the term
    private int collection_frequency;   // TODO - useless?

    public LexiconInfo(int offset, int length) {
        this.offset = offset;
        this.length = length;
        this.document_frequency = length;
    }

    public void increaseDF(){
        this.document_frequency++;
    }

    public void increaseCF(){
        this.collection_frequency++;
    }

    @Override
    public String toString() {
        return "{from=" + offset +
                ", to=" + length + "}";
    }
}
