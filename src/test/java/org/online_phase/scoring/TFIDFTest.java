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
        assertEquals(25.9082946F,tfidf.computeScore(2,2,3),0.001);

    }
    @Test
    public void getTermUpperBound(){
        //check if the term manhattan has the correct term upper bound
        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        TermEntryList termEntryList = lexicon.get("manhattan");

        PostingListReader.setEncoder(new VBEncoder(),new UnaryEncoder());
        PostingList postingList = PostingListReader.readPostingList(termEntryList.getTermEntryList().get(0));
        TFIDF tfidf = new TFIDF("data/doc_index.bin");
        float termUpperBound = tfidf.getTermUpperBound(postingList);
        assertEquals(25.543024F,termUpperBound,0.001);

    }
}
