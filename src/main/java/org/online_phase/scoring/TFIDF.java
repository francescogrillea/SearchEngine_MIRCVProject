package org.online_phase.scoring;

import org.common.DocIndexReader;
import org.common.PostingList;

/**
 * The TFIDF class implements the ScoringInterface and provides methods for scoring
 * documents using the Term Frequency-Inverse Document Frequency (TFIDF) formula.
 */
public class TFIDF implements ScoringInterface{

    private final int N;    // total number of documents in the collection

    public TFIDF(String doc_index_filename) {

        this.N = DocIndexReader.readN(doc_index_filename);
    }

    /**
     * Computes and returns the upper bound of the TFIDF scores for a given PostingList.
     *
     * @param postingList The PostingList for which to calculate the upper bound.
     * @return The upper bound of TFIDF scores for the PostingList.
     */
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

    /**
     * Computes and returns the TFIDF score based on the given term frequency (tf)
     * and document frequency (df) using the TFIDF formula.
     *
     * @param parameters An array of integers representing term frequency (tf) and document frequency (df).
     * @return The computed TFIDF score.
     */
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
