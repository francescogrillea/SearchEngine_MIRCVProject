package org.offline_phase;

import org.common.PostingList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IntermediatePostings implements Serializable {

    private List<String> terms;
    private List<PostingList> postingLists;

    public IntermediatePostings() {
        terms = new ArrayList<>();
        postingLists = new ArrayList<>();
    }


    public void addPosting(String term, int doc_id){
        int i = 0;
        int result;
        for(String t : terms){
            result = term.compareTo(t);

            // to handle sorted insertion
            if (result < 0)
                break;

            // if term already in intermediate posting, update its posting list
            if (result == 0){
                postingLists.get(i).addPosting(doc_id);
                return;
            }
            i++;
        }
        terms.add(i, term);
        postingLists.add(i, new PostingList(doc_id));
    }

    public void addPostingList(String term, PostingList intermediate_posting){
        int i = 0;
        int result;
        for(String t : terms){
            result = term.compareTo(t);

            // to handle sorted insertion
            if (result < 0)
                break;

            // if term already in intermediate posting, update its posting list
            if (result == 0){
                postingLists.get(i).concatenatePostings(intermediate_posting.getPostingList());
                return;
            }
            i++;
        }
        terms.add(i, term);
        postingLists.add(i, intermediate_posting);
    }


//    // TODO guardare le previouse push su git
//    public void addPosting(String term, int doc_id){
//
//        int result = 1;
//        int i = 0;
//        for(i = 0; i < this.terms.size() && result > 0 ; i++){
//            result = term.compareTo(this.terms.get(i));
//        }
//
//        if(result == 0)
//            this.postingLists.get(i).addPosting(doc_id);
//        else{
//            this.terms.add(i, term);
//            this.postingLists.add(i, new PostingList(doc_id));
//        }
//    }
//
//    public void addPostingList(String term, PostingList intermediate_posting){
//
//        int result = 1;
//        int i = 0;
//        for(i = 0; i < this.terms.size() && result > 0 ; i++){
//            result = term.compareTo(this.terms.get(i));
//        }
//
//        if(result == 0)
//            postingLists.get(i).concatenatePostings(intermediate_posting.getPostingList());
//        else{
//            this.terms.add(i, term);
//            this.postingLists.add(i, intermediate_posting);
//        }
//    }
//

    public void merge(IntermediatePostings intermediate_posting){

        for(int i = 0; i < intermediate_posting.size(); i++){
            addPostingList(intermediate_posting.terms.get(i), intermediate_posting.getPostingLists().get(i));
        }
    }

    public int size() {
        return this.terms.size();
    }


    public void clear(){
        this.terms.clear();
        this.postingLists.clear();
    }


    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public List<PostingList> getPostingLists() {
        return postingLists;
    }

    public void setPostingLists(List<PostingList> postingLists) {
        this.postingLists = postingLists;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < this.size(); i++){
            out.append(this.terms.get(i)).append(": \t");
            out.append(this.postingLists.get(i)).append("\n");
        }
        return out.toString();
    }

}
