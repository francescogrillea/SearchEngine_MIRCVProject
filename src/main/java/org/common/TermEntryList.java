package org.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TermEntryList implements Serializable{        // TODO - should implement TermEntryInterface ?
    private transient final int term_index;
    private List<TermEntry> termEntryList;

    public TermEntryList(int term_index) {
        this.term_index = term_index;
        this.termEntryList = new ArrayList<>();
    }

    public void addTermEntry(TermEntry termEntry){
        this.termEntryList.add(termEntry);
    }

    public int getTerm_index() {
        return term_index;
    }

    @Override
    public String toString() {
        return "TermEntryList{" +
                "term_index=" + term_index +
                ", termEntryList=" + termEntryList +
                '}';
    }
}
