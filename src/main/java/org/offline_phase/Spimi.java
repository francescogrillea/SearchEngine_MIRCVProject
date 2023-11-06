package org.offline_phase;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.common.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class Spimi extends ChunkHandler {

    // private final int memory_size = 10000;
    private int doc_id_counter = 0;
    private int block_id_counter = 0;

    private final String intermediate_posting_path = "data/intermediate_postings/";

    static Logger logger = Logger.getLogger(Spimi.class.getName());


    public Spimi(TarArchiveInputStream stream) {

        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))){

            ContentParser parser = new ContentParser("data/stop_words_english.txt");
            String line;
            do{
                StringBuilder chunk_text = new StringBuilder();
                do{
                    line = br.readLine();
                    chunk_text.append(line).append("\n");
                    doc_id_counter++;
                }while(doc_id_counter < CHUNK_SIZE * (block_id_counter+1) && line != null);

                new ProcessChunkThread(chunk_text, block_id_counter, parser).run();
//                intermediateIndex.clear();

                block_id_counter++;
                System.out.println(doc_id_counter);
            }while (line != null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        chunk_to_disk(lexicon, "data/lexicon.ser");
        chunk_to_disk(inverted_index, "data/inverted_index.ser");
    }

}
