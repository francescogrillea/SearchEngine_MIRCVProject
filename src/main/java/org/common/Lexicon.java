package org.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Lexicon implements LexiconInterface{

    List<TermEntry> lexicon;

    public Lexicon() {
        this.lexicon = new ArrayList<TermEntry>();
    }

    public int addTerm(String s){

        if(lexicon.size() == 0){
            lexicon.add(new TermEntry(s));
            return 0;
        }

        int i = indexOfNext(s);
        this.lexicon.add(i, new TermEntry(s));
        return i;
    }

    public int addTerm(TermEntry termEntry){
        if(lexicon.size() == 0){
            lexicon.add(termEntry);
            return 0;
        }

        int i = indexOfNext(termEntry.getTerm());
        this.lexicon.add(i, termEntry);
        return i;
    }

    @Override
    public int indexOf(String s){

        int low = 0;
        int high = lexicon.size() - 1;
        int mid;
        int comparison;

        while (low <= high){
            mid = low + (high - low) / 2;
            comparison = s.compareTo(this.lexicon.get(mid).getTerm());

            if (comparison == 0) {
                return mid; // Query found
            } else if (comparison < 0) {
                high = mid - 1; // Query is in the lower half
            } else {
                low = mid + 1; // Query is in the upper half
            }
        }

        return -1;
    }

    @Override
    public int indexOfNext(String s) throws NullPointerException {

        if(s == null || s.isEmpty())
            throw new NullPointerException();

        int low = 0;
        int high = lexicon.size() - 1;
        int mid;
        int comparison;

        while (low <= high) {
            mid = low + (high - low) / 2;
            comparison = s.compareTo(this.lexicon.get(mid).getTerm());

            if (comparison == 0) {
                return mid; // Query found
            } else if (comparison < 0) {
                high = mid - 1; // Query is in the lower half
            } else {
                low = mid + 1; // Query is in the upper half
            }
        }
        return low;
    }


    public List<TermEntry> getLexicon() {
        return lexicon;
    }

    @Override
    public String toString() {
        return "Lexicon: " + lexicon;
    }
}
