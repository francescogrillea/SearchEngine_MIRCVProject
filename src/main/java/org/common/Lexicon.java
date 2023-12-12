package org.common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Lexicon implements LexiconInterface {

    private final HashMap<String, TermEntryList> lexicon;
    private int size = 0;

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
            i = new TermEntry(buffer.getInt(), buffer.getLong(), buffer.getLong());

            this.lexicon.put(new String(word, StandardCharsets.UTF_8), new TermEntryList(i));
        }
        //System.out.println("Lexicon read: " + lexicon);
    }

    @Override
    public int add(String term) {
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

        TermEntryList termEntries = this.lexicon.get(term);
        int index;
        if(termEntries == null){
//            termEntries = new TermEntryList(this.size);
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

    public ByteBuffer serializeEntry(String key){

        // TODO - Short.BYTES + byte_repr.length + TermEntry.BYTES
        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES + (2 * key.length()) + TermEntry.BYTES);    // at most 2 bytes for each char
        byte[] byte_repr = key.getBytes();
        byteBuffer.putShort((short) byte_repr.length);    // store the length of the term -> 2 words are > 128
        byteBuffer.put(byte_repr);

        byteBuffer.put(this.get(key).serialize());

        byteBuffer.flip();
        byteBuffer = byteBuffer.compact();  // TODO rimuovere!

        byteBuffer.flip();
        return byteBuffer;
    }

    public void merge(Lexicon new_lexicon){

        // TODO - se this.lexicon is empty -> this.lexicon = new_lexicon
        for(String term : new_lexicon.keySet()){
            add(term, new_lexicon.get(term));
        }
    }

    public Set<String> keySet(){
        return this.lexicon.keySet();
    }

    public TermEntryList get(String term){
        return this.lexicon.get(term);
    }

    public HashMap<String, TermEntryList> getLexicon() {
        return lexicon;
    }

    @Override
    public String toString() {
        return "Lexicon{" +
                "lexicon=" + lexicon +
                '}';
    }
}
