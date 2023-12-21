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
        ByteBuffer buffer = ByteBuffer.allocate(size * DocIndex.BYTES);

        List<Integer> sorted_doc_ids = new ArrayList<>(this.docIndex.keySet());
        Collections.sort(sorted_doc_ids);

        for(int doc_id : sorted_doc_ids){
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

    public HashMap<Integer, DocInfo> getDocIndex() {
        return docIndex;
    }

    // TODO - potrebbe servire a risparmiare spazio in memoria
    public void clear(){
        this.docIndex.clear();
    }

    @Override
    public String toString() {
        return "DocIndex{" + docIndex +
                '}';
    }
}
