package org.common;

import org.common.encoding.EncoderInterface;
import org.offline_phase.Spimi;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

public class ChunkHandler {

    public static final String basename = "data/";
    public static final String basename_intermediate_index = "data/intermediate_postings/index/";
    public static final String basename_intermediate_lexicon = "data/intermediate_postings/lexicon/";
    public static final String basename_intermediate_docindex = "data/intermediate_postings/doc_index/";
    static Logger logger = Logger.getLogger(Spimi.class.getName());
    private static EncoderInterface encoderDocID;
    private static EncoderInterface encoderTermFreqs;

    public static void setEncoder(EncoderInterface e_docID, EncoderInterface e_tf){
        encoderDocID = e_docID;
        encoderTermFreqs = e_tf;
    }

    public static void writeLexicon(Lexicon lexicon, String lexicon_filename, boolean intermediate){

        try (FileOutputStream indexFileOutputStream = new FileOutputStream(lexicon_filename, false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            for(String k : lexicon.keySet())
                indexFileChannel.write(lexicon.serializeEntry(k));

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Lexicon " + lexicon_filename + " written on disk");
    }

    public static Lexicon readLexicon(String lexicon_filename){

        Lexicon lexicon = null;
        try (FileInputStream lexiconFileInputStream = new FileInputStream(lexicon_filename);
             FileChannel indexFileChannel = lexiconFileInputStream.getChannel()) {

            long size = indexFileChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            indexFileChannel.read(buffer);
            buffer.flip();

            lexicon = new Lexicon(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lexicon;
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

    public static TermEntry writeIntermediatePostingList(FileChannel indexFileChannel, PostingList postingList) throws IOException {
        long startPosition = indexFileChannel.position();
        indexFileChannel.write(postingList.serialize());
        long length = indexFileChannel.position() - startPosition;

        return new TermEntry(-1, startPosition, length, 0);
    }

    /*
        TODO - due metodi molto simili, valutare se conviene sempre passare il file channel dall'esterno
            considerando che a tempo di esecuzione devo leggermi un solo file, tanto vale tenere il file channel sempre aperto
     */
    public static PostingList readIntermediatePostingList(TermEntry termEntry){

        String index_filename = String.format(basename_intermediate_index + "block_index_%05d.bin", termEntry.getBlock_index());
        PostingList postingList = null;

        try (FileInputStream indexFileInputStream = new FileInputStream(index_filename);
             FileChannel indexFileChannel = indexFileInputStream.getChannel()) {

            // Read bytes into ByteBuffer
            ByteBuffer indexByteBuffer = ByteBuffer.allocate((int) termEntry.getLength());
            indexFileChannel.position(termEntry.getOffset());
            indexFileChannel.read(indexByteBuffer);
            indexByteBuffer.flip();

            postingList = new PostingList(indexByteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postingList;
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


    public static void writeDocIndex(DocIndex docIndex, String doc_index_filename){

        try (FileOutputStream docIndexFileOutputStream = new FileOutputStream(doc_index_filename, false);
             FileChannel docIndexFileChannel = docIndexFileOutputStream.getChannel()) {

            docIndexFileChannel.write(docIndex.serialize());

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Lexicon " + doc_index_filename + " written on disk");
    }

    public static DocIndex readDocIndex(String doc_index_filename){

        DocIndex doc_index = null;

        try (FileInputStream docIndexFileInputStream = new FileInputStream(doc_index_filename);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            long size = docIndexFileChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            docIndexFileChannel.read(buffer);
            buffer.flip();

            doc_index = new DocIndex(buffer);
        } catch (IOException e) {
                e.printStackTrace();
        }

        return doc_index;
    }


}