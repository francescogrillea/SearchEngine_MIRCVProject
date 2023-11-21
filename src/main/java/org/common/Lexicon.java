package org.common;

import java.io.Serializable;
import java.util.*;

public class Lexicon implements LexiconInterface, Serializable {

    private HashMap<String, TermEntryList> lexicon;
    private transient int size = 0;

    public Lexicon() {
        this.lexicon = new HashMap<>();
    }

    @Override
    public int add(String term){

        TermEntryList termEntries = lexicon.get(term);
        if(termEntries == null){
            termEntries = new TermEntryList(this.size);
            lexicon.put(term, termEntries);
            this.size++;
        }
        return termEntries.getTerm_index();
    }

    public Set<String> keySet(){
        return this.lexicon.keySet();
    }

    public TermEntryList get(String term){
        return this.lexicon.get(term);
    }

    @Override
    public String toString() {
        return "Lexicon{" +
                "lexicon=" + lexicon +
                '}';
    }
}
