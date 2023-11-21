package org.common;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostingList implements Iterable<Posting>{
    private List<Posting> postingList;
    private List<Integer> skipping_points;
    private short size;

    public PostingList() {
        this.postingList = new ArrayList<Posting>();
        this.size = 0;
    }

    public PostingList(int doc_id) {
        this.postingList = new ArrayList<Posting>();
        this.postingList.add(new Posting(doc_id));
        this.size = 1;
    }

    public PostingList(Posting posting){
        this.postingList = new ArrayList<>();
        this.postingList.add(posting);
        this.size = 1;
    }

    public PostingList(ByteBuffer buffer, EncoderInterface encoder){
        this.postingList = new ArrayList<>();

        byte tf;
        int doc_id;
        byte[] b;
        int i;
        int position_tmp;

        while (buffer.hasRemaining()){
            tf = buffer.get();  // return 1 byte
            position_tmp = buffer.position();
            for(i = 0; (buffer.get()) > 0; i++){}
            b = new byte[i + 1];

            buffer.position(position_tmp);
            buffer.get(b);

            doc_id = encoder.decode(b);   // return 4 bytes
            this.postingList.add(new Posting(doc_id, tf));
        }
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

    public int getSize(){
        return this.size;
    }

    public List<Posting> getPostingList() {
        return postingList;
    }

    @Override
    public Iterator<Posting> iterator() {
        return this.postingList.iterator();
    }

    @Override
    public String toString() {
        return "" + postingList;
    }

}