package org.online_phase;

import opennlp.tools.cmdline.chunker.ChunkerModelLoader;
import org.common.*;
import org.common.Scoring;

import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import java.util.List;

public class DAAT {

    public List<Integer> executeQuery(String query, boolean tfidf, int top_k) {
        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");  // #TODO gli faremo leggere solo le entry dei term della query
        DocIndex doc_index = DocIndexReader.readDocIndex("data/doc_index.bin");

        Scoring scorer = new Scoring("data/doc_index.bin");
        List<Integer> dfs= new ArrayList<>();
        int threshold;
        List<Integer> doc_ids = new ArrayList<>();
        List<Float> scores = new ArrayList<>();

        String[] words = query.split("\\s+");


        List<PostingListReader> postingReaders = new ArrayList<>();
        for (String word : words) {
            postingReaders.add(new PostingListReader(new FileChannel(new FileInputStream),word));
            //da correggere e sistemare. ci sarà anche da passare il puntatore di termentry per dirgli da dove iniziare
        }

        while(postingReaders.size() != 0){
            //trova il lettore che è sul doc id piu basso
            //chiama la getFreqs di tutti i lettori su quel doc_id
            //calcola la tfidf (ecc) per quel doc e lo inserisce in lista se supera il threshold. se si aggiorna il threshold
            //chiama la next su tutti i blocchi dai quali si è letto
            //si ripete fino a che non si finiscono tutte le postinglist

            // cerchiamo il doc_id minimo

            int min = Integer.MAX_VALUE;
            for(PostingListReader reader : postingReaders){
                if(reader.getDocId() < min){
                    min = reader.getDocId();
                }
            }


            // score di un certo doc_id (il minimo) per quella query
            float score = 0;

            List<PostingListReader> to_delete = new ArrayList<>();

            for (PostingListReader reader : postingReaders) {
                if (reader.getDocId() == min) {
                    if(tfidf) {
                        score += scorer.tfidf(reader.getFreqs(), lexicon.get(reader.getWord()));
                    }else{
                        score += scorer.computeBM25(reader.getFreqs(), doc_index.get(reader.getDocId().getLength());
                    }
                    if(reader.NextPosting()==null){ //TODO vedere cosa fargli ritornare
                        reader.close();
                        to_delete.add(reader);
                    }
                }
            }
            postingReaders.removeAll(to_delete);




            doc_ids.add(min);
            scores.add(score);


            //TODO eventualmente fare in modo che droppi i risultati andanti dopo i 10
            // se si scopre che non serve mantenerne di più
        }



        return doc_ids;
    }



}
