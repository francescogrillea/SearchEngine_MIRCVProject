package org.common;

public interface LexiconInterface {

    /**
     * Adds a term to the lexicon without associated posting list entries.
     *
     * @param term The term to be added to the lexicon.
     * @return The index in which the posting list relative to the given term must be stored in the
     *         inverted index.
     */
    int add(String term);

    /**
     * Adds a term along with its associated posting list entries to the lexicon.
     *
     * @param term    The term to be added to the lexicon.
     * @param entries The reference to the posting list(s) associated with the given term.
     * @return The index in which the posting list relative to the given term must be stored in the
     *         inverted index.
     */
    int add(String term, TermEntryList entries);

}
