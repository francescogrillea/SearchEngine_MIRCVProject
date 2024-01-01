package org.online_phase.query_processing;

import org.online_phase.ScoreBoard;

public interface QueryProcessing {

    /**
     * Executes a query and retrieves the top-k results.
     *
     * @param query The query string to be processed.
     * @param top_k The number of top results to retrieve.
     * @return A ScoreBoard containing the top-k document IDs and their corresponding scores.
     */
    ScoreBoard executeQuery(String query, int top_k);

}
