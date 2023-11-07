package org.common;

import org.offline_phase.ContentParser;
import org.offline_phase.IntermediatePostings;

import java.util.Arrays;
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

        IntermediatePostings intermediateIndex = new IntermediatePostings();
        String[] documents = this.chunk_content.split("\n");
        int doc_id_counter;

        for (int i = 0; i < documents.length; i++){
            doc_id_counter = (this.block_index * CHUNK_SIZE) + i;

            String[] fields = documents[i].split("\t");
            if (! fields[1].isEmpty()){
                List<String> terms = parser.processContent(fields[1]);

                for(String term : terms)
                    intermediateIndex.addPosting(term, doc_id_counter);
            }
        }
        //System.out.println("Block " + block_index + ": \n" + intermediateIndex);
        String filename = String.format(super.basename + "block_%05d.ser", this.block_index);
        chunk_to_disk(intermediateIndex, filename);
    }
}
