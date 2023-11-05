package org.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class PostingList implements Iterable, Serializable {

    private List<Posting> postingList;
    // private List<Integer> skipping_pointers;

    public PostingList() {
        this.postingList = new ArrayList<Posting>();
    }

    public PostingList(int doc_id) {
        this.postingList = new ArrayList<Posting>();
        this.postingList.add(new Posting(doc_id));
    }

    public void addPosting(int doc_id){
        // check if doc_id already present in the posting list.
        // if yes, just increase; if not, initialize new posting

        int index = postingList.indexOf(new Posting(doc_id));
        //int index = postingList.indexOf(doc_id); // TODO - check se funziona anche cosi'
        if (index == -1)
            postingList.add(new Posting(doc_id));
        else
            postingList.get(index).increaseTF();
    }

    // TODO - da togliere
    public void concatenatePostings(List<Posting> new_postings){
        this.postingList.addAll(new_postings);
    }

    public int concatenatePostings(PostingList new_postings){
        this.postingList.addAll(new_postings.getPostingList());
        return new_postings.size();
    }


    public int size(){
        return this.postingList.size();
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator spliterator() {
        return Iterable.super.spliterator();
    }


    public List<Posting> getPostingList() {
        return postingList;
    }

    @Override
    public String toString() {
        return "" + postingList;
    }
}
