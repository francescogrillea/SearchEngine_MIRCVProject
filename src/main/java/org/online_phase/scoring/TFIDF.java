package org.online_phase.scoring;

import org.common.DocIndexReader;
import org.common.PostingList;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TFIDF implements ScoringInterface{

    private final int N;

    public TFIDF(String doc_index_filename) {

        this.N = DocIndexReader.readN(doc_index_filename);
    }

    @Override
    public float getTermUpperBound(PostingList postingList) {

        float upper_bound = 0;
        int tf;
        float tfidf;
        int df = postingList.getSize();

        for (int i = 0; i < postingList.getSize(); i++){
            tf = postingList.getTermFrequency(i);
            tfidf = computeScore(tf, df);
            if (tfidf > upper_bound)
                upper_bound = tfidf;
        }

        return upper_bound;
    }

    @Override
    public float computeScore(int... parameters) {

        int tf = parameters[0];
        int df = parameters[1];

        // compute formula
        return tf > 0 ? (float) (1 + Math.log(tf)) * (float) (Math.log((float) this.N / df)) : 0;
    }

    public int getN() {
        return N;
    }
}
