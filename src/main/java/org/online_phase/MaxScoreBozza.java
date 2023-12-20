package org.online_phase;

import org.common.*;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.offline_phase.ContentParser;
import org.online_phase.scoring.BM25;
import org.online_phase.scoring.ScoringInterface;
import org.online_phase.scoring.TFIDF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class MaxScoreBozza { //da rimuovere. è solo una bozza
    private final ScoringInterface scoring;
    private final Lexicon lexicon;
    private final DocIndex doc_index;
    private final ContentParser parser;
    MaxScoreBozza(boolean process_data_flag, boolean compress_data_flag, boolean bm25){

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
    public ScoreBoard executeDisjunctiveQuery(String query, int top_k,boolean bm25){

        List<String> query_terms = this.parser.processContent(query);    // TODO - può essere utile fare un query term index?
        ScoreBoard scoreBoard = new ScoreBoard(top_k);

        List<PostingListBlockReader> postingReaders = new ArrayList<>();

        //List<TermEntry> termEntries = new ArrayList<>();
        // Add more TermEntry objects as needed
        // Sort the list by length in decreasing order


        try{
            //ogni postinglist block reader avrà anche un campo per il suo tf upper bound che viene assegnato in questa fase
            TermEntry termEntry;

            for (String word : query_terms){
                try{
                    termEntry = lexicon.get(word).getTermEntryList().get(0);
                    postingReaders.add(new PostingListBlockReader(termEntry, word,bm25)); //in questa fase dalla term entry vengono presi anche i termupperbounds, mettendo bm25 o tf idf a seconda dello scoring scelto alla chiamata

                }catch (NullPointerException e){
                    System.out.println("Word " + word + " not found in lexicon");
                }
            }

            if(postingReaders.isEmpty())
                return null;

            Collections.sort(postingReaders, (entry1, entry2) -> Float.compare(entry2.getTermUpperBound(), entry1.getTermUpperBound()));

            float termUpperBoundsSum =0;
            for(PostingListBlockReader p : postingReaders){
                termUpperBoundsSum += p.getTermUpperBound();
                p.setTermUpperBound(termUpperBoundsSum);
            }

            //ora tutti i postinglist block reader sono inizializzati con i valori corretti di termUpperBounds cumulati

            int min_docID=0;
            float score=0;
            int pivot=0; //le posting da pivot a n sono le essential ones. il pivot va ri-determinato ogni volta che una posting termina
            List<PostingListReader> to_delete = new ArrayList<>();

            // read the first (or next) block of each posting list
            for(PostingListBlockReader reader : postingReaders)
                reader.readBlock();

            while(pivot<postingReaders.size() && min_docID != Integer.MAX_VALUE){
                min_docID=Integer.MAX_VALUE;
                for(int i = pivot; i < postingReaders.size(); i++){ //per ciascuna delle essential
                    if(postingReaders.get(i).getDocID()<min_docID){
                        min_docID=postingReaders.get(i).getDocID();
                    }
                }
                for(int i = pivot;i < postingReaders.size(); i++){
                    if(postingReaders.get(i).getDocID()==min_docID){
                        if(bm25){
                            score += scoring.computeScore(postingReaders.get(i).getTermFreq(),
                                    postingReaders.get(i).getDocumentFrequency(),
                                    doc_index.get(postingReaders.get(i).getDocID()).getLength());
                        }else{
                            score += scoring.computeScore(postingReaders.get(i).getTermFreq(),postingReaders.get(i).getDocumentFrequency());
                        }
                        if(!postingReaders.get(i).nextPosting()){
                            postingReaders.get(i).close();
                            to_delete.add(postingReaders.get(i));
                        }
                    }
                }

                for(int i = pivot -1; i >= 0; i--){
                    if(score + postingReaders.get(i).getTermUpperBound()<= scoreBoard.getThreshold()){
                        break;
                    }

                    if(bm25){
                        score += scoring.computeScore(postingReaders.get(i).nextGEQ(min_docID),
                                postingReaders.get(i).getDocumentFrequency(),
                                doc_index.get(postingReaders.get(i).getDocID()).getLength());
                    }else{
                        score += scoring.computeScore(postingReaders.get(i).nextGEQ(min_docID),postingReaders.get(i).getDocumentFrequency());
                    }
                }
                postingReaders.removeAll(to_delete);
                to_delete.clear();
                if(scoreBoard.add(min_docID,score)){
                    pivot = 0;
                    while(pivot< postingReaders.size() && postingReaders.get(pivot).getTermUpperBound()<=scoreBoard.getThreshold()){
                        pivot++;
                    }
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }


        scoreBoard.clip();
        scoreBoard.setDoc_ids(doc_index.getPids(scoreBoard.getDoc_ids()));
        return scoreBoard;
    }
}
