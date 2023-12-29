package org.common;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;
public class PostingListBlockReaderTest {
    @Test
    public void readFromIndex(){
        //prendo la tmentry di manattan e la leggo tuttta
        //ci creo un blockreader sopra e la leggo a pezzi
        //confronto
        try {
            FileChannel fc = (new FileInputStream("data/index.bin")).getChannel();
            Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");
            TermEntry t = lexicon.get("manhattan").getTermEntryList().get(0);
            PostingListReader.setEncoder(new VBEncoder(),new UnaryEncoder());
            PostingListBlockReader pb = new PostingListBlockReader(t,"manhattan",true);

            PostingList p= PostingListReader.readPostingList(t);

            pb.readBlock();


            for(int i = 0; i< p.getSize();i++){ //check sequentially
                assertEquals(pb.getDocID(),p.getDocId(i));
                assertEquals(pb.getTermFreq(),p.getTermFrequency(i));
                pb.nextPosting();
            }


        }catch(FileNotFoundException e){
            System.out.println(e.getMessage());
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void nextGEQ(){
        try {

            FileChannel fc = (new FileInputStream("data/index.bin")).getChannel();
            Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");
            TermEntry t = lexicon.get("manhattan").getTermEntryList().get(0);
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
            PostingListBlockReader pb = new PostingListBlockReader(t, "manhattan", true);

            PostingList p = PostingListReader.readPostingList(t);
            pb.readBlock();

            int[] indexes = new int[]{2,4,8};
            for(int i=0; i<indexes.length; i++){
                int docId= p.getDocId(indexes[i]);
                assertEquals((int)pb.nextGEQ(docId),p.getTermFrequency(indexes[i]));
            }

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage());
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

}
