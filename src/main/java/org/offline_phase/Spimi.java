package org.offline_phase;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.common.*;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.online_phase.scoring.BM25;
import org.online_phase.scoring.TFIDF;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Spimi {

    private int doc_id_counter = 0;
    private int block_id_counter = 0;
    static final int  CHUNK_SIZE = 25000;
    private final int _DEBUG_N_DOCS = Integer.MAX_VALUE;    // n of documents we want to analyze
    static Logger logger = Logger.getLogger(Spimi.class.getName());
    private final boolean process_data_flag;


    public Spimi(boolean process_data_flag, boolean compress_data_flag) {
        this.process_data_flag = process_data_flag;

        if(compress_data_flag)
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        else
            PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());

    }

    public void run(String collection_filepath){

        ExecutorService threadpool = Executors.newFixedThreadPool(10);

        try(FileInputStream inputStream = new FileInputStream(collection_filepath);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(tarArchiveInputStream, StandardCharsets.UTF_8))){

            tarArchiveInputStream.getNextTarEntry();

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

            // wait all threads to finish
            threadpool.shutdown();
            try{
                threadpool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void merge_chunks(){

        Lexicon merged_lexicon = new Lexicon();
        Lexicon current_lexicon;

        File lexicon_directory = new File(LexiconReader.basename_intermediate_lexicon);

        File[] lexicon_files = lexicon_directory.listFiles();   // TODO - assert is != null

        long start_time = System.currentTimeMillis();
        // merge all intermediate lexicon to create a unique one
        for (File lexiconFile : lexicon_files) {
            current_lexicon = LexiconReader.readLexicon(LexiconReader.basename_intermediate_lexicon + lexiconFile.getName());
            merged_lexicon.merge(current_lexicon);
        }
        logger.info("Intermediate Lexicons merged in " + (System.currentTimeMillis() - start_time)/1000.0 + "s");


        // merge all intermediate DocIndex to create a unique one
        File docindex_directory = new File(DocIndexReader.basename_intermediate_docindex);
        File[] docindex_files = docindex_directory.listFiles();
        start_time = System.currentTimeMillis();
        try (FileOutputStream indexFileOutputStream = new FileOutputStream(DocIndexReader.basename + "doc_index.bin", false);
             FileChannel docIndexFileChannel = indexFileOutputStream.getChannel()) {

            for(File docIndex_file : docindex_files){

                try (FileInputStream indexFileInputStream = new FileInputStream(DocIndexReader.basename_intermediate_docindex + docIndex_file.getName());
                     FileChannel docIndex_intermediate_FileChannel = indexFileInputStream.getChannel()) {

                    long size = docIndex_intermediate_FileChannel.size();
                    ByteBuffer buffer = ByteBuffer.allocate((int) size);
                    docIndex_intermediate_FileChannel.read(buffer);
                    buffer.flip();

                    docIndexFileChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Intermediate DocIndex merged in " + (System.currentTimeMillis() - start_time)/1000.0 + "s");


        // merge all intermediate indexes
        start_time = System.currentTimeMillis();
        try (FileOutputStream indexFileOutputStream = new FileOutputStream("data/index.bin", false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            ArrayList<FileInputStream> fileInputStreams = new ArrayList<>();
            File index_directory = new File(PostingListReader.basename_intermediate_index);
            File[] indexFiles = index_directory.listFiles();
            // TODO - assert != null
            for(File indexFile : indexFiles){
                fileInputStreams.add(new FileInputStream(PostingListReader.basename_intermediate_index + indexFile.getName()));
            }

            ArrayList<FileChannel> fileChannels = new ArrayList<>();
            for(FileInputStream fos : fileInputStreams)
                fileChannels.add(fos.getChannel());

            BM25 bm25 = new BM25(DocIndexReader.basename_docindex);
            TFIDF tfidf = new TFIDF(DocIndexReader.basename_docindex);

            TermEntry finalTermEntry;
            int i = 0;
            PostingList postingList;
            for(String term : merged_lexicon.keySet()){

                TermEntryList termEntryList = merged_lexicon.get(term);
                termEntryList.setTerm_index(i);

                postingList = new PostingList();
                for(TermEntry termEntry : termEntryList){
                    PostingList p = PostingListReader.readIntermediatePostingList(fileChannels.get(termEntry.getBlock_index()), termEntry);
                    postingList.appendPostings(p);
                }
                postingList.initPointers();
                finalTermEntry = PostingListReader.writePostingList(indexFileChannel, postingList);

                // TODO - use multithreading
                finalTermEntry.setTfidf_upper_bound(tfidf.getTermUpperBound(postingList));
                finalTermEntry.setBm25_upper_bound(bm25.getTermUpperBound(postingList));
                merged_lexicon.get(term).resetTermEntry(finalTermEntry);
                i++;
            }

            // close all file channes
            for(FileChannel fc : fileChannels)
                fc.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Intermediate Index merged in " + (System.currentTimeMillis() - start_time)/1000.0 + "s");
        // TODO - wait all threads to finish
        LexiconReader.writeLexicon(merged_lexicon, LexiconReader.basename + "lexicon.bin", false);
    }

    public void debug_fun(){

        //DocIndex docIndex = DocIndexReader.readDocIndex(DocIndexReader.basename_docindex);
        //System.out.println(docIndex);
        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");

        String term = "manhattan";
        TermEntry termEntry = lexicon.get(term).getTermEntry(0);
        System.out.println(termEntry);
        //PostingList postingList = PostingListReader.readPostingList(termEntry);
        //System.out.println(postingList);
//
//        try(PostingListBlockReader reader = new PostingListBlockReader(termEntries.getTermEntryList().get(0), term)){
//
//            int tf = reader.nextGEQ(88413677);
//            System.out.println(tf);
//
//        }catch (IOException e){
//            e.printStackTrace();
//        }
    }

}
