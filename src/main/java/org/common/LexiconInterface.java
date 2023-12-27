package org.common;

public interface LexiconInterface {

    /**
     * Adds a term to the lexicon without associated posting list entries.
     *
     * @param term The term to be added to the lexicon.
     * @return An index which identifies the posting list relative to the given term.
     */
    int add(String term);

    /**
     * Appends a list of term entry to the TermEntryList relative to the given term
     *
     * @param term    The term to be added to the lexicon.
     * @param entries A list TermEntry associated to the given term.
     * @return An index which identifies the posting list relative to the given term.
     */
    int add(String term, TermEntryList entries);

}
