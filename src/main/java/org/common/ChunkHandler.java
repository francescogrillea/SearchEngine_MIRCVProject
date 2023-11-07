package org.common;

import org.offline_phase.IntermediatePostings;
import org.offline_phase.Spimi;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class ChunkHandler {

    protected final String basename = "data/intermediate_postings/";
    protected final int  CHUNK_SIZE = 10240;     // in MB or just n-docs
    protected final int _DEBUG_N_DOCS = 2;    // n of documents we want to analyze
    static Logger logger = Logger.getLogger(Spimi.class.getName());


    /**
     * Serializes and saves the provided object to a file on disk.
     *
     * @param block The object to be serialized and saved to disk.
     * @param filename The name of the file where the object will be saved.
     */
    public void chunk_to_disk(Serializable block, String filename){

        try (FileOutputStream byteArrayOutputStream = new FileOutputStream(filename);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            // Serialize the object and write it to the file
            objectOutputStream.writeObject(block);
            logger.info("Object has been written to " + filename );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public IntermediatePostings chunk_from_disk(String filename){
        try (FileInputStream fileInputStream = new FileInputStream(filename);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            IntermediatePostings intermediatePostings = (IntermediatePostings) objectInputStream.readObject();
            logger.info("Object has been read from " + filename );
            return intermediatePostings;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void merge_chunks(){

        IntermediatePostings merged_postings = new IntermediatePostings();

        File directory = new File(this.basename);

        // read each file
        File[] block_files = directory.listFiles();
        if (block_files != null){
            IntermediatePostings current_block;
            for(File file : block_files){
                current_block = chunk_from_disk(file.getPath());
                merged_postings.merge(current_block);

                //file.delete();
            }
        }
        generate_final_structures(merged_postings);
    }

    public void generate_final_structures(IntermediatePostings merged_postings){

        HashMap<String, LexiconInfo> lexicon = new HashMap<String, LexiconInfo>();
        ArrayList<PostingList> inverted_index = new ArrayList<>();

        for(int i = 0; i < merged_postings.size(); i++){
            inverted_index.add(merged_postings.getPostingLists().get(i));
            inverted_index.get(i).generate_skipping_points();
            lexicon.put(merged_postings.getTerms().get(i), new LexiconInfo(i, merged_postings.getPostingLists().get(i).getSize()));
        }

        //System.out.println(inverted_index);

        // TODO - convert to byte arrays to efficient storing?

//        chunk_to_disk(lexicon, "data/lexicon.ser");
//        chunk_to_disk(inverted_index, "data/inverted_index.ser");
    }

}
