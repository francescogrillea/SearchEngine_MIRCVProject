package org.online_phase.query_processing;

import org.common.*;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.offline_phase.ContentParser;
import org.online_phase.ScoreBoard;
import org.online_phase.scoring.BM25;
import org.online_phase.scoring.ScoringInterface;
import org.online_phase.scoring.TFIDF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The DAATConjunctive class implements the Document-At-A-Time (DAAT) conjunctive query processing
 * for retrieving top-k documents based on a given query using a specified scoring mechanism.
 * It supports both TF-IDF and BM25 scoring methods and provides the results in the form of a ScoreBoard.
 */
public class DAATConjunctive implements QueryProcessing{

    private final ScoringInterface scoring; // to compute scores for document-query matches
    private final Lexicon lexicon;      // containing information about terms in the document collection
    private final ContentParser parser;     // used to process and tokenize content, including stop-word removal


    public DAATConjunctive(boolean process_data_flag, boolean compress_data_flag, boolean bm25) {

        if(!bm25)
            this.scoring = new TFIDF(DocIndexReader.basename_docindex);
        else
            this.scoring = new BM25(DocIndexReader.basename_docindex);
        System.gc();

        this.lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        this.parser = new ContentParser("data/stop_words_english.txt", process_data_flag);

        if(compress_data_flag)
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        else
            PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());
    }

    /**
     * Executes a DAAT conjunctive query using the specified query string and retrieves the top-k results.
     *
     * @param query The query string to be processed.
     * @param top_k The number of top results to retrieve.
     * @return A ScoreBoard containing the top-k document IDs and their corresponding scores.
     */
    @Override
    public ScoreBoard executeQuery(String query, int top_k) {

        // process query content
        List<String> query_terms = this.parser.processContent(query);

        int tf;
        int df;
        // store document frequencies for each query term to save time during the scoring function -> Object Size: 20 bytes * n of query terms -> negligible
        List<Integer> document_freqs = new ArrayList<>();
        // initialize a ScoreBoard to store the top k documents retrieved
        ScoreBoard scoreBoard = new ScoreBoard(top_k);

        List<PostingListBlockReader> postingReaders = new ArrayList<>();

        try{

            // initialize one posting reader for each query term
            TermEntry termEntry;
            for (String word : query_terms){
                try{
                    termEntry = lexicon.get(word).getTermEntryList().get(0);
                    postingReaders.add(new PostingListBlockReader(termEntry, word, scoring instanceof BM25));
                    document_freqs.add(termEntry.getDocument_frequency());
                }catch (NullPointerException e){
                    // if query term not exists, return an empty ScoreBoard
                    System.out.println("Word " + word + " not found in lexicon");
                    return scoreBoard;
                }
            }

            // if no words have been written or no words found in lexicon
            if(postingReaders.size() == 0)
                return null;

            int max_docID;
            float score;
            List<PostingListReader> to_delete = new ArrayList<>();

            int df_index = 0;
            int df_size = document_freqs.size();

            // read the first (or next) block of each posting list
            for(PostingListBlockReader reader : postingReaders)
                reader.readBlock();

            // until all posting lists are not finished yet
            while (postingReaders.size() != 0){
                max_docID = -1;

                boolean all_equals = true;  // true if the cursor of all readers is on the same doc_id

                int doc_id = postingReaders.get(0).getDocID();

                // find the max doc_id
                for(PostingListBlockReader reader : postingReaders) {
                    if (reader.getDocID() > max_docID)
                        max_docID = reader.getDocID();

                    if (reader.getDocID() != doc_id)
                        all_equals = false;
                }

                // init partial score
                score = 0;
                to_delete.clear();

                if(all_equals){

                    // compute score
                    for (PostingListBlockReader block : postingReaders){

                        tf = block.getTermFreq();
                        df = document_freqs.get(df_index);

                        if(scoring instanceof TFIDF)
                            score += scoring.computeScore(tf, df);
                        else
                            score += scoring.computeScore(tf, df, ((BM25) scoring).getDl(block.getDocID() - 1));

                        // move to the next posting and if there's no posting to read, close those readers
                        if(!block.nextPosting()){
                            block.close();
                            to_delete.add(block);
                        }
                        df_index = (df_index + 1) % df_size;
                    }
                    scoreBoard.add(max_docID, score);

                }
                else{
                    for (PostingListBlockReader block : postingReaders)
                        if(block.getDocID() < max_docID){

                            // move to the next posting and check if there's still posting to read
                            if(!block.nextPosting()){
                                block.close();
                                to_delete.add(block);
                            }
                        }
                }

                // if a posting list must be removed, the conjunctive process can't find any other acceptable results
                if(to_delete.size() > 0)
                    break;
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        scoreBoard.clip();
        scoreBoard.setDoc_ids(DocIndexReader.getPids(scoreBoard.getDoc_ids()));

        return scoreBoard;
    }
}
