package org.common;

import org.apache.commons.lang3.SerializationUtils;
import org.offline_phase.ContentParser;
import java.util.List;

public class ProcessChunkThread extends ChunkHandler implements Runnable{

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

        InvertedIndex intermediateIndex = new InvertedIndex();
        Lexicon intermediateLexicon = new Lexicon();

        String[] documents = this.chunk_content.split("\n");
        int doc_id_counter;

        for (int i = 0; i < documents.length; i++){
            doc_id_counter = (this.block_index * CHUNK_SIZE) + i + 1;

            String[] fields = documents[i].split("\t");
            if (! fields[1].isEmpty()){
                List<String> terms = parser.processContent(fields[1]);
                int index;
                for(String term : terms){
                    index = intermediateLexicon.add(term);
                    intermediateIndex.addPosting(index, new Posting(doc_id_counter));
                }
            }
        }
        //System.out.println("Last block written: " + block_index);
        //System.out.println("BLOCK INDEX [LEXICON]: " + intermediateLexicon);
        //System.out.println("BLOCK INDEX [INDEX]: " + intermediateIndex);
        String index_filename = String.format(this.basename + "index/block_index_%05d.bin", this.block_index);
        String lexicon_filename = String.format(this.basename + "lexicon/block_lexicon_%05d.bin", this.block_index);
        write(intermediateIndex, intermediateLexicon, index_filename, lexicon_filename, true);
    }

}
