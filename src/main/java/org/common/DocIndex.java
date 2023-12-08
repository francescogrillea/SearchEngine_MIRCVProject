package org.common;

import org.common.encoding.EncoderInterface;
import org.common.encoding.GapEncoder;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class DocIndex {

    private final HashMap<Integer, DocInfo> docIndex;

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
        // TODO - add encoder
        int size = this.docIndex.size();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * size + DocInfo.BYTES * size);

        for (Map.Entry<Integer, DocInfo> entry : this.docIndex.entrySet()){
            buffer.putInt(entry.getKey());
            buffer.put(entry.getValue().serialize());
        }

        buffer.flip();

        return buffer;
    }

    @Override
    public String toString() {
        return "DocIndex{" + docIndex +
                '}';
    }
}
