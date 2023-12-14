package org.offline_phase;

import org.common.*;
import org.offline_phase.ContentParser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
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

        ArrayList<PostingList> intermediateIndex = new ArrayList<>();
        Lexicon intermediateLexicon = new Lexicon();
        DocIndex intemediateDocIndex = new DocIndex();

        String[] documents = this.chunk_content.split("\n");

        int doc_id_counter;
        int index;
        String[] fields;

        for (int i = 0; i < documents.length; i++){
            doc_id_counter = (this.block_index * Spimi.CHUNK_SIZE) + i + 1;

            fields = documents[i].split("\t");

            if (!fields[1].isEmpty()){
                List<String> terms = parser.processContent(fields[1]);

                // add the doc_id to DocIndex
                intemediateDocIndex.add(doc_id_counter, new DocInfo(Integer.parseInt(fields[0]), terms.size()));

                for(String term : terms){
                    // add term to intermediate Lexicon
                    index = intermediateLexicon.add(term);

                    // add term's posting list to intermediate Index
                    if(index < intermediateIndex.size())
                        intermediateIndex.get(index).addPosting(doc_id_counter);
                    else
                        intermediateIndex.add(index, new PostingList(doc_id_counter));
                }
            }
        }

        String index_filename = String.format(ChunkHandler.basename_intermediate_index + "block_index_%05d.bin", this.block_index);
        String lexicon_filename = String.format(ChunkHandler.basename_intermediate_lexicon + "block_lexicon_%05d.bin", this.block_index);
        String docindex_filename = String.format(ChunkHandler.basename_intermediate_docindex + "block_docindex_%05d.bin", this.block_index);


        // write intermediate posting lists and intermediate lexicon to disk
        try (FileOutputStream indexFileOutputStream = new FileOutputStream(index_filename, false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            TermEntry termEntry;
            for(String term : intermediateLexicon.keySet()){
                int posting_index = intermediateLexicon.get(term).getTerm_index();
                //termEntry = ChunkHandler.writePostingList(indexFileChannel, intermediateIndex.get(posting_index), true);
                termEntry = ChunkHandler.writeIntermediatePostingList(indexFileChannel, intermediateIndex.get(posting_index));
                termEntry.setBlock_index(this.block_index);
                intermediateLexicon.get(term).addTermEntry(termEntry);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ChunkHandler.writeLexicon(intermediateLexicon, lexicon_filename, true);
        ChunkHandler.writeDocIndex(intemediateDocIndex, docindex_filename);
    }

}
