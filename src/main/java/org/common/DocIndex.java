package org.common;

import java.nio.ByteBuffer;
import java.util.*;


/**
 * The DocIndex class represents the document index structure for storing document information.
 * It associates document IDs with corresponding DocInfo objects, which include
 * information such as term frequency and document length.
 */
public class DocIndex {

    private final HashMap<Integer, DocInfo> docIndex;   // the underlying HashMap for storing document IDs and corresponding DocInfo objects
    public static final int BYTES = Integer.BYTES + DocInfo.BYTES;  // the number of bytes required to represent a single entry in the DocIndex


    /**
     * Constructs an empty DocIndex with an underlying HashMap for document storage.
     */
    public DocIndex() {
        this.docIndex = new HashMap<>();
    }

    /**
     * Constructs a DocIndex by deserializing data from a ByteBuffer.
     *
     * @param buffer The ByteBuffer containing serialized DocIndex data.
     */
    public DocIndex(ByteBuffer buffer){
        this.docIndex = new HashMap<>();
        int key;
        DocInfo value;
        while (buffer.hasRemaining()){
            key = buffer.getInt();
            value = new DocInfo(buffer.getInt(), buffer.getInt());
            this.docIndex.put(key, value);
        }
    }

    /**
     * Adds document information to the DocIndex.
     *
     * @param doc_id    The document ID to be added.
     * @param doc_info  The DocInfo object associated with the document ID.
     */
    public void add(Integer doc_id, DocInfo doc_info){
        this.docIndex.put(doc_id, doc_info);
    }

    /**
     * Retrieves the DocInfo object associated with a given document ID.
     *
     * @param doc_id The document ID for which to retrieve DocInfo.
     * @return The DocInfo object associated with the specified document ID.
     */
    public DocInfo get(Integer doc_id){
        return this.docIndex.get(doc_id);
    }

    /**
     * Serializes the DocIndex into a ByteBuffer.
     *
     * @return A ByteBuffer containing the serialized DocIndex data.
     */
    public ByteBuffer serialize(){
        int size = this.docIndex.size();
        ByteBuffer buffer = ByteBuffer.allocate(size * DocIndex.BYTES);

        List<Integer> sorted_doc_ids = new ArrayList<>(this.docIndex.keySet());
        // sort docIndex by doc_id, useful in retrieving the PID directly from file knwoing the docID
        Collections.sort(sorted_doc_ids);

        for(int doc_id : sorted_doc_ids){
            buffer.putInt(doc_id);
            buffer.put(this.docIndex.get(doc_id).serialize());
        }

        buffer.flip();
        return buffer;
    }

    /**
     * Clear the DocIndex to save space
     */
    public void clear() {
        this.docIndex.clear();
    }

    /**
     * Returns a string representation of the DocIndex, mainly for debugging purposes.
     *
     * @return A string representation of the DocIndex.
     */
    @Override
    public String toString() {
        return "DocIndex{" + docIndex +
                '}';
    }

    public int getSize(){
        return this.docIndex.size();
    }

}
