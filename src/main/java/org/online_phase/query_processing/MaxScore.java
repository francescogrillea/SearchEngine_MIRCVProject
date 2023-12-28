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
import java.util.Collections;

public class MaxScore implements QueryProcessing {
    private final ScoringInterface scoring;
    private Lexicon lexicon;
    private final ContentParser parser;

    public MaxScore(boolean process_data_flag, boolean compress_data_flag, boolean bm25){

        if(!bm25)
            this.scoring = new TFIDF(DocIndexReader.basename_docindex);
        else
            this.scoring = new BM25(DocIndexReader.basename_docindex);

        this.lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        this.parser = new ContentParser("data/stop_words_english.txt", process_data_flag);

        if(compress_data_flag)
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        else
            PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());
    }

    @Override
    public ScoreBoard executeQuery(String query, int top_k) {
        List<String> query_terms = this.parser.processContent(query);

        List<PostingListBlockReader> postingReaders = new ArrayList<>();
        ScoreBoard scoreBoard = new ScoreBoard(top_k);
        //List<TermEntry> termEntries = new ArrayList<>();
        // Add more TermEntry objects as needed
        // Sort the list by length in decreasing order


        try{
            //ogni postinglist block reader avrà anche un campo per il suo tf upper bound che viene assegnato in questa fase
            TermEntry termEntry;

            for (String word : query_terms){
                try{
                    termEntry = lexicon.get(word).getTermEntryList().get(0);
                    postingReaders.add(new PostingListBlockReader(termEntry, word, scoring instanceof BM25));
                }catch (NullPointerException e){
                    System.out.println("Word " + word + " not found in lexicon");
                }
            }

            if(postingReaders.isEmpty())
                return null;

            Collections.sort(postingReaders, (entry1, entry2) -> Float.compare(entry1.getTermUpperBound(), entry2.getTermUpperBound()));

            // accumulated upper bounds
            float termUpperBoundsSum = 0;
            for(PostingListBlockReader p : postingReaders){
                termUpperBoundsSum += p.getTermUpperBound();
                p.setTermUpperBound(termUpperBoundsSum);
            }


            //ora tutti i postinglist block reader sono inizializzati con i valori corretti di termUpperBounds cumulati
            int tf, df;
            int min_docID = 0;
            float score;
            int pivot = 0; //le posting da pivot a n sono le essential ones. il pivot va ri-determinato ogni volta che una posting termina
            List<PostingListBlockReader> to_delete = new ArrayList<>();

            // read the first (or next) block of each posting list
            for(PostingListBlockReader reader : postingReaders)
                reader.readBlock();

            while(pivot < postingReaders.size() && min_docID != Integer.MAX_VALUE){

                min_docID = Integer.MAX_VALUE;
                score = 0;

                // find min doc_id
                for(int i = pivot; i < postingReaders.size(); i++)
                    if(postingReaders.get(i).getDocID()<min_docID)
                        min_docID=postingReaders.get(i).getDocID();

                // analyze essential posting lists
                for(int i = pivot; i < postingReaders.size(); i++){

                    if(postingReaders.get(i).getDocID() == min_docID){

                        tf = postingReaders.get(i).getTermFreq();
                        df = postingReaders.get(i).getDocumentFrequency();

                        if(scoring instanceof BM25)
                            score += scoring.computeScore(tf, df, DocIndexReader.readDocInfo(postingReaders.get(i).getDocID()).getLength());
                        else
                            score += scoring.computeScore(tf, df);

                        if(!postingReaders.get(i).nextPosting()){
//                            System.out.println("Remove the " + i + "th reader");
                            postingReaders.get(i).close();
                            to_delete.add(postingReaders.get(i));
                        }
                    }
                }


                // analyze non-essential posting lists
                for(int i = pivot - 1; i >= 0; i--){
                    if(score + postingReaders.get(i).getTermUpperBound() <= scoreBoard.getThreshold())
                        break;

                    tf = postingReaders.get(i).nextGEQ(min_docID);
                    df = postingReaders.get(i).getDocumentFrequency();

                    if(scoring instanceof BM25)
                        score += scoring.computeScore(tf, df, DocIndexReader.readDocInfo(postingReaders.get(i).getDocID()).getLength());
                    else
                        score += scoring.computeScore(tf, df);
                }

                postingReaders.removeAll(to_delete);
                to_delete.clear();

                // if doc_id can be added to ScoreBoard (MinHeap)
                if(scoreBoard.add(min_docID,score)){

                    // pivot = 0; // why?
                    while(pivot < postingReaders.size() && postingReaders.get(pivot).getTermUpperBound() <= scoreBoard.getThreshold())
                        pivot++;
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }

        // cutoff at k
        scoreBoard.clip();
        // return the PID associated to the retrieved doc_ids
        scoreBoard.setDoc_ids(DocIndexReader.getPids(scoreBoard.getDoc_ids()));

        return scoreBoard;
    }


}
