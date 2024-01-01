package org.online_phase.scoring;

import org.common.PostingList;

public interface ScoringInterface {

    /**
     * Computes and returns the upper bound of the relative scoring function for a given PostingList.
     *
     * @param postingList The PostingList for which to calculate the upper bound.
     * @return The upper bound of TFIDF scores for the PostingList.
     */
    float getTermUpperBound(PostingList postingList);

    /**
     * Computes and returns the score.
     *
     * @param parameters An array of data useful to compute score.
     * @return The computed TFIDF or BM25 score.
     */
    float computeScore(int... parameters);
}
