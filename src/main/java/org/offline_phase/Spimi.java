package org.offline_phase;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.common.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class Spimi extends ChunkHandler {

    private int doc_id_counter = 0;
    private int block_id_counter = 0;
    static Logger logger = Logger.getLogger(Spimi.class.getName());

    public void run(TarArchiveInputStream stream){

        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))){

            ContentParser parser = new ContentParser("data/stop_words_english.txt");
            String line;
            do{
                StringBuilder chunk_text = new StringBuilder();
                do{
                    line = br.readLine();
                    if (!line.isEmpty() && line != null) {
                        chunk_text.append(line).append("\n");
                        doc_id_counter++;
                    }
                }while(doc_id_counter < CHUNK_SIZE * (block_id_counter+1) && line != null && doc_id_counter < _DEBUG_N_DOCS);

                new ProcessChunkThread(chunk_text, block_id_counter, parser).run();

                block_id_counter++;
            }while (line != null && doc_id_counter < _DEBUG_N_DOCS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // TODO - shall we wait the threads to finish ? -> Implement a threadpool ?
        merge_chunks();
    }

    public void merge_chunks(){

        InvertedIndex merged_index = new InvertedIndex();
        Lexicon merged_lexicon = new Lexicon();

        File lexicon_directory = new File("data/intermediate_postings/lexicon");
        File index_directory = new File("data/intermediate_postings/index");

        File[] lexicon_files = lexicon_directory.listFiles();
        File[] index_files = index_directory.listFiles();

        // TODO - assert size of both directories be the same

        int n = lexicon_files.length;

        merged_lexicon = readLexicon("data/intermediate_postings/lexicon/block_lexicon_00000.ser");
        System.out.println(merged_lexicon);


    }


}
