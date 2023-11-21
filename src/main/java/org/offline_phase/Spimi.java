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
                    if ( line != null && !line.isEmpty()) {
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
        //merge_chunks();
    }

    public void merge_chunks(){

//        InvertedIndex merged_index = new InvertedIndex();
//        Lexicon merged_lexicon = new Lexicon();
//
//        merged_lexicon = readLexicon("data/intermediate_postings/lexicon/block_lexicon_00000.bin");
//        System.out.println(merged_lexicon);
//        System.out.println(merged_lexicon.get("current"));

        PostingList postingList = readPostingList("data/intermediate_postings/index/block_index_00000.bin", new TermEntry(0, 626003, 433));
        System.out.println(postingList);
//        Lexicon current_lexicon;
//        int posting_size = 0;
//        int lexicon_size = 0;
//
//        File lexicon_directory = new File("data/intermediate_postings/lexicon");
//        File index_directory = new File("data/intermediate_postings/index");
//
//        File[] lexicon_files = lexicon_directory.listFiles();
//        File[] index_files = index_directory.listFiles();
//
//        String current_lexicon_filename;
//        String current_index_filename;
//        int n = lexicon_files.length;
//

//        for(int i = 0; i < n; i++){
//            current_lexicon_filename = lexicon_files[i].getPath();
//            current_index_filename = index_files[i].getPath();
//            current_lexicon = readLexicon(current_lexicon_filename);
//
//            for(TermEntry term : current_lexicon.getLexicon()){
//                lexicon_size++;
//                posting_size += readPostingList(current_index_filename, term).getSize();
//
//                int index = merged_lexicon.indexOf(term.getTerm());
//                // if term is not in merged_lexicon
//                if(index < 0){
//                    index = merged_lexicon.addTerm(term);
//                    merged_index.addPostingList(index, readPostingList(current_index_filename, term));
//                }
//                else{
//                    merged_index.appendPostingList(index, readPostingList(current_index_filename, term));
//                }
//            }
//            break;
//        }
//        System.out.println(lexicon_size);
//        System.out.println(posting_size);
//        System.out.println(merged_lexicon);
//        System.out.println(merged_index);
//        write(merged_index, merged_lexicon, "data/index.ser", "data/lexicon.ser");
    }
}
