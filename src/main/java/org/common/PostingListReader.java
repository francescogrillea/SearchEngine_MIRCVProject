package org.common;

import org.common.encoding.EncoderInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PostingListReader {

    public static final String basename = "data/";
    public static final String basename_intermediate_index = "data/intermediate_postings/index/";
    private static EncoderInterface encoderDocID;
    private static EncoderInterface encoderTermFreqs;

    public static void setEncoder(EncoderInterface e_docID, EncoderInterface e_tf){
        encoderDocID = e_docID;
        encoderTermFreqs = e_tf;
    }

    public static TermEntry writeIntermediatePostingList(FileChannel indexFileChannel, PostingList postingList) throws IOException {
        long startPosition = indexFileChannel.position();
        indexFileChannel.write(postingList.serialize());
        long length = indexFileChannel.position() - startPosition;

        return new TermEntry(-1, startPosition, length, 0);
    }

    public static PostingList readIntermediatePostingList(FileChannel indexFileChannel, TermEntry termEntry){

        PostingList postingList = null;

        // Read bytes into ByteBuffer
        ByteBuffer indexByteBuffer = ByteBuffer.allocate((int) termEntry.getLength());
        try {
            indexFileChannel.position(termEntry.getOffset());
            indexFileChannel.read(indexByteBuffer);
            indexByteBuffer.flip();
            postingList = new PostingList(indexByteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return postingList;
    }


    /**
     * Reads the entire posting list associated with a given term from the index file,
     * decompresses it, and constructs a PostingList object.
     *
     * The method uses information from the provided TermEntry, such as offset and length,
     * to locate and read the compressed posting list data from the index file.
     *
     * @param termEntry The TermEntry object containing information about the term,
     *                  including offset and length in the index file.
     * @return A PostingList object representing the decompressed posting list for the given term.
     *         Returns null if an IOException occurs during the file reading process.
     * @throws NullPointerException If the provided TermEntry is null.
     * @throws IllegalArgumentException If the TermEntry's offset or length is negative.
     */
    public static PostingList readPostingList(TermEntry termEntry){

        String index_filename = basename + "index.bin";
        PostingList postingList = null;

        try (FileInputStream indexFileInputStream = new FileInputStream(index_filename);
             FileChannel indexFileChannel = indexFileInputStream.getChannel()) {

            // Read bytes into ByteBuffer
            ByteBuffer indexByteBuffer = ByteBuffer.allocate((int) termEntry.getLength());
            indexFileChannel.position(termEntry.getOffset());
            indexFileChannel.read(indexByteBuffer);
            indexByteBuffer.flip();

            postingList = new PostingList(indexByteBuffer, encoderDocID, encoderTermFreqs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postingList;
    }

    /**
     * Writes the provided PostingList to the specified FileChannel, storing the posting list on disk.
     * The method also returns a TermEntry object representing the location and length information
     * of the stored posting list in the index file.
     *
     * @param indexFileChannel The FileChannel where the posting list will be stored.
     * @param postingList The PostingList to be written to the index file.
     * @return A TermEntry object specifying the location and length of the stored posting list
     *         in the index file, along with the number of documents in the posting list.
     * @throws IOException If an I/O error occurs during the file writing process.
     * @throws NullPointerException If either the indexFileChannel or postingList is null.
     * @throws IllegalArgumentException If the starting position of the indexFileChannel is negative.
     */
    public static TermEntry writePostingList(FileChannel indexFileChannel, PostingList postingList) throws IOException {

        long start_PostingList_position = indexFileChannel.position();
        long pointerFilePosition;
        long docIDs_blockStartPosition;
        long termFreqs_blockStartPosition;

        int start_index_block = 0;
        int end_index_block;
        // for each block (subset of the posting list) identified by the skipping pointer
        for (SkippingPointer pointer : postingList.getSkipping_points()) {

            pointerFilePosition = indexFileChannel.position();  // save where the Skipping Pointer should be stored
            end_index_block = Math.min(start_index_block + postingList.getBlock_size(), postingList.getDoc_ids().size());

            indexFileChannel.position(pointerFilePosition + SkippingPointer.SIZE);  // move the position of the docIDs list
            docIDs_blockStartPosition = indexFileChannel.position();    // save where the docIDs list starts
            indexFileChannel.write(postingList.serializeDocIDsBlock(encoderDocID, start_index_block, end_index_block));  // write the docIDs list

            // update the skipping pointer passing the length of the docIDs list (in bytes)
            pointer.setBlock_length_docIDs((short) (indexFileChannel.position() - docIDs_blockStartPosition));


            termFreqs_blockStartPosition = indexFileChannel.position(); // save where the TermFreqs starts
            indexFileChannel.write(postingList.serializeTFsBlock(encoderTermFreqs, start_index_block, end_index_block)); // write the termFreqs list

            // update the skipping pointer passing the length of the termFreqs list (in bytes)
            pointer.setBlock_length_TFs((short) (indexFileChannel.position() - termFreqs_blockStartPosition));

            // finally write the skipping pointer
            indexFileChannel.write(pointer.serialize(), pointerFilePosition);

            start_index_block = end_index_block;

        }
        return new TermEntry(-1, start_PostingList_position, indexFileChannel.position() - start_PostingList_position, postingList.getDoc_ids().size());
    }

    private static SkippingPointer readBlockPointer(FileChannel fileChannel) throws IOException{
        ByteBuffer pointerByteBuffer = ByteBuffer.allocate(SkippingPointer.SIZE);
        fileChannel.read(pointerByteBuffer);
        pointerByteBuffer.flip();

        return new SkippingPointer(pointerByteBuffer);
    }

    public static PostingList readBlockContent(FileChannel fileChannel, SkippingPointer pointer) throws IOException {

        ByteBuffer docIDsByteBuffer = ByteBuffer.allocate(pointer.getBlock_length_docIDs());
        fileChannel.read(docIDsByteBuffer);
        docIDsByteBuffer.flip();

        ByteBuffer termFreqsByteBuffer = ByteBuffer.allocate(pointer.getBlock_length_TFs());
        fileChannel.read(termFreqsByteBuffer);
        termFreqsByteBuffer.flip();

        return new PostingList(pointer, docIDsByteBuffer, termFreqsByteBuffer, encoderDocID, encoderTermFreqs);
    }

    public static PostingList readNextBlock(FileChannel fileChannel, SkippingPointer previous_block_pointer) throws IOException {

        long position = fileChannel.position();
        // move from the previous_block_pointer to the next one
        fileChannel.position(position + previous_block_pointer.getBlock_length_docIDs() + previous_block_pointer.getBlock_length_docIDs());

        SkippingPointer current_block_pointer = readBlockPointer(fileChannel);
        return readBlockContent(fileChannel, current_block_pointer);
    }

    public static Integer nextGEQ(FileChannel fileChannel, int doc_id) throws IOException {

        long position_tmp;
        SkippingPointer pointer = null;

        // move along pointers until the block has been found
        boolean found_flag = false;
        while(!found_flag){
            pointer = readBlockPointer(fileChannel);
            if(pointer.getMax_doc_id() < doc_id){
                position_tmp = fileChannel.position();
                fileChannel.position(position_tmp +  pointer.getBlock_length_docIDs() + pointer.getBlock_length_docIDs());
            }
            else
                found_flag = true;
        }
        
        PostingList postingList = readBlockContent(fileChannel, pointer);
        int index = postingList.getDoc_ids().indexOf(doc_id);

        return postingList.getTermFrequency(index);
    }





}
