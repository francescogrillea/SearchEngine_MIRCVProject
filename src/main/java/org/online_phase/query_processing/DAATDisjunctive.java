package org.online_phase.query_processing;

import org.common.*;
import org.common.encoding.*;
import org.offline_phase.ContentParser;
import org.online_phase.ScoreBoard;
import org.online_phase.scoring.BM25;
import org.online_phase.scoring.ScoringInterface;
import org.online_phase.scoring.TFIDF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The DAATDisjunctive class implements the Document-At-A-Time (DAAT) disjunctive query processing
 * for retrieving top-k documents based on a given query using a specified scoring mechanism.
 * It supports both TF-IDF and BM25 scoring methods and provides the results in the form of a ScoreBoard.
 */
public class DAATDisjunctive implements QueryProcessing{

    private final ScoringInterface scoring; // to compute scores for document-query matches
    private final Lexicon lexicon;      // containing information about terms in the document collection
    private final ContentParser parser;     // used to process and tokenize content, including stop-word removal


    public DAATDisjunctive(boolean process_data_flag, boolean compress_data_flag, boolean bm25) {

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
     * Executes a DAAT disjunctive query using the specified query string and retrieves the top-k results.
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
            // init posting readers
            TermEntry termEntry;
            for (String word : query_terms){
                try{
                    termEntry = lexicon.get(word).getTermEntryList().get(0);
                    postingReaders.add(new PostingListBlockReader(termEntry, word,scoring instanceof BM25));
                    document_freqs.add(termEntry.getDocument_frequency());
                }catch (NullPointerException e){
                    System.out.println("Word " + word + " not found in lexicon");
                }
            }

            // if no words have been written or no words found in lexicon
            if(postingReaders.size() == 0)
                return scoreBoard;

            int min_docID;
            float score;
            List<PostingListBlockReader> to_delete = new ArrayList<>();

            int df_index = 0;
            int df_size = document_freqs.size();

            // read the first (or next) block of each posting list
            for(PostingListBlockReader reader : postingReaders)
                reader.readBlock();

            // iterate over all the blocks of the query posting list
            while(postingReaders.size() != 0){

                min_docID = Integer.MAX_VALUE;

                // find the min doc_id for each block
                for(PostingListBlockReader reader : postingReaders)
                    if(reader.getDocID() < min_docID)
                        min_docID = reader.getDocID();

                score = 0;
                df_index = 0;
                to_delete.clear();


                for (PostingListBlockReader block : postingReaders) {

                    // compute score for the following doc_id
                    if (block.getDocID() == min_docID) {

                        tf = block.getTermFreq();
                        df = document_freqs.get(df_index);

                        if(scoring instanceof TFIDF)
                            score += scoring.computeScore(tf, df);
                        else
                            score += scoring.computeScore(tf, df, ((BM25) scoring).getDl(block.getDocID() - 1));

                        if(!block.nextPosting()){
                            block.close();
                            to_delete.add(block);
                        }
                    }
                    df_index = (df_index + 1) % df_size;
                }
                postingReaders.removeAll(to_delete);
                df_size -= to_delete.size();

                // save tuple <doc_id, score>
                scoreBoard.add(min_docID, score);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        scoreBoard.clip();
        scoreBoard.setDoc_ids(DocIndexReader.getPids(scoreBoard.getDoc_ids()));
        return scoreBoard;
    }

}
