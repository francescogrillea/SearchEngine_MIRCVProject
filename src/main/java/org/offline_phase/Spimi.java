package org.offline_phase;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.common.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Spimi extends ChunkHandler {

    private int doc_id_counter = 0;
    private int block_id_counter = 0;
    static Logger logger = Logger.getLogger(Spimi.class.getName());

    public void run(TarArchiveInputStream stream){

        ExecutorService threadpool = Executors.newCachedThreadPool();
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
                }while(doc_id_counter < CHUNK_SIZE * (block_id_counter+1) && line != null);

                threadpool.submit(new ProcessChunkThread(chunk_text, block_id_counter, parser));
                block_id_counter++;
            }while (line != null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        threadpool.shutdown();
        while(!threadpool.isTerminated()) {
            try {
                threadpool.awaitTermination(100, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void merge_chunks(){

        Lexicon merged_lexicon = new Lexicon();
        Lexicon current_lexicon;

        File lexicon_directory = new File("data/intermediate_postings/lexicon");
        File index_directory = new File("data/intermediate_postings/index");

        File[] lexicon_files = lexicon_directory.listFiles();

        String current_lexicon_filename;

        // TODO - we can use multithreading in merging lexicon -> seems not
        // merge all intermediate lexicon to create a unique one
        for (File lexiconFile : lexicon_files) {
            current_lexicon_filename = lexiconFile.getPath();
            current_lexicon = readLexicon(current_lexicon_filename);
            merged_lexicon.merge(current_lexicon);
        }
        logger.info("Intermediate Lexicons merged!");
        System.out.println(merged_lexicon);


        InvertedIndex merged_index = new InvertedIndex();
        PostingList current_posting_list;

        int i = 0;
        for(String term : merged_lexicon.keySet()){

            current_posting_list = new PostingList();
            TermEntryList termEntryList = merged_lexicon.get(term);
            termEntryList.setTerm_index(i);
            for(TermEntry termEntry : termEntryList){
                PostingList p = readPostingList(index_directory.getPath(), termEntry, true);
                current_posting_list.appendPostings(p);
            }
            current_posting_list.generatePointers();
            merged_index.addPostingList(current_posting_list);
            i++;
        }
        logger.info("Intermediate Posting Lists merged");
        System.out.println(merged_index);

        write(merged_index, merged_lexicon, "data/index.bin", "data/lexicon.bin", false);
    }

    public void debug_fun(){
        Lexicon lexicon = readLexicon("data/lexicon.bin");


        TermEntryList tel = lexicon.get("manhattan");
        System.out.println(tel);
        int n = tel.getTermEntryList().size();

        PostingList postingList = readPostingList("data/", tel.getTermEntryList().get(n-1), false);
        System.out.println(postingList);
    }

}
