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
    public double getTermUpperBound(PostingList postingList) {

        double upper_bound = 0;
        int tf;
        double tfidf;
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
    public double computeScore(int... parameters) {

        int tf = parameters[0];
        int df = parameters[1];

        float tfidf;
        tfidf = (float) (1 + Math.log(tf)) * (float) (Math.log( (double) this.N / df));
        // magic

        // TODO - ??? perche' dovrebbe ritornare qualcosa < 0 ????
        return tfidf > 0 ? tfidf : 0;
    }

    public int getN() {
        return N;
    }
}
