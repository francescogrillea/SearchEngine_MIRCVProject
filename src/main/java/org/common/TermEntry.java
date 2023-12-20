package org.common;

import java.nio.ByteBuffer;

public class TermEntry {
    private int block_index;
    private long offset;
    private long length;    // TODO - change to integer to save space
    private final int document_frequency;
    private float tfidf_upper_bound;
    private float bm25_upper_bound;
    static final int BYTES = Integer.BYTES + Long.BYTES + Long.BYTES + Integer.BYTES + Float.BYTES + Float.BYTES;   // TODO - change legth to int and add document frequency

    public TermEntry(int block_index, long offset, long length, int document_frequency) {
        this.block_index = block_index;
        this.offset = offset;
        this.length = length;
        this.document_frequency = document_frequency;
        this.tfidf_upper_bound = 0;
        this.bm25_upper_bound = 0;
    }

    public TermEntry(int block_index, long offset, long length, int document_frequency, float tfidf_upper_bound, float bm25_upper_bound) {
        this.block_index = block_index;
        this.offset = offset;
        this.length = length;
        this.document_frequency = document_frequency;
        this.tfidf_upper_bound = tfidf_upper_bound;
        this.bm25_upper_bound = bm25_upper_bound;
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

    public float getTfidf_upper_bound() {
        return tfidf_upper_bound;
    }

    public float getBm25_upper_bound() {
        return bm25_upper_bound;
    }

    public void setBlock_index(int block_index) {
        this.block_index = block_index;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setTfidf_upper_bound(float tfidf_upper_bound) {
        this.tfidf_upper_bound = tfidf_upper_bound;
    }

    public void setBm25_upper_bound(float bm25_upper_bound) {
        this.bm25_upper_bound = bm25_upper_bound;
    }

    public ByteBuffer serialize() {

        ByteBuffer byteBuffer = ByteBuffer.allocate(TermEntry.BYTES);
        byteBuffer.putInt(this.block_index);
        byteBuffer.putLong(this.offset);
        byteBuffer.putLong(this.length);    // TODO - change to INT
        byteBuffer.putInt(this.document_frequency);

        byteBuffer.putFloat(this.tfidf_upper_bound);
        byteBuffer.putFloat(this.bm25_upper_bound);

        byteBuffer.flip();
        return byteBuffer;
    }

    @Override
    public String toString() {
        return "TermEntry{" +
                "block_index=" + block_index +
                ", offset=" + offset +
                ", length=" + length +
                ", document_frequency=" + document_frequency +
                ", tfidf_upper_bound=" + tfidf_upper_bound +
                ", bm25_upper_bound=" + bm25_upper_bound +
                '}';
    }
}
