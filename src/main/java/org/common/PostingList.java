package org.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class PostingList implements Serializable {

    private static final long serialVersionUID = 1234L;
    private List<Posting> postingList;
    private List<Integer> skipping_points;
    private short size;
    private int block_size;

    public PostingList() {
        this.postingList = new ArrayList<Posting>();
        this.size = 0;
    }

    public PostingList(int doc_id) {
        this.postingList = new ArrayList<Posting>();
        //this.skipping_pointers = new ArrayList<>();
        this.postingList.add(new Posting(doc_id));
        this.size = 1;
    }

    public PostingList(Posting posting){
        this.postingList = new ArrayList<>();
        this.postingList.add(posting);
        this.size = 1;
    }

    public void addPosting(Posting posting){
        int index = postingList.indexOf(posting);
        if (index == -1){
            postingList.add(posting);
            this.size++;
        }
        else
            postingList.get(index).increaseTF();
    }

    public void appendPostings(PostingList new_postings){
        this.postingList.addAll(new_postings.getPostingList());
        this.size += new_postings.getSize();
    }

    public void compute_block_size(){
        this.block_size = (int) ceil(sqrt(size));
    }
    public void generate_skipping_points(){

        compute_block_size();

        for(int i = 0; i < this.getSize() / this.block_size; i++){
            int max_doc_id = this.postingList.get(((i + 1) * this.block_size) - 1).getDoc_id();
            //this.skipping_pointers.add(max_doc_id);
        }
    }

    public int SIZE(){
        return this.size * Posting.SIZE;
    }

    public int getSize(){
        return this.size;
    }

    public List<Posting> getPostingList() {
        return postingList;
    }

    @Override
    public String toString() {
        return "" + postingList;
    }
}