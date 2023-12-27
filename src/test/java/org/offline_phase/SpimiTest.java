package org.offline_phase;


import junit.framework.TestCase;
import junit.framework.TestResult;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.common.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.offline_phase.Spimi;

import static org.junit.jupiter.api.Assertions.*;
public class SpimiTest {

    Spimi spimi;


    @Test
    @DisplayName("Test DocIndex Bullding")
    public void doc_index_test() {

        Spimi spimi = new Spimi(true, false);
        spimi.run("data/smallest_collection.tar.gz");
        DocIndex docIndex_built = DocIndexReader.readDocIndex("data/doc_index.bin");


        DocIndex docIndexCORRECT = new DocIndex();
        DocInfo docInfo = new DocInfo(0, 10);
        docIndexCORRECT.add(1, docInfo);
        docInfo = new DocInfo(1, 6);
        docIndexCORRECT.add(2, docInfo);
        docInfo = new DocInfo(2, 10);
        docIndexCORRECT.add(3, docInfo);


        assertEquals(docIndex_built, docIndexCORRECT, "Lexicon Built is equal to the correct one");



    }

    @Test
    public void lexiconTest() {


        Lexicon lexicon_built = LexiconReader.readLexicon("data/lexicon.bin");


        List<String> words = new ArrayList<>();
        words.add("scienc");
        words.add("mind");
        words.add("intellect");
        words.add("manhattan");
        words.add("essai");
        words.add("project");
        words.add("bomb");
        words.add("presenc");
        words.add("scientif");
        words.add("equal");
        words.add("help");
        words.add("success");
        words.add("commun");
        words.add("atom");
        words.add("make");

        HashMap<String, TermEntryList> termEntryMap = new HashMap<>();


        for (int i = 0; i < words.size(); i++) {
            List<TermEntry> tmp = new ArrayList<>();
            int index = i;
            tmp.add(new TermEntry(-1, index, 10, 1, 1.0986123f, 0.418519f));
            TermEntryList correct = new TermEntryList(index, tmp);
            termEntryMap.put(words.get(i), correct);
        }


        Lexicon lexiconCORRECT = new Lexicon(termEntryMap, 0);

        Assert.assertEquals("Lexicon Built is equal to the correct one",lexicon_built, lexiconCORRECT);;

    }

public void IndexTest() {


            List<TermEntryList> index_built = null;
            for (String k : lexicon_built.keySet()){
                index_built.add(lexicon_built.get(k));
            }


            List<String> words = new ArrayList<>();
            words.add("scienc");
            words.add("mind");
            words.add("intellect");
            words.add("manhattan");
            words.add("essai");
            words.add("project");
            words.add("bomb");
            words.add("presenc");
            words.add("scientif");
            words.add("equal");
            words.add("help");
            words.add("success");
            words.add("commun");
            words.add("atom");
            words.add("make");



            // Manually insert values into PostingList
            PostingList postingList = new PostingList();
            postingList.addPosting(2, 1);  // Example data for doc_id=2 and term_freq=1
            postingList.addSkippingPointer(new SkippingPointer(2, (short) 1, (short) 1));  // Example SkippingPointer


            // Manually insert values into the second set of PostingList
            PostingList postingList2 = new PostingList();
            postingList2.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList2.addSkippingPointer(new SkippingPointer(1, (short) 1, (short) 1));  // Example SkippingPointer


            // Manually insert values into the third set of PostingList
            PostingList postingList3 = new PostingList();
            postingList3.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList3.addSkippingPointer(new SkippingPointer(1, (short) 1, (short) 1));  // Example SkippingPointer

            // Manually insert values into the fourth set of PostingList
            PostingList postingList4 = new PostingList();
            postingList4.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList4.addPosting(2, 1);  // Example data for doc_id=2 and term_freq=1
            postingList4.addPosting(3, 3);  // Example data for doc_id=3 and term_freq=3
            postingList4.addSkippingPointer(new SkippingPointer(2, (short) 2, (short) 1));  // Example SkippingPointer for doc_id=2
            postingList4.addSkippingPointer(new SkippingPointer(3, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=3

            // Manually insert values into the fifth set of PostingList
            PostingList postingList5 = new PostingList();
            postingList5.addPosting(3, 1);  // Example data for doc_id=3 and term_freq=1
            postingList5.addSkippingPointer(new SkippingPointer(3, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=3


            // Manually insert values into the sixth set of PostingList
            PostingList postingList6 = new PostingList();
            postingList6.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList6.addPosting(2, 1);  // Example data for doc_id=2 and term_freq=1
            postingList6.addPosting(3, 3);  // Example data for doc_id=3 and term_freq=3
            postingList6.addSkippingPointer(new SkippingPointer(2, (short) 2, (short) 1));  // Example SkippingPointer for doc_id=2
            postingList6.addSkippingPointer(new SkippingPointer(3, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=3


            // Manually insert values into the seventh set of PostingList
            PostingList postingList7 = new PostingList();
            postingList7.addPosting(2, 1);  // Example data for doc_id=2 and term_freq=1
            postingList7.addPosting(3, 1);  // Example data for doc_id=3 and term_freq=1
            postingList7.addSkippingPointer(new SkippingPointer(3, (short) 2, (short) 1));  // Example SkippingPointer for doc_id=3



            // Manually insert values into the eighth set of PostingList
            PostingList postingList8 = new PostingList();
            postingList8.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList8.addSkippingPointer(new SkippingPointer(1, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=1



            // Manually insert values into the ninth set of PostingList
            PostingList postingList9 = new PostingList();
            postingList9.addPosting(1, 2);  // Example data for doc_id=1 and term_freq=2
            postingList9.addSkippingPointer(new SkippingPointer(1, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=1

            // Manually insert values into the tenth set of PostingList
            PostingList postingList10 = new PostingList();
            postingList10.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList10.addSkippingPointer(new SkippingPointer(1, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=1


            // Manually insert values into the eleventh set of PostingList
            PostingList postingList11 = new PostingList();
            postingList11.addPosting(2, 1);  // Example data for doc_id=2 and term_freq=1
            postingList11.addSkippingPointer(new SkippingPointer(2, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=2


            // Manually insert values into the twelfth set of PostingList
            PostingList postingList12 = new PostingList();
            postingList12.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList12.addSkippingPointer(new SkippingPointer(1, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=1




            // Manually insert values into the thirteenth set of PostingList
            PostingList postingList13 = new PostingList();
            postingList13.addPosting(1, 1);  // Example data for doc_id=1 and term_freq=1
            postingList13.addSkippingPointer(new SkippingPointer(1, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=1


            // Manually insert values into the fourteenth set of PostingList
            PostingList postingList14 = new PostingList();
            postingList14.addPosting(2, 1);  // Example data for doc_id=2 and term_freq=1
            postingList14.addPosting(3, 1);  // Example data for doc_id=3 and term_freq=1
            postingList14.addSkippingPointer(new SkippingPointer(3, (short) 2, (short) 1));  // Example SkippingPointer for doc_id=3

            // Manually insert values into the fifteenth set of PostingList
            PostingList postingList15 = new PostingList();
            postingList15.addPosting(3, 1);  // Example data for doc_id=3 and term_freq=1
            postingList15.addSkippingPointer(new SkippingPointer(3, (short) 1, (short) 1));  // Example SkippingPointer for doc_id=3




            TermEntry term_entry_read;
            StringBuilder stringBuilder = new StringBuilder();
            for(i = 0; i < 15; i++ ) {
                term_entry_read=lexicon_built.get(words.get(i)).getTermEntry(i);
                assertEquals("termEntry"+ i+ "produced equals the correct one", term_entry_read, stringBuilder.append("termEntry").append(i).append("\n"));

            }

            Assert.assertEquals("Index Built is equal to the correct one",index_built, indexCORRECT);;


        }
    }
