package org.online_phase;

import org.common.*;
import org.common.Scoring;
import org.common.encoding.*;
import org.offline_phase.ContentParser;
import org.online_phase.scoring.BM25;
import org.online_phase.scoring.ScoringInterface;
import org.online_phase.scoring.TFIDF;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

public class DAAT {

    private final ScoringInterface scoring;
    private final Lexicon lexicon;
    private final DocIndex doc_index;
    private final ContentParser parser;

    public DAAT(boolean process_data_flag, boolean compress_data_flag, boolean bm25) {

        if(!bm25)
            this.scoring = new TFIDF(DocIndexReader.basename_docindex);
        else
            this.scoring = new BM25(DocIndexReader.basename_docindex);

        this.lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        this.doc_index = DocIndexReader.readDocIndex("data/doc_index.bin"); // TODO - da togliere, troppa roba in memoria
        this.parser = new ContentParser("data/stop_words_english.txt", process_data_flag);

        if(compress_data_flag)
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        else
            PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());
    }

    public ScoreBoard executeDisjunctiveQuery(String query, int top_k) {

        List<String> query_terms = parser.processContent(query);    // TODO - può essere utile fare un query term index?
        ScoreBoard scoreBoard = new ScoreBoard(top_k);
        System.out.println(query_terms);
        int tf;
        int df;

        List<PostingListBlockReader> postingReaders = new ArrayList<>();

        try{
            // init posting readers
            PostingListBlockReader current_reader;
            for (String word : query_terms){
                try{
                    postingReaders.add(new PostingListBlockReader(lexicon.get(word).getTermEntryList().get(0), word));
                }catch (NullPointerException e){
                    System.out.println("Word " + word + " not found in lexicon");
                }
            }

            // if no words have been written or no words found in lexicon
            if(postingReaders.size() == 0)
                return null;

            int min;
            float score;
            List<PostingListReader> to_delete = new ArrayList<>();

            while(postingReaders.size() != 0){

                // init all blocks
                for(PostingListBlockReader reader : postingReaders)
                    reader.readBlock();

                // search min doc_id
                min = Integer.MAX_VALUE;
                for(PostingListBlockReader reader : postingReaders)
                    if(reader.getDocID() < min)
                        min = reader.getDocID();

                score = 0;

                to_delete.clear();

                for (PostingListBlockReader reader : postingReaders) {
                    if (reader.getDocID() == min) {

                        tf = reader.getTermFreq();
                        df = lexicon.get(reader.getTerm()).getTermEntry(0).getDocument_frequency();

                        if(scoring instanceof TFIDF)
                            score += scoring.computeScore(tf, df);
                        else
                            score += scoring.computeScore(tf, df, doc_index.get(reader.getDocID()).getLength());

                        if(!reader.nextPosting()){
                            reader.close();
                            to_delete.add(reader);
                        }
                    }
                }
                postingReaders.removeAll(to_delete);

                // save tuple <doc_id, score>
                scoreBoard.add(min, score);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        scoreBoard.clip();
        return scoreBoard;
    }

    public static List<Integer> executeConjuntiveQuery(String query, boolean tfidf, int top_k) {
        return null;
    }


    }
