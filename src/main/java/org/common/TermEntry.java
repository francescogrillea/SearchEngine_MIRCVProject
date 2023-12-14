package org.common;

import java.nio.ByteBuffer;

public class TermEntry {
    private int block_index;
    private long offset;
    private long length;    // TODO - change to integer to save space
    private final int document_frequency;
    static final int BYTES = Integer.BYTES + Long.BYTES + Long.BYTES + Integer.BYTES;   // TODO - change legth to int and add document frequency

    public TermEntry(int block_index, long offset, long length, int document_frequency) {
        this.block_index = block_index;
        this.offset = offset;
        this.length = length;
        this.document_frequency = document_frequency;
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

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setLength(long length) {
        this.length = length;
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

    public ByteBuffer serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(TermEntry.BYTES);
        byteBuffer.putInt(this.block_index);
        byteBuffer.putLong(this.offset);
        byteBuffer.putLong(this.length);    // TODO - change to INT
        byteBuffer.putInt(this.document_frequency);

        byteBuffer.flip();
        return byteBuffer;
    }
}
