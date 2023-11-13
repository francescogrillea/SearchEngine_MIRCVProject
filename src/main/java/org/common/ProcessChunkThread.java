package org.common;

import org.apache.commons.lang3.SerializationUtils;
import org.offline_phase.ContentParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
            doc_id_counter = (this.block_index * CHUNK_SIZE) + i;

            String[] fields = documents[i].split("\t");
            if (! fields[1].isEmpty()){
                List<String> terms = parser.processContent(fields[1]);

                for(String term : terms){
                    int index = intermediateLexicon.indexOf(term);
                    if(index < 0){
                        index = intermediateLexicon.addTerm(term);
                        intermediateIndex.addPosting(index, new Posting(doc_id_counter));
                    }
                    else
                        intermediateIndex.appendPosting(index, new Posting(doc_id_counter));
                }
            }
        }
        System.out.println("Block " + block_index);
        System.out.println(intermediateLexicon);
        System.out.println(intermediateIndex);
        String index_filename = String.format(super.basename + "block_index_%05d.ser", this.block_index);
        String lexicon_filename = String.format(super.basename + "block_lexicon_%05d.ser", this.block_index);

        write(intermediateIndex, intermediateLexicon, index_filename, lexicon_filename);
    }

}
