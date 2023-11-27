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

    @Override
    public String toString() {
        return "Lexicon{" +
                "lexicon=" + lexicon +
                '}';
    }
}
