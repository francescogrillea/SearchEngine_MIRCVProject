package org.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DocIndexList implements Iterable<DocIndex>, Serializable {

    private List<DocIndex> docIndexList;
    private int size;

    public DocIndexList() {
        this.docIndexList = new ArrayList<>();
    }

    public void addDocIndex(DocIndex docIndex){
        this.size++;
        this.docIndexList.add(docIndex);
    }

    public void addEntryList(DocIndexList entries){
        this.docIndexList.addAll(entries.getDocIndexList());
        size+= entries.size();
    }

    public int size(){
        return this.size;
    }

    public List<DocIndex> getDocIndexList() {
        return docIndexList;
    }

    public Iterator<DocIndex> iterator() {
        return this.docIndexList.iterator();
    }

    @Override
    public String toString() {
        return "DocIndexList{" +
                "docIndexList=" + docIndexList.toString() +
                ", size=" + size +
                '}';
    }

    public void merge(DocIndexList doc_index){
        this.docIndexList.addAll(doc_index.getDocIndexList());
    }
}
