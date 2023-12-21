package org.online_phase;

import org.common.*;
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

        List<String> query_terms = this.parser.processContent(query);    // TODO - può essere utile fare un query term index?
        ScoreBoard scoreBoard = new ScoreBoard(top_k);
        int tf;
        int df;
        // store document frequencies for each query term to save time during the scoring function -> Object Size: 4bytes * n of query terms
        List<Integer> document_freqs = new ArrayList<>();


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
                return null;

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


                /*
                    TODO - posting list block reader ha dentro un'instanza di PostingList
                        si, ma forse sarebbe piu' corretto / carino fare una lista di PostingList ottenute
                        tramite PostingListReader.readBlock() ->>>
                 */
                for (PostingListBlockReader block : postingReaders) {

                    // compute score for the following doc_id
                    if (block.getDocID() == min_docID) {

                        tf = block.getTermFreq();
                        df = document_freqs.get(df_index);
                        //df = lexicon.get(block.getTerm()).getTermEntryList().get(0).getDocument_frequency();

                        if(scoring instanceof TFIDF)        // TODO - non mi piace sta scrittura
                            score += scoring.computeScore(tf, df);
                        else
                            score += scoring.computeScore(tf, df, doc_index.get(block.getDocID()).getLength());    // TODO - il docIndex vorrei non caricarlo in memoria

                        if(!block.nextPosting()){
                            block.close();
                            to_delete.add(block);
                        }
                    }
                    df_index = (df_index + 1) % df_size;
                }
                postingReaders.removeAll(to_delete);
                df_size -= to_delete.size();    // TODO - controllare se effettivamente va bene

                // save tuple <doc_id, score>
                scoreBoard.add(min_docID, score);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        scoreBoard.clip();
        scoreBoard.setDoc_ids(doc_index.getPids(scoreBoard.getDoc_ids()));
        return scoreBoard;
    }


    public ScoreBoard executeConjunctiveQuery(String query, int top_k) {

        List<String> query_terms = this.parser.processContent(query);    // TODO - può essere utile fare un query term index?
        System.out.println(query_terms);

        ScoreBoard scoreBoard = new ScoreBoard(top_k);
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
                    System.out.println("Word " + word + " not found in lexicon");
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
                            score += scoring.computeScore(tf, df, doc_index.get(block.getDocID()).getLength());    // TODO - il docIndex vorrei non caricarlo in memoria

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
        scoreBoard.setDoc_ids(doc_index.getPids(scoreBoard.getDoc_ids()));

        return scoreBoard;
    }



}
