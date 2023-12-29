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
        EncoderInterface e_docId = new VBEncoder();
        EncoderInterface e_freq = new UnaryEncoder();
        PostingListReader.setEncoder(e_docId,e_freq);

        Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");

        //try if we can read from index correctly
        PostingList manhattan = PostingListReader.readPostingList(lexicon.get("manhattan").getTermEntryList().get(0));
        System.out.println(manhattan.getDoc_ids());

        List<Integer> docIDsCorretti = Arrays.asList(0,0);

        assertArrayEquals(docIDsCorretti.toArray(),manhattan.getDoc_ids().toArray());
    }
}
