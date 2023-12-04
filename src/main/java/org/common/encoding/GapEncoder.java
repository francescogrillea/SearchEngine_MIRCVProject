package org.common.encoding;

public class GapEncoder {

    public GapEncoder() {

    }

    public int encode(int prec_doc_id, int doc_id){
        int gap =  doc_id - prec_doc_id;
        System.out.println("ENCODING DOCID: "+ doc_id+ " IN GAP "+ gap + " with PREC DOCID "+ prec_doc_id);
        return gap;
    }

    public int decode(int gap, int prec_doc_id){
        int doc_id=gap+prec_doc_id;
        System.out.println("DECODING GAP: "+ gap+ " IN DOCID "+ doc_id + " with PREC DOCID "+ prec_doc_id);
        return doc_id;
    }


}
