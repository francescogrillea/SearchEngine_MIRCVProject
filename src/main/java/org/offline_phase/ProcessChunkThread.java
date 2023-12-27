package org.offline_phase;

import org.common.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static org.offline_phase.Spimi.logger;

/**
 * The ProcessChunkThread class represents a runnable thread for processing
 * a chunk of textual content. It is designed to be used in a multithreaded
 * environment during the Spimi indexing process.
 * Each instance of this class processes a specific chunk of content,
 * creates intermediate posting lists, intermediate lexicons, and intermediate
 * DocIndex structures, and writes them to disk.
 */
public class ProcessChunkThread implements Runnable{

    private final ContentParser parser; // the ContentParser instance used for processing content
    private final String chunk_content; // the textual content of the chunk to be processed
    private final int chunk_index;  // index associated to the processed chunk. Useful for intermediate filenames

    /**
     * Constructs a ProcessChunkThread with the specified chunk content, block index,
     * and ContentParser instance.
     *
     * @param chunk_content The textual content of the chunk to be processed.
     * @param chunk_index   The index associated with the processed chunk.
     * @param parser        The ContentParser instance used for processing content.
     */
    public ProcessChunkThread(StringBuilder chunk_content, int chunk_index, ContentParser parser) {
        this.chunk_content = chunk_content.toString();
        this.chunk_index = chunk_index;
        this.parser = parser;
    }

    /**
     * Executes the processing logic for the chunk, including tokenization, parsing,
     * and indexing. Generates intermediate index structures and writes them to disk.
     */
    @Override
    public void run() {

        logger.info("Block " + this.chunk_index + " has stated to be processed");

        ArrayList<PostingList> intermediateIndex = new ArrayList<>();
        Lexicon intermediateLexicon = new Lexicon();
        DocIndex intemediateDocIndex = new DocIndex();

        String[] documents = this.chunk_content.split("\n");

        int doc_id_counter;
        int index;
        String[] fields;

        for (int i = 0; i < documents.length; i++){
            doc_id_counter = (this.chunk_index * Spimi.CHUNK_SIZE) + i + 1;

            fields = documents[i].split("\t");

            if (!fields[1].isEmpty()){
                // process the chunk content
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

        // generate filenames for intermediate structures
        String index_filename = String.format(PostingListReader.basename_intermediate_index + "block_index_%05d.bin", this.chunk_index);
        String lexicon_filename = String.format(LexiconReader.basename_intermediate_lexicon + "block_lexicon_%05d.bin", this.chunk_index);
        String docindex_filename = String.format(DocIndexReader.basename_intermediate_docindex + "block_docindex_%05d.bin", this.chunk_index);


        // write intermediate posting lists to disk
        try (FileOutputStream indexFileOutputStream = new FileOutputStream(index_filename, false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            TermEntry termEntry;
            for(String term : intermediateLexicon.keySet()){
                int posting_index = intermediateLexicon.get(term).getTerm_index();
                termEntry = PostingListReader.writeIntermediatePostingList(indexFileChannel, intermediateIndex.get(posting_index));
                termEntry.setBlock_index(this.chunk_index);
                // associate a termEntry to the term associated to the written posting list
                intermediateLexicon.get(term).addTermEntry(termEntry);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // write intermediate lexicon and document index to disk
        LexiconReader.writeLexicon(intermediateLexicon, lexicon_filename);
        DocIndexReader.writeDocIndex(intemediateDocIndex, docindex_filename);
    }

}
