package org.common;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TermEntryList implements Iterable<TermEntry>{        // TODO - should implement TermEntryInterface ?
    private int term_index;
    private List<TermEntry> termEntryList;

    public TermEntryList(int term_index) {
        this.term_index = term_index;
        this.termEntryList = new ArrayList<>();
    }

    public TermEntryList(TermEntry termEntry){
        this.termEntryList = new ArrayList<>();
        this.termEntryList.add(termEntry);
    }

    public void addTermEntry(TermEntry termEntry){
        this.termEntryList.add(termEntry);
    }

    public void addTermEntries(TermEntryList entries){
        this.termEntryList.addAll(entries.getTermEntryList());
    }

    public int getTerm_index() {
        return term_index;
    }

    public TermEntry getTermEntry(int index){
        return this.termEntryList.get(index);
    }

    public List<TermEntry> getTermEntryList() {
        return termEntryList;
    }

    public void setTerm_index(int term_index) {
        this.term_index = term_index;
    }

    public void resetTermEntry(TermEntry termEntry) {
        this.termEntryList.clear();
        this.termEntryList.add(termEntry);
    }

    @Override
    public String toString() {
        return "TermEntryList{" +
                "term_index=" + term_index +
                ", termEntryList=" + termEntryList +
                '}';
    }

    @Override
    public Iterator<TermEntry> iterator() {
        return this.termEntryList.iterator();
    }

    public ByteBuffer serialize() {
        if(this.termEntryList.size() > 1)
            throw new ArrayIndexOutOfBoundsException();

        return this.termEntryList.get(0).serialize();
    }
}
