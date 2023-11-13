package org.common;

import org.apache.commons.lang3.SerializationUtils;
import org.offline_phase.Spimi;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class ChunkHandler {

    protected final String basename = "data/intermediate_postings/";
    protected final int  CHUNK_SIZE = 5;     // in MB or just n-docs
    protected final int _DEBUG_N_DOCS = 15;    // n of documents we want to analyze
    static Logger logger = Logger.getLogger(Spimi.class.getName());


    protected void write(InvertedIndex invertedIndex, Lexicon lexicon, String index_filename, String lexicon_filename){

        try (FileOutputStream indexFileOutputStream = new FileOutputStream(index_filename, true);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel();
             FileOutputStream lexiconFileOutputStream = new FileOutputStream(lexicon_filename, true);
             FileChannel lexiconFileChannel = lexiconFileOutputStream.getChannel()) {

            // to save the starting position
            long startPosition;
            long length;

            for(int i = 0; i < invertedIndex.getSize(); i++){

                PostingList postingList = invertedIndex.getInverted_index().get(i);
                TermEntry termEntry = lexicon.getLexicon().get(i);

                startPosition = indexFileChannel.position();

                // store posting list of the i-th term to disk
                ByteBuffer index_buffer = ByteBuffer.allocate(1024);
                byte[] index_bytes = SerializationUtils.serialize(postingList);

                index_buffer.put(index_bytes);
                index_buffer.flip();
                indexFileChannel.write(index_buffer);

                length = indexFileChannel.position() - startPosition;

                // update the fields of the i-th term in the vocabulary
                termEntry.setOffset(startPosition);
                termEntry.setLength(length);

                // store the i-th term to disk
                ByteBuffer lexicon_buffer = ByteBuffer.allocate(512);
                byte[] lexicon_bytes = SerializationUtils.serialize(termEntry);

                lexicon_buffer.put(lexicon_bytes);
                lexicon_buffer.flip();
                lexiconFileChannel.write(lexicon_buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Block has been written on disk");
    }


    protected void read(String lexicon_filename, String index_filename){

        try (FileInputStream lexiconFileInputStream = new FileInputStream(lexicon_filename);
             FileChannel lexiconFileChannel = lexiconFileInputStream.getChannel();
             FileInputStream indexFileInputStream = new FileInputStream(index_filename);
             FileChannel indexFileChannel = indexFileInputStream.getChannel()) {

            // TODO - for each term in terms

            // Read bytes into ByteBuffer
            ByteBuffer lexiconByteBuffer = ByteBuffer.allocate(512);
            int lexiconBytesRead = lexiconFileChannel.read(lexiconByteBuffer);
            lexiconByteBuffer.flip();

            byte[] lexiconBytes = lexiconByteBuffer.array();
            TermEntry termEntry = SerializationUtils.deserialize(lexiconBytes);
            System.out.println(termEntry);



            // Read bytes into ByteBuffer
            ByteBuffer indexByteBuffer = ByteBuffer.allocate((int) termEntry.getLength());
            indexFileChannel.position(termEntry.getOffset());
            indexFileChannel.read(indexByteBuffer);
            indexByteBuffer.flip();

            byte[] indexBytes = indexByteBuffer.array();
            PostingList postingList = SerializationUtils.deserialize(indexBytes);
            System.out.println(postingList);


            // TODO - end for



        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Block has been read from disk");
    }


//    public void merge_chunks(){
//
//        IntermediatePostings merged_postings = new IntermediatePostings();
//
//        File directory = new File(this.basename);
//
//        // read each file
//        File[] block_files = directory.listFiles();
//        if (block_files != null){
//            IntermediatePostings current_block;
//            long startTime;
//            for(File file : block_files){
//                System.out.println(file.getPath());
//                startTime = System.currentTimeMillis();
//                current_block = chunk_from_disk(file.getPath());
//                System.out.println(current_block);
//                merged_postings.merge(current_block);
//                System.out.println("File " + file.getName() + " merged in " + (System.currentTimeMillis() - startTime)/1000.0 + "s");
//
//                //file.delete();
//            }
//        }
//        generate_final_structures(merged_postings);
//    }

//    public void generate_final_structures(IntermediatePostings merged_postings){
//
//        System.out.println("Creating Inverted Index");
//        HashMap<String, LexiconInfo> lexicon = new HashMap<String, LexiconInfo>();
//        ArrayList<PostingList> inverted_index = new ArrayList<>();
//
//        for(int i = 0; i < merged_postings.size(); i++){
//            inverted_index.add(merged_postings.getPostingLists().get(i));
//            inverted_index.get(i).generate_skipping_points();
//            lexicon.put(merged_postings.getTerms().get(i), new LexiconInfo(i, merged_postings.getPostingLists().get(i).getSize()));
//        }
//
//        //System.out.println(inverted_index);
//
//        // TODO - convert to byte arrays to efficient storing?
//
//        chunk_to_disk(lexicon, "data/lexicon.ser");
//        chunk_to_disk(inverted_index, "data/inverted_index.ser");
//    }

}
