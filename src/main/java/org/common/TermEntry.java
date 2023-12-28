package org.common;

import java.nio.ByteBuffer;

/**
 * The TermEntry class represents an entry for a term in an inverted index. Instances of
 * this class are used to store metadata about terms in information retrieval systems.
 */
public class TermEntry {
    private int chunk_index;    // the chunk index where the term is located. Useful in merging intermediate postings lists
    private final long offset;    // the offset within the file where the term's postings start.
    private final long length;    // the length of the term's postings within the file. It can be changed to int to save space
    private final int document_frequency;   // the document frequency of the term in the collection, i.e. how many documents contain that term
    private float tfidf_upper_bound;    // the upper bound for TF-IDF score of the term
    private float bm25_upper_bound; // the upper bound for BM25 score of the term
    static final int BYTES = Integer.BYTES + Long.BYTES + Long.BYTES + Integer.BYTES + Float.BYTES + Float.BYTES;   // the number of bytes required to serialize a TermEntry object

    /**
     * Constructs a TermEntry with specified attributes, initializing upper bounds to zero.
     *
     * @param chunk_index         The chunk index where the term is located.
     * @param offset              The offset within the block where the term's postings start.
     * @param length              The length of the term's postings within the block.
     * @param document_frequency The document frequency of the term in the collection.
     */
    public TermEntry(int chunk_index, long offset, long length, int document_frequency) {
        this.chunk_index = chunk_index;
        this.offset = offset;
        this.length = length;
        this.document_frequency = document_frequency;
        this.tfidf_upper_bound = 0;
        this.bm25_upper_bound = 0;
    }

    /**
     * Constructs a TermEntry with specified attributes, including upper bounds.
     *
     * @param chunk_index         The chunk index where the term is located.
     * @param offset              The offset within the block where the term's postings start.
     * @param length              The length of the term's postings within the block.
     * @param document_frequency The document frequency of the term in the collection.
     * @param tfidf_upper_bound   The upper bound for TF-IDF score of the term.
     * @param bm25_upper_bound    The upper bound for BM25 score of the term.
     */
    public TermEntry(int chunk_index, long offset, long length, int document_frequency, float tfidf_upper_bound, float bm25_upper_bound) {
        this.chunk_index = chunk_index;
        this.offset = offset;
        this.length = length;
        this.document_frequency = document_frequency;
        this.tfidf_upper_bound = tfidf_upper_bound;
        this.bm25_upper_bound = bm25_upper_bound;
    }

    /**
     * Serializes the TermEntry into a ByteBuffer for write it to disk.
     *
     * @return A ByteBuffer containing the serialized TermEntry.
     */
    public ByteBuffer serialize() {

        ByteBuffer byteBuffer = ByteBuffer.allocate(TermEntry.BYTES);
        byteBuffer.putInt(this.chunk_index);
        byteBuffer.putLong(this.offset);
        byteBuffer.putLong(this.length);
        byteBuffer.putInt(this.document_frequency);

        byteBuffer.putFloat(this.tfidf_upper_bound);
        byteBuffer.putFloat(this.bm25_upper_bound);

        byteBuffer.flip();
        return byteBuffer;
    }

    public int getBlock_index() {
        return chunk_index;
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
        this.chunk_index = block_index;
    }

    public void setTfidf_upper_bound(float tfidf_upper_bound) {
        this.tfidf_upper_bound = tfidf_upper_bound;
    }

    public void setBm25_upper_bound(float bm25_upper_bound) {
        this.bm25_upper_bound = bm25_upper_bound;
    }

    @Override
    public String toString() {
        return "TermEntry{" +
                "block_index=" + chunk_index +
                ", offset=" + offset +
                ", length=" + length +
                ", document_frequency=" + document_frequency +
                ", tfidf_upper_bound=" + tfidf_upper_bound +
                ", bm25_upper_bound=" + bm25_upper_bound +
                '}';
    }
}
