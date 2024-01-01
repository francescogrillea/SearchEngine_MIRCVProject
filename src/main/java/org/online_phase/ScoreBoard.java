package org.online_phase;

import java.util.ArrayList;
import java.util.List;

/**
 * The ScoreBoard class represents a scoreboard for tracking document IDs and corresponding scores.
 * It maintains a sorted list of document IDs and scores, limiting the number of entries to a specified
 * maximum value (k). The scoreboard can be used to store and retrieve top-ranked documents based on their scores.
 */
public class ScoreBoard{

    private List<Integer> doc_ids;  // the doc_ids of the top results
    private List<Float> scores;     // the score associated to the relative doc_ids of the top results
    private final int MAX_RESULTS;  // k - maximum number of results to be returned
    private float threshold;        // the minimum score stored in the scoreboard

    public ScoreBoard(int max_result) {
        this.doc_ids = new ArrayList<>();
        this.scores = new ArrayList<>();
        this.MAX_RESULTS = max_result;
        this.threshold = 0;
    }

    /**
     * Adds a document ID and its corresponding score to the scoreboard.
     * The method ensures that the scoreboard remains sorted in descending order of scores
     * and limits the number of entries to the specified maximum.
     *
     * @param doc_id The document ID to be added.
     * @param score  The score corresponding to the document ID.
     * @return true if the addition is successful, false otherwise.
     */
    public boolean add(int doc_id, float score){


        // if the last score is higher than the current one, ignore it
        if(this.doc_ids.size() >= MAX_RESULTS && scores.get(MAX_RESULTS - 1) > score)
            return false;

        // otherwise iterate through the list until the correct position is found
        int index;
        for(index = 0; index < doc_ids.size() && scores.get(index) > score; index++)
            if(index > MAX_RESULTS - 1)
                return false;

        // insert the elements at the determined index
        this.doc_ids.add(index, doc_id);
        this.scores.add(index, score);

        if(this.doc_ids.size()>=MAX_RESULTS){
            this.threshold=this.scores.get(MAX_RESULTS-1);
        }
        return true;
    }

    /**
     * Clips the scoreboard to ensure it contains at most the maximum number of results.
     * If the number of entries exceeds the maximum, the extra entries are removed.
     */
    public void clip(){

        if(this.doc_ids.size() > MAX_RESULTS){
            this.doc_ids = this.doc_ids.subList(0, MAX_RESULTS);
            this.scores = this.scores.subList(0, MAX_RESULTS);
        }
    }

    public List<Integer> getDoc_ids() {
        return this.doc_ids;
    }

    public List<Float> getScores() {
        return this.scores;
    }

    public void setDoc_ids(List<Integer> doc_ids) {
        this.doc_ids = doc_ids;
    }

    public float getThreshold(){
        return this.threshold;
    }

    @Override
    public String toString() {

        if(this.doc_ids.isEmpty())
            return "No relevant document found according to the chosen criterion";

        StringBuilder result = new StringBuilder();
        result.append("Top ").append(this.MAX_RESULTS).append(" results: {\n");

        result.append("\t").append(this.doc_ids).append("\n");
        result.append("\t").append(this.scores).append("\n");

        result.append("}");
        return result.toString();
    }
}
