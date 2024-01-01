package org.common;

import org.common.encoding.EncoderInterface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The PostingList class represents a list of postings for a specific term in an
 * inverted index. Each posting consists of a document ID, term frequency, and
 * optionally includes skipping pointers for efficient traversal after merge has
 * been done.
 */
public class PostingList {
    private final List<Integer> doc_ids;    // the list of docIDs in the posting list
    private final List<Integer> term_frequencies;   // the list of term frequencies corresponding to document IDs
    private final List<SkippingPointer> skipping_points;    // the list of skipping pointers for efficient traversal
    private int block_size; // the block size for skipping pointers

    /**
     * Constructs an empty PostingList with empty lists for DocIDs, term frequencies,
     * and skipping pointers.
     */
    public PostingList() {
        this.doc_ids = new ArrayList<>();
        this.term_frequencies = new ArrayList<>();
        this.skipping_points = new ArrayList<>();
    }

    /**
     * Constructs a PostingList with a single specified document ID and initializes
     * corresponding term frequency and skipping pointer lists.
     *
     * @param doc_id The document ID to add to the posting list.
     */
    public PostingList(int doc_id){
        this.doc_ids = new ArrayList<>();
        this.term_frequencies = new ArrayList<>();
        this.skipping_points = new ArrayList<>();

        addPosting(doc_id);
    }

    /**
     * Constructs a PostingList by reading from a ByteBuffer without decompression.
     *
     * @param buffer The ByteBuffer containing serialized posting information.
     */
    public PostingList(ByteBuffer buffer){
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

    /**
     * Constructs a PostingList by reading from a ByteBuffer using specific
     * EncoderInterface instances for DocIDs and term frequencies.
     *
     * @param buffer The ByteBuffer containing serialized posting information.
     * @param decoder_docID The EncoderInterface for decoding DocIDs.
     * @param decoder_TFs The EncoderInterface for decoding term frequencies.
     */
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

    /**
     * Constructs a PostingList with specified pointers, DocIDs, and term frequencies
     * using provided EncoderInterface instances.
     *
     * @param pointer The skipping pointer for efficient traversal.
     * @param docIDsByteBuffer The ByteBuffer containing serialized DocIDs.
     * @param termFreqsByteBuffer The ByteBuffer containing serialized term frequencies.
     * @param decoder_docID The EncoderInterface for decoding DocIDs.
     * @param decoder_TFs The EncoderInterface for decoding term frequencies.
     */
    public PostingList(SkippingPointer pointer, ByteBuffer docIDsByteBuffer, ByteBuffer termFreqsByteBuffer,
                       EncoderInterface decoder_docID, EncoderInterface decoder_TFs){
        this();

        this.doc_ids.addAll(decoder_docID.decodeList(docIDsByteBuffer));
        this.term_frequencies.addAll(decoder_TFs.decodeList(termFreqsByteBuffer));
    }

    /**
     * Adds a new posting to the PostingList with the specified document ID. If the
     * document ID is already present, increments the corresponding term frequency.
     *
     * @param doc_id The document ID to add to the posting list.
     */
    public void addPosting(int doc_id){
        int index = this.doc_ids.indexOf(doc_id);
        if (index == -1){
            this.doc_ids.add(doc_id);
            this.term_frequencies.add(1);
        }else{
            this.term_frequencies.set(index, (this.term_frequencies.get(index) + 1));
        }
    }

    /**
     * Appends postings from another PostingList to the current list.
     *
     * @param new_postings The PostingList containing postings to append.
     */
    public void appendPostings(PostingList new_postings){
        this.doc_ids.addAll(new_postings.getDoc_ids());
        this.term_frequencies.addAll(new_postings.getTerm_frequencies());
    }

    /**
     * Serializes the PostingList into a ByteBuffer for storage or transmission.
     *
     * @return A ByteBuffer containing the serialized PostingList.
     */
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

    /**
     * Serializes a block of DocIDs using a specified encoder and index range.
     *
     * @param encoder The EncoderInterface for encoding DocIDs.
     * @param start_index The starting index of the block.
     * @param end_index The ending index of the block.
     * @return A ByteBuffer containing the serialized DocID block.
     */
    public ByteBuffer serializeDocIDsBlock(EncoderInterface encoder, int start_index, int end_index) {

        List<Integer> doc_id_BlockSubset = this.doc_ids.subList(start_index, end_index);
        return encoder.encodeList(doc_id_BlockSubset);
    }

    /**
     * Serializes a block of term frequencies using a specified encoder and index range.
     *
     * @param encoder The EncoderInterface for encoding term frequencies.
     * @param start_index The starting index of the block.
     * @param end_index The ending index of the block.
     * @return A ByteBuffer containing the serialized term frequency block.
     */
    public ByteBuffer serializeTFsBlock(EncoderInterface encoder, int start_index, int end_index){

        List<Integer> termFreqs_BlockSubset = this.term_frequencies.subList(start_index, end_index);
        return encoder.encodeList(termFreqs_BlockSubset);
    }

    /**
     * Initializes skipping pointers based on the block size.
     */
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