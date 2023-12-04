package org.offline_phase;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.common.*;
import org.common.encoding.NoEncoder;
import org.common.encoding.VBEncoder;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Spimi {

    private int doc_id_counter = 0;
    private int block_id_counter = 0;

    static final int  CHUNK_SIZE = 2;
    private final int _DEBUG_N_DOCS = Integer.MAX_VALUE;    // n of documents we want to analyze

    static Logger logger = Logger.getLogger(Spimi.class.getName());
    private final boolean process_data_flag;


    public Spimi(boolean process_data_flag, boolean compress_data_flag) {
        this.process_data_flag = process_data_flag;
        if(compress_data_flag)
            ChunkHandler.setEncoder(new VBEncoder());
        else
            ChunkHandler.setEncoder(new NoEncoder());
    }

    public void run(TarArchiveInputStream stream){

        ExecutorService threadpool = Executors.newFixedThreadPool(50);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))){

            ContentParser parser = new ContentParser("data/stop_words_english.txt", this.process_data_flag);
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

                threadpool.submit(new ProcessChunkThread(chunk_text, block_id_counter, parser));
                block_id_counter++;
            }while (line != null && doc_id_counter < _DEBUG_N_DOCS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // wait all threads to finish
        threadpool.shutdown();
        try{
            threadpool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void merge_chunks(){

        Lexicon merged_lexicon = new Lexicon();
        Lexicon current_lexicon;

        File lexicon_directory = new File(ChunkHandler.basename_intermediate_lexicon);
        File index_directory = new File("data/intermediate_postings/index");

        File[] lexicon_files = lexicon_directory.listFiles();   // TODO - assert is != null

        String current_lexicon_filename;

        // TODO - we can use multithreading in merging lexicon ?
        // merge all intermediate lexicon to create a unique one
        for (File lexiconFile : lexicon_files) {
            current_lexicon_filename = lexiconFile.getPath();
            current_lexicon = ChunkHandler.readLexicon(current_lexicon_filename);
            merged_lexicon.merge(current_lexicon);
        }
        logger.info("Intermediate Lexicons merged!");
        //System.out.println(merged_lexicon);
        // TODO - if concurrent hash map -> sort term entries by block_id


        try (FileOutputStream indexFileOutputStream = new FileOutputStream("data/index.bin", false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            TermEntry finalTermEntry;
            int i = 0;
            PostingList postingList;
            for(String term : merged_lexicon.keySet()){

                TermEntryList termEntryList = merged_lexicon.get(term);
                termEntryList.setTerm_index(i);

                postingList = new PostingList();
                for(TermEntry termEntry : termEntryList){
                    PostingList p = ChunkHandler.readPostingList(termEntry, true);
                    postingList.appendPostings(p);
                }
                postingList.generatePointers();
                //System.out.println("Term: " + term + "\t -> " + postingList);
                finalTermEntry = ChunkHandler.writePostingList(indexFileChannel, postingList, false);

                merged_lexicon.get(term).resetTermEntry(finalTermEntry);
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(merged_lexicon);
        ChunkHandler.writeLexicon(merged_lexicon, ChunkHandler.basename + "lexicon.bin", false);

        logger.info("Intermediate Posting Lists merged");
    }

    public void debug_fun(){
        Lexicon lexicon = ChunkHandler.readLexicon("data/lexicon.bin");
        //System.out.println(lexicon);

        TermEntryList termEntries = lexicon.get("project");
        PostingList postingList = ChunkHandler.readPostingList(termEntries.getTermEntryList().get(0), false);
        System.out.println(postingList);
    }

}
