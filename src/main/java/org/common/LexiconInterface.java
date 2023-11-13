package org.common;

public interface LexiconInterface {

    /**
     * Searches for the index of a query string in a sorted list of strings.
     * If the query string is found, returns its index; otherwise, returns the index
     * in which the query string should be
     *
     * @param s The string to search for in the list.
     * @return The index of the query string if found; otherwise, the index of the next string.
     * @throws NullPointerException if query is null.
     */
    int indexOf(String s) throws NullPointerException;
    int indexOfNext(String s) throws NullPointerException;

}
