package org.common;

import org.common.encoding.EncoderInterface;
import org.common.encoding.GapEncoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostingList implements Iterable<Posting>{
    private  List<Posting> postingList;
    private  List<SkippingPointer> skipping_points;
    private int size;

    public PostingList() {
        this.postingList = new ArrayList<Posting>();
        this.skipping_points = new ArrayList<>();
        this.size = 0;
    }

    public PostingList(Posting posting){
        this.postingList = new ArrayList<>();
        this.skipping_points = new ArrayList<>();
        this.postingList.add(posting);
        this.size = 1;
    }

    public PostingList(ByteBuffer buffer, EncoderInterface encoder, boolean pointers){
        this.postingList = new ArrayList<>();
        this.skipping_points = new ArrayList<>();

        byte tf;
        int doc_id;
        SkippingPointer pointer = null;

        GapEncoder gap_encoder = new GapEncoder();

        while (buffer.hasRemaining()){
            int prec_doc_id=0;

            if(pointers){
                int max_doc_id = buffer.getInt();
                short offset = buffer.getShort();
                pointer = new SkippingPointer(max_doc_id, offset);
                this.skipping_points.add(pointer);
            }

            do{
                tf = buffer.get();  // return 1 byte


                doc_id = gap_encoder.decode(encoder.decode(buffer),prec_doc_id);
                prec_doc_id=doc_id;

                this.postingList.add(new Posting(doc_id, tf));
            }while (pointers && pointer.getMax_doc_id() > doc_id);

        }
        this.size = this.postingList.size();
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

    public void generatePointers(){
        int block_size = (int) Math.ceil(Math.sqrt(this.size));

        for (int i = block_size ; i < this.size; i = i+block_size){
            this.skipping_points.add(new SkippingPointer(this.postingList.get(i - 1).getDoc_id()));
        }
        this.skipping_points.add(new SkippingPointer(this.postingList.get(this.size - 1).getDoc_id()));
    }

    public int getSize(){
        return this.size;
    }

    public List<Posting> getPostingList() {
        return postingList;
    }

    public List<SkippingPointer> getSkipping_points() {
        return skipping_points;
    }

    public Posting getPosting(int i){
        return this.postingList.get(i);
    }

    @Override
    public Iterator<Posting> iterator() {
        return this.postingList.iterator();
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "postingList=" + postingList +
                ", skipping_points=" + skipping_points +
                '}';
    }
}