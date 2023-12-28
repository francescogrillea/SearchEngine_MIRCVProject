package org.common;

import org.junit.Test;

import java.nio.ByteBuffer;


import static org.junit.Assert.assertEquals;
public class LexiconTest {
    @Test
    public void add(){
        Lexicon lexicon = new Lexicon();
        lexicon.add("manhattan");
        lexicon.add("stonhenge");
        assertEquals(0,lexicon.get("manhattan").getTerm_index());
        assertEquals(1,lexicon.get("stonhenge").getTerm_index());
        TermEntryList termEntryList = new TermEntryList(new TermEntry(2,2,2,2));
        lexicon.add("stonhenge",termEntryList);
        assertEquals(2,lexicon.get("stonhenge").getTermEntryList().get(0).getBlock_index());
    }

    @Test
    public void writeAndRead(){ //we try to write down and then read a lexicon
        Lexicon lexicon = new Lexicon();
        lexicon.add("manhattan");
        lexicon.add("stonhenge");
        TermEntryList termEntryList = new TermEntryList(new TermEntry(2,2L,2L,2,2F,2F));
        lexicon.add("stonhenge",termEntryList);
        lexicon.add("manhattan",termEntryList);


        ByteBuffer temp;
        ByteBuffer def=ByteBuffer.allocate(0);
        for(String key: lexicon.keySet()) {
            temp= ByteBuffer.allocate(lexicon.serializeEntry(key).capacity()+def.capacity());
            temp.put(def);
            temp.put(lexicon.serializeEntry(key));
            temp.flip();
            def= temp;
        }
        Lexicon lexiconReaded= new Lexicon(def);

        assertEquals(2L,lexiconReaded.get("stonhenge").getTermEntryList().get(0).getLength());
        assertEquals(2F,lexiconReaded.get("manhattan").getTermEntryList().get(0).getTfidf_upper_bound(),0.001);
    }
}
