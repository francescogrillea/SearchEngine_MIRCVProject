package org.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class PostingList implements Iterable, Serializable {

    private List<Posting> postingList;
    private List<Integer> skipping_pointers;

    private int block_size;

    public PostingList() {
        this.postingList = new ArrayList<Posting>();
        this.skipping_pointers = new ArrayList<>();
    }

    public PostingList(int doc_id) {
        this.postingList = new ArrayList<Posting>();
        this.skipping_pointers = new ArrayList<>();
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

    public void compute_block_size(){
        this.block_size = (int) ceil(sqrt(size()));
    }
    public void generate_skipping_points(){
        compute_block_size();

        for(int i = 0; i < this.size()/this.block_size; i++){
            int max_doc_id = this.postingList.get(((i + 1) * this.block_size) - 1).getDoc_id();
            this.skipping_pointers.add(max_doc_id);
        }
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
        return "" + postingList + "\t Skipping Points: " + skipping_pointers;
    }
}
