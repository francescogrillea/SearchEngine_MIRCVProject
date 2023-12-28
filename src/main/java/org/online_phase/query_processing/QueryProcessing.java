package org.online_phase.query_processing;

import org.online_phase.ScoreBoard;

public interface QueryProcessing {

    ScoreBoard executeQuery(String query, int top_k);

}
