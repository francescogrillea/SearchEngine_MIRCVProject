package org.common;
import org.common.encoding.EncoderInterface;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
public class PostingListReaderTest {
    @Test
    public void readingTest(){

        PostingListReader.setEncoder(new VBEncoder(),new UnaryEncoder());

        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");

        //try if we can read from index correctly
        PostingList manhattan = PostingListReader.readPostingList(lexicon.get("manhattan").getTermEntryList().get(0));

        List<Integer> docIDsCorretti = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1192, 2091, 2749, 2751, 2867, 4678, 4679, 5083, 5088, 5345);
        List<Integer> freqsCorrette = Arrays.asList(1, 1, 3, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        assertArrayEquals(docIDsCorretti.toArray(),manhattan.getDoc_ids().subList(0,20).toArray());
        assertArrayEquals(freqsCorrette.toArray(),manhattan.getTerm_frequencies().subList(0,20).toArray());
    }
}
