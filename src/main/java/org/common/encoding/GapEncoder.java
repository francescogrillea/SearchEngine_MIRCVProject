package org.common.encoding;

// TODO provare a fare in modo che implementi EncoderInterface
public class GapEncoder {

    private int last_doc_id;
    public GapEncoder() {
        this.last_doc_id = 0;
    }

    public int encode(int doc_id){
        int out = doc_id - this.last_doc_id;
        this.last_doc_id = doc_id;
        return out;
    }

    public int decode(int doc_id){
        int out = this.last_doc_id + doc_id;
        this.last_doc_id = out;
        return out;
    }

    public void setLast_doc_id(int last_doc_id) {
        this.last_doc_id = last_doc_id;
    }
}
