package org.online_phase.scoring;

import org.common.PostingList;

public interface ScoringInterface {

    float getTermUpperBound(PostingList postingList);
    float computeScore(int... parameters);
}
