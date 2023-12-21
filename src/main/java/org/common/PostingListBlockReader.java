package org.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PostingListBlockReader extends PostingListReader implements AutoCloseable{

    private final FileChannel fileChannel;
    private PostingList current_block;
    private final long last_byte;   // TODO - controllare in ogni metodo che quello che leggo non sfori
    private int index_pointer;
    private final String term;
    private float termUpperBound;
    private int documentFrequency;

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

//    public boolean hasNext() throws IOException {
//        long position_tmp = fileChannel.position();
//        long current_tmp = position_tmp + SkippingPointer.SIZE;
//
//        if(current_tmp < this.last_byte)
//            return false;
//        SkippingPointer pointer = readBlockPointer();
//
//        current_tmp += pointer.getBlock_length_docIDs() + pointer.getBlock_length_TFs();
//        if(current_tmp <= this.last_byte)
//            return false;
//
//        fileChannel.position(position_tmp);
//        return true;
//
//    }

    public boolean readBlock() throws IOException {

        // if i'm not at the end of the posting list
        if(this.fileChannel.position() + SkippingPointer.SIZE < this.last_byte){
            SkippingPointer pointer = readBlockPointer();
            readBlockContent(pointer);
            return true;
        }
        return false;
    }

    private SkippingPointer readBlockPointer() throws IOException {
        ByteBuffer pointerByteBuffer = ByteBuffer.allocate(SkippingPointer.SIZE);
        this.fileChannel.read(pointerByteBuffer);
        pointerByteBuffer.flip();

        return new SkippingPointer(pointerByteBuffer);
    }

    public void readBlockContent(SkippingPointer pointer) throws IOException {

        ByteBuffer docIDsByteBuffer = ByteBuffer.allocate(pointer.getBlock_length_docIDs());
        this.fileChannel.read(docIDsByteBuffer);
        docIDsByteBuffer.flip();

        ByteBuffer termFreqsByteBuffer = ByteBuffer.allocate(pointer.getBlock_length_TFs());
        this.fileChannel.read(termFreqsByteBuffer);
        termFreqsByteBuffer.flip();

        this.current_block = new PostingList(pointer, docIDsByteBuffer, termFreqsByteBuffer, encoderDocID, encoderTermFreqs);
        this.index_pointer = 0;
    }

    public boolean nextPosting() throws IOException {

        if(this.index_pointer == this.current_block.getDoc_ids().size() - 1){
            return readBlock();
        }
        this.index_pointer++;
        return true;
    }



    // TODO - da eliminare dopo
    public void readNextBlock(SkippingPointer previous_block_pointer) throws IOException {

        long position = this.fileChannel.position();
        // move from the previous_block_pointer to the next one
        this.fileChannel.position(position + previous_block_pointer.getBlock_length_docIDs() + previous_block_pointer.getBlock_length_docIDs());

        SkippingPointer current_block_pointer = readBlockPointer();
        readBlockContent(current_block_pointer);
    }

    public Integer nextGEQ(int doc_id) throws IOException {

        // if the doc_id is lower than the one in the current position return 0
        if(this.current_block.getDocId(this.index_pointer) > doc_id)
            return 0;

        // if the doc_id is the one in the current block return its term frequency
        else if(this.getMaxDocId() >= doc_id)
            return this.getTermFrequency(doc_id);



        long position_tmp;
        SkippingPointer pointer = null;

        // move along pointers until the block has been found
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

    private void rewind(){
        // TODO - rewind to the starting of the posting list if it has been read to the end
        // E' possibile che succeda di fare un rewind ? Non penso -> la posting list la scandisco una volta sola per query
    }

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

    public PostingList getCurrent_block() {
        return current_block;
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
    public void setDocumentFrequency(int newDocumentFrequency){
        this.documentFrequency= newDocumentFrequency;
    }
}
