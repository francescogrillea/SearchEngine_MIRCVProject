package org.online_phase.scoring;

import org.common.*;
import org.common.encoding.EncoderInterface;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class BM25Test {
    @Test
    public void computeScore(){
        BM25 bm25 = new BM25("data/doc_index.bin");
        assertEquals(7.31482F,bm25.computeScore(3,3,2),0.01);
        //TODO: put the correct number, quando l'ho fatto c'erano solo 20000 docs
    }

    @Test
    public void getTermUpperBound(){
        //check if the term manhattan has the correct term upper bound
        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        TermEntryList termEntryList = lexicon.get("manhattan");
        EncoderInterface e_docid= new VBEncoder();
        EncoderInterface e_freq= new UnaryEncoder(); //TODO sistemare sta roba
        PostingListReader.setEncoder(e_docid,e_freq);
        PostingList postingList = PostingListReader.readPostingList(termEntryList.getTermEntryList().get(0));
        //System.out.println(postingList);
    }
}
