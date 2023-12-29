package org.online_phase;

import java.util.ArrayList;
import java.util.List;

public class ScoreBoard{

    private List<Integer> doc_ids;
    private List<Float> scores;
    private final int MAX_RESULTS;
    private float threshold;

    public ScoreBoard(int max_result) {
        this.doc_ids = new ArrayList<>();
        this.scores = new ArrayList<>();
        this.MAX_RESULTS = max_result;
        this.threshold = 0;
    }

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
        // TODO - valutare se eliminare l'ultimo elemento o clippare dopo
    }

    public void clip(){

//        int prev = 0;
//        int curr = 0;
//        for(int doc_id : this.doc_ids){
//            prev = curr;
//            curr = doc_id;
//            if(curr < prev)
//                System.out.println("No order between them\t" + curr + "=" + prev);
//
//        }

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
