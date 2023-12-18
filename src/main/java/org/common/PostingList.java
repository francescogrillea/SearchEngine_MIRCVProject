package org.common;

import org.common.encoding.EncoderInterface;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostingList {
    private final List<Integer> doc_ids;
    private final List<Integer> term_frequencies;
    private final List<SkippingPointer> skipping_points;
    private int block_size;

    public PostingList() {
        this.doc_ids = new ArrayList<>();
        this.term_frequencies = new ArrayList<>();
        this.skipping_points = new ArrayList<>();
        // TODO - block_size = 0 ?
    }

    public PostingList(int doc_id){
        this.doc_ids = new ArrayList<>();
        this.term_frequencies = new ArrayList<>();
        this.skipping_points = new ArrayList<>();

        addPosting(doc_id);
    }

    public PostingList(ByteBuffer buffer){
        /*
            TODO - per renderla generica sia nelle intermediate che in quella normale,
                fare un check
                    if lists are null -> instatiate
                tanto devo comunque appendere blocco dopo blocco
         */
        this.doc_ids = new ArrayList<>();
        this.term_frequencies = new ArrayList<>();
        this.skipping_points = new ArrayList<>();

        int value;
        while((value = buffer.getInt()) > 0){
            this.doc_ids.add(value);
        }

        while(buffer.hasRemaining()){
            this.term_frequencies.add(buffer.getInt());
        }
    }

    public PostingList(ByteBuffer buffer, EncoderInterface decoder_docID, EncoderInterface decoder_TFs){

        this();

        SkippingPointer current_pointer;
        ByteBuffer doc_ids;
        ByteBuffer tfs;
        while(buffer.hasRemaining()){
            current_pointer = new SkippingPointer(buffer);
            skipping_points.add(current_pointer);

            doc_ids = ByteBuffer.allocate(current_pointer.getBlock_length_docIDs());
            buffer.get(doc_ids.array());
            this.doc_ids.addAll(decoder_docID.decodeList(doc_ids));

            // read termFreqs of one block
            tfs =  ByteBuffer.allocate(current_pointer.getBlock_length_TFs());
            buffer.get(tfs.array());
            this.term_frequencies.addAll(decoder_TFs.decodeList(tfs));
        }
    }

    public PostingList(SkippingPointer pointer, ByteBuffer docIDsByteBuffer, ByteBuffer termFreqsByteBuffer,
                       EncoderInterface decoder_docID, EncoderInterface decoder_TFs){
        this();

        this.doc_ids.addAll(decoder_docID.decodeList(docIDsByteBuffer));
        this.term_frequencies.addAll(decoder_TFs.decodeList(termFreqsByteBuffer));
    }

    public void addPosting(int doc_id){
        int index = this.doc_ids.indexOf(doc_id);
        if (index == -1){
            this.doc_ids.add(doc_id);
            this.term_frequencies.add(1);
        }else{
            this.term_frequencies.set(index, (this.term_frequencies.get(index) + 1));
        }
    }

    public void appendPostings(PostingList new_postings){
        this.doc_ids.addAll(new_postings.getDoc_ids());
        this.term_frequencies.addAll(new_postings.getTerm_frequencies());
    }

    public ByteBuffer serialize(){
        int size = (this.doc_ids.size() * Integer.BYTES) + Integer.BYTES + (this.term_frequencies.size() * Integer.BYTES);
        ByteBuffer buffer = ByteBuffer.allocate(size);

        // write DocIDs
        for(Integer i : this.doc_ids)
            buffer.putInt(i);

        // separator char
        buffer.putInt(-1);

        // write TermFreqs
        for(Integer i : this.term_frequencies)
            buffer.putInt(i);

        buffer.flip();
        return buffer;
    }

    public ByteBuffer serializeDocIDsBlock(EncoderInterface encoder, int start_index, int end_index) {

        List<Integer> doc_id_BlockSubset = this.doc_ids.subList(start_index, end_index);
        return encoder.encodeList(doc_id_BlockSubset);
    }

    public ByteBuffer serializeTFsBlock(EncoderInterface encoder, int start_index, int end_index){

        List<Integer> termFreqs_BlockSubset = this.term_frequencies.subList(start_index, end_index);
        return encoder.encodeList(termFreqs_BlockSubset);
    }


    public void initPointers(){
        this.block_size = (int) Math.ceil(Math.sqrt(this.doc_ids.size()));

        for (int i = block_size; i < this.doc_ids.size(); i = i+block_size){
            this.skipping_points.add(new SkippingPointer(this.doc_ids.get(i - 1)));
        }
        this.skipping_points.add(new SkippingPointer(this.doc_ids.get(this.doc_ids.size() - 1)));
    }

    public List<Integer> getDoc_ids() {
        return doc_ids;
    }

    public List<Integer> getTerm_frequencies() {
        return term_frequencies;
    }

    public List<SkippingPointer> getSkipping_points() {
        return skipping_points;
    }

    public int getBlock_size() {
        return block_size;
    }

    public int getDocId(int index){
        return this.doc_ids.get(index);
    }

    public int getTermFrequency(int index){
        return this.term_frequencies.get(index);
    }

    @Override
    public String toString() {
        return "PostingList{" + "\n" +
                "\t doc_ids=\t" + doc_ids + "\n" +
                "\t term_freqs=\t" + term_frequencies + "\n" +
                "\t skipping_points=" + skipping_points +
                '}';
    }

    public int getSize(){
        return this.doc_ids.size();
    }

}