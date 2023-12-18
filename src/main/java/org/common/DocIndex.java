package org.common;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class DocIndex {

    // TODO - valuatre se fare una lista ordinata
    private final HashMap<Integer, DocInfo> docIndex;
    public static final int BYTES = Integer.BYTES + DocInfo.BYTES;

    public DocIndex() {
        this.docIndex = new HashMap<>();
    }

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

    public void add(Integer doc_id, DocInfo doc_info){
        this.docIndex.put(doc_id, doc_info);
    }

    public DocInfo get(Integer doc_id){
        return this.docIndex.get(doc_id);
    }

    public ByteBuffer serialize(){
        int size = this.docIndex.size();
        System.out.println(size);
        ByteBuffer buffer = ByteBuffer.allocate(size * DocIndex.BYTES);

        for(int doc_id : this.docIndex.keySet()){
            buffer.putInt(doc_id);
            buffer.put(this.docIndex.get(doc_id).serialize());
        }

        buffer.flip();
        return buffer;
    }

    public List<Integer> getPids(List<Integer> doc_ids){
        List<Integer> list = new ArrayList<>();
        for(int doc_id : doc_ids)
            list.add(this.get(doc_id).getPid());
        return list;
    }

    @Override
    public String toString() {
        return "DocIndex{" + docIndex +
                '}';
    }
}
