package org.common;

import java.nio.ByteBuffer;

/**
 * The DocInfo class represents information associated with a document, including
 * the original identifier and the length of the document.
 */
public class DocInfo {
    private final int pid;  // the original identifier of the document
    private final int length;   // the length of the document.
    static final int BYTES = Integer.BYTES + Integer.BYTES; // the number of bytes required to represent a serialized DocInfo object

    /**
     * Constructs a DocInfo object with the specified process ID and document length.
     *
     * @param pid    The process ID associated with the document.
     * @param length The length of the document.
     */
    public DocInfo(int pid, int length) {
        this.pid = pid;
        this.length = length;
    }

    /**
     * Constructs a DocInfo object by deserializing data from a ByteBuffer.
     *
     * @param buffer The ByteBuffer containing serialized DocInfo data.
     */
    public DocInfo(ByteBuffer buffer){
        this.pid = buffer.getInt();
        this.length = buffer.getInt();
    }

    /**
     * Retrieves the process ID associated with the document.
     *
     * @return The process ID (pid) of the document.
     */
    public int getPid() {
        return pid;
    }

    /**
     * Retrieves the length of the document.
     *
     * @return The length of the document.
     */
    public int getLength() {
        return length;
    }

    /**
     * Serializes the DocInfo object into a ByteBuffer.
     *
     * @return A ByteBuffer containing the serialized DocInfo data.
     */
    public ByteBuffer serialize(){
        ByteBuffer buffer = ByteBuffer.allocate(BYTES);
        buffer.putInt(this.pid);
        buffer.putInt(this.length);
        buffer.flip();
        return buffer;
    }

    /**
     * Returns a string representation of the DocInfo object, mainly for debugging purposes.
     *
     * @return A string representation of the DocInfo object.
     */
    @Override
    public String toString() {
        return "DocInfo{" +
                "pid=" + pid +
                ", length=" + length +
                '}';
    }
}
