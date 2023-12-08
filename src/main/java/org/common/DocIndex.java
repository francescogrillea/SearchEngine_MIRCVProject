package org.common;

import org.common.encoding.EncoderInterface;
import org.common.encoding.GapEncoder;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Consumer;

public class DocIndex implements Serializable {

    int doc_id;
    int doc_length;
    int pid;
    private long offset;
    private long length;

    public DocIndex(int doc_id, int doc_length, int pid) {
        this.doc_id = doc_id;
        this.doc_length = doc_length;
        this.pid = pid;
    }

    public DocIndex(){

    }

    public void setDoc_id(int doc_id) {
        System.out.println("doc_id: " + doc_id);
        this.doc_id = doc_id;
    }

    public void setDocLength(int doc_length) {
        this.doc_length = doc_length;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public ByteBuffer serialize(EncoderInterface encoder, int prec_doc_id, int prec_pid){

        GapEncoder gap_encoder = new GapEncoder();
        gap_encoder.setLast_doc_id(prec_doc_id);
        this.doc_id=gap_encoder.encode(this.doc_id);

        byte[] encoded = encoder.encode(this.doc_id);
        ByteBuffer byteBuffer = ByteBuffer.allocate((Byte.SIZE*3 + (Byte.SIZE * encoded.length)) / 8);

        byteBuffer.putInt(this.pid);
        byteBuffer.putInt(this.doc_length);
        byteBuffer.put(encoded);

        byteBuffer.flip();

        return byteBuffer;
    }


    public int getDoc_id() {
        return doc_id;
    }

    public int getDoc_length() {
        return doc_length;
    }

    public int getPid() {
        return pid;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "DocIndex{" +
                "doc_id=" + doc_id +
                ", doc_length=" + doc_length +
                ", pid=" + pid +
                '}';
    }
}
