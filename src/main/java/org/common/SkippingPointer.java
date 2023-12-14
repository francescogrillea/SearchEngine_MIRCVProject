package org.common;

import java.nio.ByteBuffer;

public class SkippingPointer {

    private final int max_doc_id;
    private short block_length_docIDs;  // block size (in bytes) of the corresponding docID list
    private short block_length_TFs;     // block size (in bytes) of the corresponding TF list

    public static int SIZE = Integer.BYTES + Short.BYTES + Short.BYTES;

    public SkippingPointer(int max_doc_id) {
        this.max_doc_id = max_doc_id;
    }

    public SkippingPointer(ByteBuffer buffer){
        this.max_doc_id = buffer.getInt();
        this.block_length_docIDs = buffer.getShort();
        this.block_length_TFs = buffer.getShort();
    }

    public ByteBuffer serialize(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE);
        byteBuffer.putInt(max_doc_id);
        byteBuffer.putShort(block_length_docIDs);
        byteBuffer.putShort(block_length_TFs);
        byteBuffer.flip();

        return byteBuffer;
    }

    public void setBlock_length_docIDs(short block_length_docIDs) {
        this.block_length_docIDs = block_length_docIDs;
    }

    public void setBlock_length_TFs(short block_length_TFs) {
        this.block_length_TFs = block_length_TFs;
    }

    public int getMax_doc_id() {
        return max_doc_id;
    }

    public short getBlock_length_docIDs() {
        return block_length_docIDs;
    }

    public short getBlock_length_TFs() {
        return block_length_TFs;
    }

    @Override
    public String toString() {
        return "SkippingPointer{" +
                "max_doc_id=" + max_doc_id +
                ", block_length_docIDs=" + block_length_docIDs +
                ", block_length_TFs=" + block_length_TFs +
                '}';
    }
}
