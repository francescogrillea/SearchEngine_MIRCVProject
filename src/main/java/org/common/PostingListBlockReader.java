package org.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * The PostingListBlockReader class provides functionality for block-wise reading of posting
 * lists, where each block is accompanied by a SkippingPointer that indicates the maximum
 * document ID in the block and the lengths of the corresponding docIDs and termFreqs lists.
 * The reader can iterate through the posting list blocks and retrieve information about
 * document IDs and term frequencies for a given term.
 */
public class PostingListBlockReader extends PostingListReader implements AutoCloseable{

    private final String term;
    private final FileChannel fileChannel;  // file channel associated to a term for reading posting list blocks
    private PostingList current_block;  // the current block read
    private final long last_byte;   // the position of the last byte in the current posting list, used to check if the posting list is terminated. Otherwise we could read postings of another term.
    private int index_pointer;  // the index pointer indicating the current position within the current block
    private float termUpperBound;   // the upper bound value associated with the term (e.g., TF-IDF or BM25 upper bound)
    private final int documentFrequency;    // the document frequency of the term in the entire document collection

    /**
     * Constructs a PostingListBlockReader for reading posting list blocks associated with a term.
     *
     * @param termEntry The TermEntry containing information about the term, such as offset and length.
     * @param term The term for which posting list blocks are being read.
     * @param bm25 Flag indicating whether BM25 upper bound should be considered.
     * @throws IOException If an I/O error occurs during file channel creation.
     */
    public PostingListBlockReader(TermEntry termEntry, String term, boolean bm25) throws IOException {

        this.fileChannel = new FileInputStream(basename_index).getChannel();
        this.fileChannel.position(termEntry.getOffset());
        this.last_byte = termEntry.getOffset() + termEntry.getLength();

        this.current_block = null;
        this.index_pointer = 0;

        this.termUpperBound = bm25 ? termEntry.getBm25_upper_bound() : termEntry.getTfidf_upper_bound();

        this.term = term;
        this.documentFrequency = termEntry.getDocument_frequency();
    }

    /**
     * Reads the next block of the posting list.
     *
     * @return true if a new block is successfully read, false if the end of the posting list is reached.
     * @throws IOException If an I/O error occurs during block reading.
     */
    public boolean readBlock() throws IOException {

        // if i'm not at the end of the posting list
        if(this.fileChannel.position() + SkippingPointer.SIZE < this.last_byte){
            SkippingPointer pointer = readBlockPointer();
            readBlockContent(pointer);
            return true;
        }
        return false;
    }

    /**
     * Reads the skipping pointer associated with the current block.
     *
     * @return The SkippingPointer object indicating the block's properties.
     * @throws IOException If an I/O error occurs during skipping pointer reading.
     */
    private SkippingPointer readBlockPointer() throws IOException {
        ByteBuffer pointerByteBuffer = ByteBuffer.allocate(SkippingPointer.SIZE);
        this.fileChannel.read(pointerByteBuffer);
        pointerByteBuffer.flip();

        return new SkippingPointer(pointerByteBuffer);
    }

    /**
     * Reads the content (docIDs and termFreqs) of the current block.
     *
     * @param pointer The SkippingPointer indicating the block's properties.
     * @throws IOException If an I/O error occurs during block content reading.
     */
    public void readBlockContent(SkippingPointer pointer) throws IOException {

        ByteBuffer docIDsByteBuffer = ByteBuffer.allocate(pointer.getBlock_length_docIDs());
        this.fileChannel.read(docIDsByteBuffer);
        docIDsByteBuffer.flip();

        ByteBuffer termFreqsByteBuffer = ByteBuffer.allocate(pointer.getBlock_length_TFs());
        this.fileChannel.read(termFreqsByteBuffer);
        termFreqsByteBuffer.flip();

        this.current_block = new PostingList(pointer, docIDsByteBuffer, termFreqsByteBuffer, encoderDocID, encoderTermFreqs);
        this.index_pointer = 0; // we're at the beginning of the block
    }

    /**
     * Moves to the next posting within the current block or reads the next block if necessary.
     *
     * @return true if a new posting is successfully moved to, false if the end of the posting list is reached.
     * @throws IOException If an I/O error occurs during next posting reading.
     */
    public boolean nextPosting() throws IOException {

        if(this.index_pointer == this.current_block.getDoc_ids().size() - 1){
            return readBlock();
        }
        this.index_pointer++;   // move one step ahead in the current block
        return true;
    }


    /**
     * Retrieves the term frequency associated to the next document ID that is greater
     * than or equal to the specified doc_id.
     *
     * @param doc_id The target document ID.
     * @return The term frequency associated to the doc_id document, or 0 if not found.
     * @throws IOException If an I/O error occurs during document ID retrieval.
     */
    public Integer nextGEQ(int doc_id) throws IOException {

        // if the doc_id is lower than the one in the current position, for sure it will not be found -> return 0
        if(this.current_block.getDocId(this.index_pointer) > doc_id)
            return 0;

        // if the doc_id should be in the current block return its term frequency, 0 if not found
        else if(this.getMaxDocId() >= doc_id)
            return this.getTermFrequency(doc_id);


        long position_tmp;
        SkippingPointer pointer = null;

        // move along pointers until the block has been found or the posting list ends
        boolean found_flag = false;
        while(fileChannel.position() + SkippingPointer.SIZE < this.last_byte){

            pointer = readBlockPointer();
            if(pointer.getMax_doc_id() >= doc_id){
                found_flag = true;
                break;
            }
            position_tmp = fileChannel.position();
            fileChannel.position(position_tmp +  pointer.getBlock_length_docIDs() + pointer.getBlock_length_TFs());
        }

        if(found_flag){
            readBlockContent(pointer);
            return this.getTermFrequency(doc_id);
        }

        return 0;
    }

    /**
     * Retrieves the term frequency associated with the specified document ID in the current block.
     * If the document ID is found in the current block, the method updates the index pointer to
     * the corresponding position and returns the term frequency. If the document ID is not found,
     * it returns 0.
     *
     * @param doc_id The document ID for which to retrieve the term frequency.
     * @return The term frequency for the specified document ID if found; otherwise, returns 0.
     */
    private int getTermFrequency(int doc_id){
        int index = this.current_block.getDoc_ids().indexOf(doc_id);
        if(index != -1){
            this.index_pointer = index;
            return this.current_block.getTermFrequency(index);
        }
        return 0;
    }

    public int getDocID(){
        return this.current_block.getDocId(index_pointer);
    }
    public int getTermFreq(){
        return this.current_block.getTermFrequency(index_pointer);
    }

    public String getTerm() {
        return this.term;
    }

    @Override
    public void close() throws IOException {
        this.fileChannel.close();
    }

    public int getMaxDocId(){
        int n = this.current_block.getDoc_ids().size();
        return this.current_block.getDocId(n-1);
    }

    public float getTermUpperBound(){
        return this.termUpperBound;
    }
    public void setTermUpperBound(float newTermUpperBound){
        this.termUpperBound= newTermUpperBound;
    }
    public int getDocumentFrequency(){
        return this.documentFrequency;
    }
}
