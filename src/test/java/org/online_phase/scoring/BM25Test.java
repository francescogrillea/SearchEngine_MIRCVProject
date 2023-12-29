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
        assertEquals(12.3890771865F,bm25.computeScore(3,3,2),0.01);

    }

    @Test
    public void getTermUpperBound(){
        //check if the term manhattan has the correct term upper bound
        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");
        TermEntryList termEntryList = lexicon.get("manhattan");

        PostingListReader.setEncoder(new VBEncoder(),new UnaryEncoder());
        PostingList postingList = PostingListReader.readPostingList(termEntryList.getTermEntryList().get(0));
        BM25 bm25 = new BM25("data/doc_index.bin");
        float termUpperBound = bm25.getTermUpperBound(postingList);
        assertEquals(6.204782F,termUpperBound,0.001);
    }
}
