package org.online_phase.scoring;

import org.common.DocIndex;
import org.common.DocIndexReader;
import org.common.DocInfo;
import org.common.PostingList;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class BM25 implements ScoringInterface{

    private final int N;
    private final int avdl;
    private final List<Integer> dl;

    public BM25(String doc_index_filename) {

        this.N = DocIndexReader.readN(doc_index_filename);

        /*
            TODO - non vorrei caricarlo tutto in memoria -> scrittura ordinata
                considerare che comunque teniamo in memoria anche this.dl -> 8.000.000 * 4 Bytes -> Circa 34MB
                senza considerare il docIndex e il lexicon
                questo si puo' fare prima di caricare il lexicon in memoria:
                    - calcolo dl
                    - chiudo tutti i channel a riguardo e termino la funzione -> magari una funzione clear
                    - leggo lexicon
         */

        DocIndex doc_index = DocIndexReader.readDocIndex(doc_index_filename);
        this.dl = new ArrayList<>();

        int sum = 0;
        int length;
        for (int i = 1; i <= this.N; i++) {
            length = doc_index.get(i).getLength();
            sum += length;
            this.dl.add(length);
        }
        doc_index.clear();

        this.avdl = sum / this.N;
    }

    @Override
    public float getTermUpperBound(PostingList postingList) {

        float upper_bound = 0;
        float result;

        for (int i = 0; i < postingList.getSize(); i++){
            result = computeScore(postingList.getTermFrequency(i), postingList.getSize(), this.dl.get(postingList.getDocId(i)-1));
            if (result > upper_bound) {
                upper_bound = result;
            }
        }

        return upper_bound;
    }

    @Override
    public float computeScore(int... parameters) {

        int tf = parameters[0];
        int df = parameters[1];
        int doc_len = parameters[2];

        float b = 0.75F;
        float k = 2F;
        float denominator = k*((1 - b) + b * ((float) doc_len / this.avdl)) + tf;
        float log = (float) Math.log((float) this.N / df);

        return tf / denominator * log;
    }
}
