package org.online_phase.scoring;

import org.common.PostingList;

public interface ScoringInterface {

    double getTermUpperBound(PostingList postingList);
    double computeScore(int... parameters);
}
