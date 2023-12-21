package org.common;

import org.common.encoding.EncoderInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PostingListReader {

    public static final String basename = "data/";
    public static final String basename_intermediate_index = "data/intermediate_postings/index/";
    public static final String basename_index = "data/index.bin";
    protected static EncoderInterface encoderDocID;
    protected static EncoderInterface encoderTermFreqs;


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


    public static TermEntry writePostingList(FileChannel indexFileChannel, PostingList postingList) throws IOException {

        long start_PostingList_position = indexFileChannel.position();
        long pointerFilePosition;
        long docIDs_blockStartPosition;
        long termFreqs_blockStartPosition;

        float tfidf_ub;
        float bm25_ub;

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






}
