package org.common;

import java.nio.ByteBuffer;

public class SkippingPointer {

    private int max_doc_id;
    private short offset;

    public static int SIZE = (Integer.SIZE + Short.SIZE) / 8;

    public SkippingPointer(int max_doc_id) {
        this.max_doc_id = max_doc_id;
    }

    public SkippingPointer(int max_doc_id, short offset) {
        this.max_doc_id = max_doc_id;
        this.offset = offset;
    }

    public ByteBuffer serialize(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 * Integer.BYTES);
        byteBuffer.putInt(max_doc_id);
        byteBuffer.putShort(offset);
        byteBuffer.flip();

        return byteBuffer;
    }

    public int getMax_doc_id() {
        return max_doc_id;
    }

    public void setMax_doc_id(int max_doc_id) {
        this.max_doc_id = max_doc_id;
    }

    public short getOffset() {
        return offset;
    }

    public void setOffset(short offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "max_doc_id=" + max_doc_id +
               ", offset=" + offset +
               '}';
    }
}
