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

public class DAATDisjunctive  implements QueryProcessing{

    private final ScoringInterface scoring;
    private final ScoreBoard scoreBoard;
    private final Lexicon lexicon;
    private final ContentParser parser;


    public DAATDisjunctive(boolean process_data_flag, boolean compress_data_flag, boolean bm25, int top_k) {

        if(!bm25)
            this.scoring = new TFIDF(DocIndexReader.basename_docindex);
        else
            this.scoring = new BM25(DocIndexReader.basename_docindex);

        this.lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        this.parser = new ContentParser("data/stop_words_english.txt", process_data_flag);
        this.scoreBoard = new ScoreBoard(top_k);

        if(compress_data_flag)
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        else
            PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());
    }


    @Override
    public ScoreBoard executeQuery(String query) {
        List<String> query_terms = this.parser.processContent(query);
        System.out.println(query_terms);

        int tf;
        int df;
        // store document frequencies for each query term to save time during the scoring function -> Object Size: 4bytes * n of query terms
        List<Integer> document_freqs = new ArrayList<>();   // TODO - capire se serve


        List<PostingListBlockReader> postingReaders = new ArrayList<>();

        try{

            // init posting readers
            TermEntry termEntry;
            for (String word : query_terms){
                try{
                    termEntry = lexicon.get(word).getTermEntryList().get(0);
                    postingReaders.add(new PostingListBlockReader(termEntry, word, scoring instanceof BM25));
                    document_freqs.add(termEntry.getDocument_frequency());
                }catch (NullPointerException e){
                    // TODO - if query term not exists, return a empty lists
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

            while (postingReaders.size() != 0){
                max_docID = -1;

                boolean all_equals = true;

                int doc_id = postingReaders.get(0).getDocID();

                for(PostingListBlockReader reader : postingReaders) {
                    if (reader.getDocID() > max_docID)
                        max_docID = reader.getDocID();

                    if (reader.getDocID() != doc_id)
                        all_equals = false;
                }

                score = 0;
                to_delete.clear();

                if(all_equals){

                    // compute score
                    for (PostingListBlockReader block : postingReaders){

                        tf = block.getTermFreq();
                        df = document_freqs.get(df_index);
                        //df = lexicon.get(block.getTerm()).getTermEntryList().get(0).getDocument_frequency();

                        if(scoring instanceof TFIDF)        // TODO - non mi piace sta scrittura
                            score += scoring.computeScore(tf, df);
                        else
                            score += scoring.computeScore(tf, df, DocIndexReader.readDocInfo(block.getDocID()).getLength());    // TODO - il docIndex vorrei non caricarlo in memoria

                        // move to the next posting and check if there's still posting to read
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
