package org.online_phase;

import org.common.*;
import org.common.Scoring;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

public class DAAT {

    public List<Integer> executeDisjunctiveQuery(String query, boolean tfidf, int top_k) {

        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        DocIndex doc_index = DocIndexReader.readDocIndex("data/doc_index.bin");

        Scoring scorer = new Scoring("data/doc_index.bin");
        ScoreBoard scoreBoard = new ScoreBoard(10);

        int tf;
        int df;

        String[] words = query.split("\\s+");   // TODO-  content parser.parse(query()

        List<PostingListBlockReader> postingReaders = new ArrayList<>();

        try{
            // init posting readers
            for (String word : words)
                postingReaders.add(new PostingListBlockReader(lexicon.get(word).getTermEntryList().get(0), word));

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
                        if(tfidf)
                            score += scorer.tfidf(tf, df);
                        else
                            score += scorer.computeBM25(tf, df, doc_index.get(reader.getDocID()).getLength());

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

        return scoreBoard.getDoc_ids();
    }

    public List<Integer> executeConjuntiveQuery(String query, boolean tfidf, int top_k) {
        return null;
    }


    }
