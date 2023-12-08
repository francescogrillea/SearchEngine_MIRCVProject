package org.offline_phase;

import org.common.*;
import org.offline_phase.ContentParser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import org.common.ChunkHandler;

import static org.offline_phase.Spimi.logger;

public class ProcessChunkThread implements Runnable{

    private final ContentParser parser;
    private final String chunk_content;
    private final int block_index;

    public ProcessChunkThread(StringBuilder chunk_content, int block_index, ContentParser parser) {
        this.chunk_content = chunk_content.toString();
        this.block_index = block_index;
        this.parser = parser;
    }

    @Override
    public void run() {

        logger.info("Block " + this.block_index + " has stated to be processed");

        InvertedIndex intermediateIndex = new InvertedIndex();
        Lexicon intermediateLexicon = new Lexicon();
        DocIndexList docIndexList = new DocIndexList();

        String[] documents = this.chunk_content.split("\n");

        int doc_id_counter;
        int index;
        String[] fields;

        int pid = 0;
        for (int i = 0; i < documents.length; i++){
            DocIndex doc_index = new DocIndex();
            doc_id_counter = (this.block_index * Spimi.CHUNK_SIZE) + i + 1;


            fields = documents[i].split("\t");

            // set pid
            pid = Integer.parseInt(fields[0]);
            doc_index.setPid(pid);
            //set document lenght
            doc_index.setDocLength(documents[i].length());
            // set document id
            doc_index.setDoc_id(doc_id_counter);

            docIndexList.addDocIndex(doc_index);


            if (!fields[1].isEmpty()){
                List<String> terms = parser.processContent(fields[1]);

                for(String term : terms){
                    index = intermediateLexicon.add(term);
                    intermediateIndex.addPosting(index, new Posting(doc_id_counter));
                }
            }
        }

        String index_filename = String.format(ChunkHandler.basename_intermediate_index + "block_index_%05d.bin", this.block_index);
        String lexicon_filename = String.format(ChunkHandler.basename_intermediate_lexicon + "block_lexicon_%05d.bin", this.block_index);
        String docindex_filename = String.format("data/intermediate_postings/docindex/"+ "block_docindex_%05d.bin", this.block_index);


        // write intermediate posting lists and intermediate lexicon to disk
        try (FileOutputStream indexFileOutputStream = new FileOutputStream(index_filename, false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            TermEntry termEntry;
            for(String term : intermediateLexicon.keySet()){
                int posting_index = intermediateLexicon.get(term).getTerm_index();
                termEntry = ChunkHandler.writePostingList(indexFileChannel, intermediateIndex.getInverted_index().get(posting_index), true);
                termEntry.setBlock_index(this.block_index);
                intermediateLexicon.get(term).addTermEntry(termEntry);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ChunkHandler.writeLexicon(intermediateLexicon, lexicon_filename, true);
        ChunkHandler.writeDocIndex(docIndexList, docindex_filename);


    }


}
