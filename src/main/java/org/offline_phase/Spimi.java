package org.offline_phase;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.common.LexiconInfo;
import org.common.Posting;
import org.common.PostingList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

public class Spimi {

    // private final int memory_size = 10000;
    private int doc_id_counter = 0;
    private int block_id_counter = 0;
    private final int  CHUNK_SIZE = 1;
    private final int __DEBUG_TEST = 2;    // n of blocks we want to analyze

    private final String intermediate_posting_path = "data/intermediate_postings/";

    static Logger logger = Logger.getLogger(Spimi.class.getName());


    public Spimi(TarArchiveInputStream stream) {

        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))){

            ContentParser parser = new ContentParser("data/stop_words_english.txt");
            String line;
            IntermediatePostings intermediateIndex = new IntermediatePostings();

            while((line = br.readLine()) != null && block_id_counter < __DEBUG_TEST){   // TODO - while there's still memory
                logger.info("block_id_counter: " + block_id_counter);

                while(doc_id_counter < CHUNK_SIZE * (block_id_counter+1)){

                    String[] fields = line.split("\t");
                    List<String> terms = parser.processContent(fields[1]);

                    for(String term : terms){
                        intermediateIndex.addPosting(term, doc_id_counter);
                    }
                    doc_id_counter += 1;
                }// end of chunk
                System.out.println(intermediateIndex);

                // write intermediate index structure to a file
                String filename = this.intermediate_posting_path + "block_" + block_id_counter + ".ser";
                chunk_to_disk(intermediateIndex, filename);

                // clear up datastructures to save space after they have been written on disk
                intermediateIndex.clear();
                block_id_counter++;
            }// end of file


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


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

        File directory = new File(this.intermediate_posting_path);

        // read each file
        File[] block_files = directory.listFiles();
        if (block_files != null){
            IntermediatePostings current_block;
            for(File file : block_files){
                current_block = chunk_from_disk(file.getPath());
                merged_postings.merge(current_block);

                //file.delete();
            }
            logger.info("" + merged_postings);
        }

        generate_final_structures(merged_postings);
    }

    public void generate_final_structures(IntermediatePostings merged_postings){

        HashMap<String, LexiconInfo> lexicon = new HashMap<String, LexiconInfo>();
        ArrayList<PostingList> inverted_index = new ArrayList<>();

        for(int i = 0; i < merged_postings.size(); i++){
            inverted_index.add(merged_postings.getPostingLists().get(i));
            inverted_index.get(i).generate_skipping_points();
            lexicon.put(merged_postings.getTerms().get(i), new LexiconInfo(i, merged_postings.getPostingLists().get(i).size()));
        }

        logger.info("" + lexicon);
        logger.info("" + inverted_index);

        // TODO - convert to byte arrays to efficient storing?

        chunk_to_disk(lexicon, "data/lexicon.ser");
        chunk_to_disk(inverted_index, "data/inverted_index.ser");
    }

}
