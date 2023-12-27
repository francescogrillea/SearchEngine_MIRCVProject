package org.common;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The TermEntryList class represents a list of TermEntry objects associated with a
 * specific term index. Is useful to store a list of TermEntry because when merging
 * intermediate posting lists, we have the location of all posting lists of that term
 * along all the intermediate index files.
 */
public class TermEntryList implements Iterable<TermEntry>{
    private int term_index; // an index associated to the respective term
    private final List<TermEntry> termEntryList;    // the list of TermEntry instances stored in the TermEntryList

    /**
     * Constructs a TermEntryList with a specified term index and an empty list of entries.
     *
     * @param term_index The term index associated with the list.
     */
    public TermEntryList(int term_index) {
        this.term_index = term_index;
        this.termEntryList = new ArrayList<>();
    }

    /**
     * Constructs a TermEntryList with a single specified TermEntry.
     *
     * @param termEntry The initial TermEntry to add to the list.
     */
    public TermEntryList(TermEntry termEntry){
        this.termEntryList = new ArrayList<>();
        this.termEntryList.add(termEntry);
    }

    /**
     * Adds a single TermEntry to the list.
     *
     * @param termEntry The TermEntry to add to the list.
     */
    public void addTermEntry(TermEntry termEntry){
        this.termEntryList.add(termEntry);
    }

    /**
     * Concatenate multiple TermEntry instances from another TermEntryList to the current list.
     *
     * @param entries The TermEntryList containing entries to add to the current list.
     */
    public void addTermEntries(TermEntryList entries){
        this.termEntryList.addAll(entries.getTermEntryList());
    }

    /**
     * Gets the term index associated with the TermEntryList.
     *
     * @return The term index.
     */
    public int getTerm_index() {
        return term_index;
    }

    /**
     * Gets the list of TermEntry instances stored in the TermEntryList.
     *
     * @return The list of TermEntry instances.
     */
    public List<TermEntry> getTermEntryList() {
        return termEntryList;
    }

    /**
     * Sets the term index associated with the TermEntryList.
     *
     * @param term_index The new term index.
     */
    public void setTerm_index(int term_index) {
        this.term_index = term_index;
    }

    /**
     * Resets the TermEntryList with a single specified TermEntry which will be the final one.
     *
     * @param termEntry The TermEntry to set as the sole entry in the list.
     */
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
