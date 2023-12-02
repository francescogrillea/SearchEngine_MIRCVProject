package org.common;

import java.io.Serializable;

public class TermEntry implements Serializable {
    private int block_index;
    private long offset;
    private long length;    // TODO - change to integer to save space
    private final int document_frequency;

    public TermEntry(int block_index, long offset, long length) {
        this.block_index = block_index;
        this.offset = offset;
        this.length = length;
        this.document_frequency = 0;
    }

    public int getBlock_index() {
        return block_index;
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

    public void setBlock_index(int block_index) {
        this.block_index = block_index;
    }
    @Override
    public String toString() {
        return "TermEntry{" +
                "block_index=" + block_index +
                ", offset=" + offset +
                ", length=" + length +
                ", document_frequency=" + document_frequency +
                '}';
    }
}
