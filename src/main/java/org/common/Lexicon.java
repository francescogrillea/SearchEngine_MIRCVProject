package org.common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The Lexicon class implements the LexiconInterface and represents a mapping of
 * terms to TermEntryList, which is a data structure that stores information about
 * the (intermediate and final) posting list of a given term.
 *
 * Is useful to store a TermEntryList because when merging intermediate posting lists,
 * we have the location of all posting lists of that term along all the intermediate index files.
 */
public class Lexicon implements LexiconInterface {

    private final HashMap<String, TermEntryList> lexicon;   // the underlying HashMap for storing terms and their corresponding TermEntryLists
    private int size = 0;   // the current size of the lexicon, representing the number of unique terms.

    public Lexicon() {
        this.lexicon = new HashMap<>();
    }

    public Lexicon(ByteBuffer buffer){

        this.lexicon = new HashMap<>();
        int word_length;
        byte[] word;
        TermEntry i;

        while (buffer.hasRemaining()){

            // read how many byes the term need
            word_length = buffer.getShort();
            // read the term
            word = new byte[word_length];
            buffer.get(word);

            // read the term entry
            i = new TermEntry(buffer.getInt(),
                    buffer.getLong(),
                    buffer.getLong(),
                    buffer.getInt(),
                    buffer.getFloat(),
                    buffer.getFloat()
            );

            this.lexicon.put(new String(word, StandardCharsets.UTF_8), new TermEntryList(i));
        }
    }

    @Override
    public int add(String term) {

        /*  Note that each term has a TermIndex (inside the TermEntryList object)
            in such a way to identify the posting list associated to that term
         */

        TermEntryList termEntries = this.lexicon.get(term);
        if(termEntries == null){
            termEntries = new TermEntryList(this.size);
            this.lexicon.put(term, termEntries);
            this.size++;
        }
        return termEntries.getTerm_index();
    }

    @Override
    public int add(String term, TermEntryList entries){

        /*  Note that each term has a TermIndex (inside the TermEntryList object)
            in such a way to identify the posting list associated to that term
         */

        TermEntryList termEntries = this.lexicon.get(term);
        int index;
        if(termEntries == null){
            entries.setTerm_index(this.size);
            index = this.size;
            this.lexicon.put(term, entries);
            this.size++;
        }else{
            index = this.lexicon.get(term).getTerm_index();
            this.lexicon.get(term).addTermEntries(entries);
        }
        return index;
    }

    /**
     * Serializes an individual Lexicon entry into a ByteBuffer.
     *
     * @param key The term for which to serialize the entry.
     * @return A ByteBuffer containing the serialized Lexicon entry.
     */
    public ByteBuffer serializeEntry(String key){

        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES + (2 * key.length()) + TermEntry.BYTES);    // at most 2 bytes for each char
        byte[] byte_repr = key.getBytes();
        byteBuffer.putShort((short) byte_repr.length);    // store the length of the term -> 2 words are > 128, so a short must be used
        byteBuffer.put(byte_repr);

        // serialize the TermEntryList object
        byteBuffer.put(this.get(key).serialize());

        byteBuffer.flip();
        byteBuffer = byteBuffer.compact();

        byteBuffer.flip();
        return byteBuffer;
    }

    /**
     * Merges the current Lexicon with another Lexicon appending TermEntries to the TermEntryList of the same term.
     *
     * @param new_lexicon The Lexicon to be merged into the current Lexicon.
     */
    public void merge(Lexicon new_lexicon){

        for(String term : new_lexicon.keySet())
            add(term, new_lexicon.get(term));
    }

    /**
     * Retrieves the set of terms (keys) in the Lexicon.
     *
     * @return A set of terms present in the Lexicon.
     */
    public Set<String> keySet(){
        return this.lexicon.keySet();
    }

    /**
     * Retrieves the TermEntryList associated with a specific term.
     *
     * @param term The term for which to retrieve the TermEntryList.
     * @return The TermEntryList associated with the specified term.
     */
    public TermEntryList get(String term){
        return this.lexicon.get(term);
    }

    /**
     * Returns a string representation of the Lexicon, mainly for debugging purposes.
     *
     * @return A string representation of the Lexicon.
     */
    @Override
    public String toString() {
        return "Lexicon{" +
                "lexicon=" + lexicon +
                '}';
    }
}
