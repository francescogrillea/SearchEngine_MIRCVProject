package org.online_phase.scoring;

import org.common.DocIndex;
import org.common.DocIndexReader;
import org.common.PostingList;


/**
 * The BM25 class implements the ScoringInterface and provides methods for scoring
 * documents using the BM25 algorithm, a relevance ranking function used in information
 * retrieval. BM25 takes into account the term frequency, document frequency, and document
 * length to compute a relevance score.
 */
public class BM25 implements ScoringInterface{

    private final int N;    // total number of documents in the collection
    private final int avdl; // average document length in the collection
    private final int[] dl; // an array storing the document lengths for each document

    public BM25(String doc_index_filename) {

        this.N = DocIndexReader.readN(doc_index_filename);
        this.dl = new int[this.N];

        // Load the whole docIndex in memory to compute DL and ADVL, then remove it to save space
        DocIndex doc_index = DocIndexReader.readDocIndex(doc_index_filename);

        int sum = 0;
        int length;
        for (int i = 1; i <= this.N; i++) {
            length = doc_index.get(i).getLength();
            sum += length;
            this.dl[i-1] = length;
        }
        doc_index.clear();
        doc_index = null;
        System.gc();

        this.avdl = sum / this.N;
    }

    /**
     * Computes and returns the upper bound of the BM25 scores for a given PostingList.
     *
     * @param postingList The PostingList for which to calculate the upper bound.
     * @return The upper bound of BM25 scores for the PostingList.
     */
    @Override
    public float getTermUpperBound(PostingList postingList) {

        float upper_bound = 0;
        float result;

        for (int i = 0; i < postingList.getSize(); i++){
            result = computeScore(postingList.getTermFrequency(i), postingList.getSize(), this.dl[postingList.getDocId(i)-1]);
            if (result > upper_bound) {
                upper_bound = result;
            }
        }

        return upper_bound;
    }

    /**
     * Computes and returns the BM25 score based on the given term frequency (tf),
     * document frequency (df), and document length (doc_len) using the BM25 formula.
     *
     * @param parameters An array of integers representing term frequency (tf),
     *                   document frequency (df), and document length (doc_len).
     * @return The computed BM25 score.
     */
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

    /**
     * Gets the document length for a specific document.
     *
     * @param i The index of the document.
     * @return The document length for the specified document.
     */
    public int getDl(int i) {
        return this.dl[i];
    }
}
