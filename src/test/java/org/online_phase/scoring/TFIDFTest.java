package org.online_phase.scoring;
import org.common.*;
import org.common.encoding.EncoderInterface;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class TFIDFTest {
    @Test
    public void computeScore(){
        TFIDF tfidf = new TFIDF("data/doc_index.bin");
        assertEquals(15.594462F,tfidf.computeScore(2,2,3),0.001);
        //TODO: mettere un numero corretto, quando l'ho fatto c'erano meno documenti
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
