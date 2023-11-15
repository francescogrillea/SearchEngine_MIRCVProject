package org.common;

import java.io.Serializable;

public class TermEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String term;
    private long offset;
    private long length;
    private int document_frequency;


    public TermEntry(String term) {
        this.term = term;
        this.document_frequency = 0;
    }

    public void increaseDF(){
        this.document_frequency++;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setDocument_frequency(int document_frequency) {
        this.document_frequency = document_frequency;
    }

    public String getTerm() {
        return term;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    public int getDocument_frequency() {
        return document_frequency;
    }

    @Override
    public String toString() {
        return "TermEntry{" +
                "term='" + term + '\'' +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }
}
