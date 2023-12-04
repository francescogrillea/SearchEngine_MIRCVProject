package org.common;

import org.common.encoding.EncoderInterface;
import org.common.encoding.GapEncoder;

import java.nio.ByteBuffer;
import java.util.Objects;

public class Posting implements Comparable<Integer> {

    private int doc_id;
    private byte term_frequency = 0;

    public Posting(int doc_id) {
        this.doc_id = doc_id;
        this.term_frequency += 1;
    }

    public Posting(int doc_id, byte term_frequency){
        this.doc_id = doc_id;
        this.term_frequency = term_frequency;
    }

    public ByteBuffer serialize(EncoderInterface encoder, int prec_doc_id){

        GapEncoder gap_encoder = new GapEncoder();
        this.doc_id=gap_encoder.encode(prec_doc_id,this.doc_id);

        byte[] encoded = encoder.encode(this.doc_id);
        ByteBuffer byteBuffer = ByteBuffer.allocate((Byte.SIZE + (Byte.SIZE * encoded.length)) / 8);

        byteBuffer.put(term_frequency);
        byteBuffer.put(encoded);

        byteBuffer.flip();

        return byteBuffer;
    }

    public int getDoc_id() {
        return doc_id;
    }

    @Override
    public int compareTo(Integer o) {
        return Integer.compare(this.doc_id, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;

        if (o instanceof Posting){
            Posting posting = (Posting) o;
            return this.doc_id == posting.doc_id;
        }
        if(o instanceof Integer){
            return this.doc_id == (int) o;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doc_id);
    }

    public void increaseTF(){
        this.term_frequency++;
    }

    @Override
    public String toString() {
        return "{doc_id=" + doc_id +
                ", term_frequency=" + term_frequency +
                '}';
    }
}
